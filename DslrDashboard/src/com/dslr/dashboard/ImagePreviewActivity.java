/*
	<DslrDashboard - controling DSLR camera with Android phone/tablet>
    Copyright (C) <2012>  <Zoltan Hubai>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package com.dslr.dashboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import com.dslr.dashboard.imgzoom.*;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ImagePreviewActivity extends ActivityBase {

	private static String TAG = "ImagePreviewActivity";
	private ListView mExifList;
	private ImageView mHistogramView;
	private boolean mHistogramSeparate = true;
	private Boolean mIsExifVisible = false;

	private String[] mExifNames;
	
    /** Image zoom view */
    private ImageZoomView mZoomView;
    
    private TextView mTxtLoading;
    private LinearLayout mProgressLayout;

    /** Zoom control */
    private DynamicZoomControl mZoomControl;

    /** Decoded bitmap image */
    private Bitmap mBitmap = null;

    /** On touch listener for zoom view */
//    private LongPressZoomListener mZoomListener;
    
    private String _imgPath;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		
		if (savedInstanceState != null) {
			String tmpPath = savedInstanceState.getString("tmpFile");
			if (!tmpPath.isEmpty()) {
				Log.i(TAG, "Restore from tmp file");
				File f = new File(tmpPath);
				mBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
				Log.i(TAG, "Delete temp file");
				if (!f.delete())
					Log.d(TAG, "tmp file not deleted");
				
			}
			//mBitmap = savedInstanceState.getParcelable("bitmap");
			_imgPath = savedInstanceState.getString("imgPath");
			
			
		}
		
        mLongPressTimeout = ViewConfiguration.getLongPressTimeout();
        mScaledTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        mScaledMaximumFlingVelocity = ViewConfiguration.get(this)
                .getScaledMaximumFlingVelocity();
        mVibrator = (Vibrator)getSystemService("vibrator");
        
        
		setContentView(R.layout.activity_image_preview);

        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);
		
        mZoomControl = new DynamicZoomControl();

        mZoomView = (ImageZoomView)findViewById(R.id.zoomview);
        mZoomView.setZoomState(mZoomControl.getZoomState());
        mZoomView.setOnTouchListener(mZoomListener);
        mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());
        
        mZoomView.setVisibility(View.GONE);

		mHistogramView = (ImageView)findViewById(R.id.histogram_view);
		mHistogramView.setAlpha((float)0.4);
		mHistogramView.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mHistogramSeparate = !mHistogramSeparate;
				mHistogramView.setImageBitmap(NativeMethods.getInstance().createHistogramBitmap(mBitmap, mHistogramSeparate));
			}
		});
        
        mTxtLoading = (TextView)findViewById(R.id.txtLoading);
        mProgressLayout = (LinearLayout)findViewById(R.id.progresslayout);
        
        mExifList = (ListView)findViewById(R.id.exifList);
        
        mExifNames = getResources().getStringArray(R.array.exifNames);
        
	}
	
	private void toggleActionBar(){
		if (getActionBar().isShowing())
			getActionBar().hide();
		else
			getActionBar().show();
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_image_preview, menu);
        return true;
    }

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
		case R.id.menu_histogram:
			if (mHistogramView.getVisibility() == View.GONE) {
				mHistogramView.setImageBitmap(NativeMethods.getInstance().createHistogramBitmap(mBitmap, mHistogramSeparate));
				mHistogramView.setVisibility(View.VISIBLE);
			}
			else
				mHistogramView.setVisibility(View.GONE);
			return true;
			case R.id.menu_exif:
				displayExif();
				
				return true;
			case R.id.menu_share:
				shareImage();
				return true;
			default:
				return true;
		}
	}
    
	private void displayExif() {
		if (_imgPath != null && !_imgPath.isEmpty()){
			
			if (!mIsExifVisible) {
				ArrayList<ExifDataHelper> exifs = NativeMethods.getInstance().getImageExif(mExifNames, _imgPath);
				ExifAdapter adapter = new ExifAdapter(this, exifs);
				mExifList.setAdapter(adapter);
			}
			mIsExifVisible = !mIsExifVisible;
			mExifList.setVisibility(mIsExifVisible ? View.VISIBLE : View.GONE);
		}
	}
	private int reqCode = 1;
	private String tmpPath;
	private boolean isTemporary = false;
	
	private void shareImage(){
		tmpPath = _imgPath;
		if (!tmpPath.isEmpty() && mBitmap != null) {
			// if nef we need to save to jpg
			if (tmpPath.substring((tmpPath.lastIndexOf(".") + 1), tmpPath.length()).toLowerCase().equals("nef")) {
				tmpPath = tmpPath.substring(0, tmpPath.lastIndexOf(".") + 1) + "jpg";
				
				isTemporary = saveAsJpeg(new File(tmpPath));
				if (isTemporary)
					NativeMethods.getInstance().copyExifData(_imgPath, tmpPath);
				
			}
			
			File f = new File(tmpPath);
		    ContentValues values = new ContentValues(2);
		    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
		    values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
		    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		    Intent intent = new Intent(Intent.ACTION_SEND);
		    intent.setType("image/png");
		    intent.putExtra(Intent.EXTRA_STREAM, uri);
		    intent.putExtra(Intent.EXTRA_TEXT, "Shared with DslrDashboard");
		    startActivityForResult(Intent.createChooser(intent , "How do you want to share?"), reqCode);
		    
		}
		
	}
	
	private boolean saveAsJpeg(File f) {
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(f);
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
			fOut.flush();
			fOut.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");
		if (requestCode == reqCode) {
			Log.d(TAG, "Result: " + resultCode);
			if (isTemporary) {
				Log.d(TAG, "Temporary file, delete");
				File f = new File(tmpPath);
				f.delete();
			}
			else
				Log.d(TAG, "Original file shared");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		if (mBitmap != null)
			loadImage(null);
		else {
			Intent intent = getIntent();
			if (intent != null){
				Log.d(TAG, "Image from intent");
				Uri data = intent.getData();
				if (data != null) {
					String path = data.getEncodedPath();
					Log.d(TAG, "Image path " + path);
					_imgPath = path;
					loadImage(path);
				}
				else if (intent.hasExtra("data")){
					Log.d(TAG, "Image from bitmap ");
				    mBitmap = BitmapFactory.decodeByteArray(
				            intent.getByteArrayExtra("data"),0,getIntent().getByteArrayExtra("data").length);
				    loadImage(null);
				}
				else {
					Log.d(TAG, "No data in intent");
					loadImage(null);
				}
			}
			else {
				Log.d(TAG, "No Intent");
				loadImage(null);
			}
		}
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		if (mBitmap != null)
			mBitmap.recycle();
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mBitmap != null) {
			// check if this is a nef file, then save as temp file
			
			File cache = getCacheDir();
			File f;
			try {
				f = File.createTempFile("dslr", "jpg", cache);
				if (saveAsJpeg(f)) {
					Log.d(TAG, "Tmp file: " + f.getAbsolutePath());
					outState.putString("tmpFile", f.getAbsolutePath());
				}
			} catch (IOException e) {
			}
			
		}
		if (_imgPath != null && !_imgPath.isEmpty())
			outState.putString("imgPath", _imgPath);
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				if (mBitmap != null) {
					mBitmap.recycle();					
					//mBitmap = null;
				}
				finish();
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}
	
	private Bitmap loadJpeg(String path)   {
	    Log.i(TAG,"loading:"+path);
		//final int IMAGE_MAX_SIZE = 8000000; // 1.2MP
		
	    Bitmap bm = null;
	    File file=new File(path);
	    
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        try {
        	FileInputStream fis = new FileInputStream(file);
        	BitmapFactory.decodeStream(fis, null, o);
        	fis.close();
        } catch (IOException e) {
        	return null;
        }
        
        int scale = 1;
        int width = o.outWidth;
        int height = o.outHeight;
        while ((int)(width / scale) > 2048 || (int)(height / scale) > 2048)
        	scale++;
        scale++;
//        while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
//            scale++;
//        }
        Log.i(TAG, "Sample size: " + scale);
        
	    BitmapFactory.Options bfOptions=new BitmapFactory.Options();
	    bfOptions.inMutable = true;
	    bfOptions.inSampleSize = scale;
	    bfOptions.inDither=false;                     //Disable Dithering mode
	    bfOptions.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
	    bfOptions.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
	    bfOptions.inTempStorage=new byte[32 * 1024]; 


	    FileInputStream fs=null;
	    try {
	        fs = new FileInputStream(file);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }

	    try {
	        if(fs!=null) 
	        	bm=BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
	    } catch (IOException e) {
	    	
	    } finally{ 
	        if(fs!=null) {
	            try {
	                fs.close();
	            } catch (IOException e) {
	            	
	            }
	        }
	    }
	    return bm;
	}	


	private void displayBitmap(final Bitmap bmp){
		runOnUiThread(new Runnable() {
			
			public void run() {
				mProgressLayout.setVisibility(View.GONE);
				//mTxtLoading.setVisibility(View.GONE);
				mZoomView.setImage(bmp);
				mZoomView.setVisibility(View.VISIBLE);
		        resetZoomState();
			}
		});
	}
	
	private float calcBarHeight(int value, int max, int height) {
		float proc = (float)(100 * value) / (float)max;
		return (float) height * proc / (float) 100;
	}
	private void loadImage(final String path){
        new Thread(new Runnable() {
			
			public void run() {
				
				if (path != null && !path.isEmpty()) {
					File f = new File(path);
					if (f.exists()) {
						if (path.substring((path.lastIndexOf(".") + 1), path.length()).toLowerCase().equals("nef")) {
							mBitmap = (Bitmap)NativeMethods.getInstance().loadRawImage(path);
							Log.d(TAG, "IsMutable: " + mBitmap.isMutable());
							//drawHistogram(mBitmap);
						}
						else {
							mBitmap = loadJpeg(path);
							
						}
					}
				}
				if (mBitmap != null) {
					displayBitmap(mBitmap);
				}
			}
		}).start();
	}
	
    private void resetZoomState() {
        mZoomControl.getZoomState().setPanX(0.5f);
        mZoomControl.getZoomState().setPanY(0.5f);
        mZoomControl.getZoomState().setZoom(1f);
        mZoomControl.getZoomState().notifyObservers();
    }
	
    /**
     * Enum defining listener modes. Before the view is touched the listener is
     * in the UNDEFINED mode. Once touch starts it can enter either one of the
     * other two modes: If the user scrolls over the view the listener will
     * enter PAN mode, if the user lets his finger rest and makes a longpress
     * the listener will enter ZOOM mode.
     */
    private enum Mode {
        UNDEFINED, PAN, ZOOM, PINCH, FULLSCREEN
    }

    /** Time of tactile feedback vibration when entering zoom mode */
    private static final long VIBRATE_TIME = 50;

    /** Current listener mode */
    private Mode mMode = Mode.UNDEFINED;

    /** X-coordinate of previously handled touch event */
    private float mX;

    /** Y-coordinate of previously handled touch event */
    private float mY;

    /** X-coordinate of latest down event */
    private float mDownX;

    /** Y-coordinate of latest down event */
    private float mDownY;

    /** Velocity tracker for touch events */
    private VelocityTracker mVelocityTracker;

    /** Distance touch can wander before we think it's scrolling */
    private int mScaledTouchSlop;

    /** Duration in ms before a press turns into a long press */
    private int mLongPressTimeout;

    /** Vibrator for tactile feedback */
    private Vibrator mVibrator;

    /** Maximum velocity for fling */
    private int mScaledMaximumFlingVelocity;

    float dist0, distCurrent, mCenterx, mCentery;
    
    private View.OnTouchListener mZoomListener = new View.OnTouchListener() {
		
        /**
         * Runnable that enters zoom mode
         */
        private final Runnable mLongPressRunnable = new Runnable() {
            public void run() {
//                mMode = Mode.ZOOM;
//                mVibrator.vibrate(VIBRATE_TIME);
            }
        };

        // implements View.OnTouchListener
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction() & MotionEvent.ACTION_MASK;
            final float x = event.getX();
            final float y = event.getY();
            
            float distx, disty;
            
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mZoomControl.stopFling();
                    v.postDelayed(mLongPressRunnable, mLongPressTimeout);
                    mDownX = x;
                    mDownY = y;
                    mX = x;
                    mY = y;
                    mMode = Mode.FULLSCREEN;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                	   //Get the distance when the second pointer touch
                	mMode = Mode.PINCH;
                    distx = Math.abs(event.getX(0) - event.getX(1));
                    disty = Math.abs(event.getY(0) - event.getY(1));
                    dist0 = FloatMath.sqrt(distx * distx + disty * disty);
                    mCenterx = ((event.getX(0) + event.getX(1)) / 2) / v.getWidth();
                    mCentery = ((event.getY(0) + event.getY(1)) / 2) / v.getHeight();
                    Log.d(TAG, "Pinch mode");
              	break;
                case MotionEvent.ACTION_POINTER_UP:
                	mMode = Mode.UNDEFINED;
                	break;
                case MotionEvent.ACTION_MOVE: {
                    final float dx = (x - mX) / v.getWidth();
                    final float dy = (y - mY) / v.getHeight();

                    if (mMode == Mode.ZOOM) {
                    	//Log.d(TAG, "dy: " + dy + "zoom: " + (float)Math.pow(20, -dy));
                        mZoomControl.zoom((float)Math.pow(20, -dy), mDownX / v.getWidth(), mDownY
                                / v.getHeight());
                    } else if (mMode == Mode.PAN) {
                        mZoomControl.pan(-dx, -dy);
                    } else if (mMode == Mode.PINCH) {
                        //Get the current distance
                        distx = Math.abs(event.getX(0) - event.getX(1));
                        disty = Math.abs(event.getY(0) - event.getY(1));
                        distCurrent = FloatMath.sqrt(distx * distx + disty * disty);
                        float factor = (float)Math.pow(2, -(dist0 - distCurrent)/1000);
                        mZoomControl.zoom(factor, mCenterx, mCentery);
                    } else if (mMode == Mode.FULLSCREEN) {
                    	mMode = Mode.PAN;
                    } else {
                        final float scrollX = mDownX - x;
                        final float scrollY = mDownY - y;

                        final float dist = (float)Math.sqrt(scrollX * scrollX + scrollY * scrollY);

                        if (dist >= mScaledTouchSlop) {
                            v.removeCallbacks(mLongPressRunnable);
                            mMode = Mode.PAN;
                        }
                    }

                    mX = x;
                    mY = y;
                    break;
                }

                case MotionEvent.ACTION_UP:
                	switch (mMode) {
                	case PAN:
                        mVelocityTracker.computeCurrentVelocity(1000, mScaledMaximumFlingVelocity);
                        mZoomControl.startFling(-mVelocityTracker.getXVelocity() / v.getWidth(),
                                -mVelocityTracker.getYVelocity() / v.getHeight());
                		break;
                	case FULLSCREEN:
                		toggleActionBar();
                		break;
                	case PINCH:
                		break;
                		default:
                            mZoomControl.startFling(0, 0);
                			break;
                	}
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    v.removeCallbacks(mLongPressRunnable);
                    mMode = Mode.UNDEFINED;
                    break;

                default:
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    v.removeCallbacks(mLongPressRunnable);
                    mMode = Mode.UNDEFINED;
                    break;

            }

            return true;
        }
	};

	@Override
	protected void serviceConnected(Class<?> serviceClass, ComponentName name,
			ServiceBase service) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void serviceDisconnected(Class<?> serviceClass, ComponentName name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processArduinoButtons(
			EnumSet<ArduinoButtonEnum> pressedButtons,
			EnumSet<ArduinoButtonEnum> releasedButtons) {
		for (ArduinoButtonEnum button : releasedButtons) {
			switch (button) {
			case Button4:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK,0));
				break;
			case Button5:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER,0));
				break;
			case Button6:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT,0));
				break;
			case Button7:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT,0));
				break;
			case Button8:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP,0));
				break;
			case Button9:
				generateKeyEvent(new KeyEvent(button.getLongPressStart(), button.getLongPressEnd(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN,0));
				break;
			}
			Log.d(TAG, "Released button: " + button.toString() + " is long press: " + button.getIsLongPress());
		}
		for (ArduinoButtonEnum button : pressedButtons) {
			switch(button){
			case Button4:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				break;
			case Button5:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
				break;
			case Button6:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
				break;
			case Button7:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
				break;
			case Button8:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
				break;
			case Button9:
				generateKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
				break;
			}
			Log.d(TAG, "Pressed button: " + button.toString());
		}
	}
} 