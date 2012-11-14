package com.dslr.dashboard;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.EnumSet;

import com.dslr.dashboard.ImagePreviewActionProvider.ImageMenuButtonEnum;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageBrowseActivity extends ActivityBase {
	
	private final static String TAG = "ImageBroseActivity";
	
    private GridView mImageGallery;
	private TextView mProgressText;
	private LinearLayout mProgressLayout;
    private ImageGalleryAdapter mImageGalleryAdapter;
    private ArrayList<ImageObjectHelper> mImagesFromPhone;
    private String mSdramSaveLocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DSLR").getAbsolutePath();
	private MenuItem mMenuItem;
	private ImagePreviewActionProvider mMenuProvider;
	private boolean mUseInternalViewer = true;
	
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate"); 
        
        mUseInternalViewer = mPrefs.getBoolean(PtpDevice.PREF_KEY_GENERAL_INTERNAL_VIEWER, true);
        
        setContentView(R.layout.activity_dslr_image_browse);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String tmp = prefs.getString(PtpDevice.PREF_KEY_SDRAM_LOCATION, "");
        if (tmp.equals("")) {
        	Editor editor = prefs.edit();
        	editor.putString(PtpDevice.PREF_KEY_SDRAM_LOCATION, mSdramSaveLocation);
        	editor.commit();
        }
        else
        	mSdramSaveLocation = tmp;
        
        mMenuProvider = new ImagePreviewActionProvider(this);
        mMenuProvider.setDownloadVisible(false);
        mMenuProvider.setImageMenuClickListener(new ImagePreviewActionProvider.ImageMenuClickListener() {
			
			public void onImageButtonClick(ImageMenuButtonEnum btnEnum) {
				switch(btnEnum) {
				case SelectAll:
					for (ImageObjectHelper obj : mImagesFromPhone) {
						obj.isChecked = true;
					}
					mImageGalleryAdapter.notifyDataSetChanged();
					break;
				case Delete:
					deleteSelectedImages();
					break;
				}
			}
		});
        
		mProgressText = (TextView) findViewById(R.id.browsetxtLoading);
		mProgressLayout = (LinearLayout) findViewById(R.id.browseprogresslayout);
        
        mImagesFromPhone = new ArrayList<ImageObjectHelper>();
        
        mImageGalleryAdapter = new ImageGalleryAdapter(this, mImagesFromPhone);

        mImageGallery = (GridView)findViewById(R.id.img_gallery);
        mImageGallery.setAdapter(mImageGalleryAdapter);
        
//        mImageGalleryAdapter.setOnSelectionChanged(new ImageGalleryAdapter.SelectionChangedListener() {
//			
//			public void onSelectionChanged(ArrayList<ImageObjectHelper> selectedItems) {
//			}
//		});
//        mImageGalleryAdapter.setOnImageItemClicked(new ImageGalleryAdapter.ImageItemClickedListener() {
//			
//			public void onImageItemClicked(ImageObjectHelper obj) {
//				if (mMenuProvider.getIsSelectionModeEnabled()) {
//					obj.isChecked = !obj.isChecked;
//					mImageGalleryAdapter.notifyDataSetChanged();
//				}
//				else
//					displayImage(obj);
//			}
//		});
//        mImageGallery.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        mImageGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		Log.d(TAG, "Position: " + position + " id: " + id);
        		ImageObjectHelper helper = (ImageObjectHelper)parent.getItemAtPosition(position);
        		if (helper != null) {
        			if (mMenuProvider.getIsSelectionModeEnabled()) {
        				helper.isChecked = !helper.isChecked;
        				mImageGalleryAdapter.notifyDataSetChanged();
        			}
        			else
    					displayImage(helper);
        		}
        	}

		}); 
        mImageGallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
        		Log.d(TAG, "Long click Position: " + position + " id: " + id);
        		mMenuProvider.setIsSelectionModeEnabled(!mMenuProvider.getIsSelectionModeEnabled());
        		ImageObjectHelper helper = (ImageObjectHelper)parent.getItemAtPosition(position);
        		helper.isChecked = !helper.isChecked;
//        		mImageGallery.setSelected(true);
        		mImageGalleryAdapter.notifyDataSetChanged();
        		return true;
        	}
		});
    }
    
    private void deleteSelectedImages() {
    	mProgressText.setText("Deleting selected images");
    	mProgressLayout.setVisibility(View.VISIBLE);
		for(int i = mImageGalleryAdapter.getCount()-1; i >= 0; i--){
			ImageObjectHelper item = mImageGalleryAdapter.items().get(i);
			if (item.isChecked) {
				item.deleteImage();
				mImageGalleryAdapter.items().remove(i);
			}
		}
    	mProgressLayout.setVisibility(View.GONE);
		mImageGalleryAdapter.notifyDataSetChanged();     	
    }
    private void displayImage(ImageObjectHelper obj) {
		Uri uri = Uri.fromFile(obj.file);
    	if (mUseInternalViewer) {
    		Intent ipIntent = new Intent(this, ImagePreviewActivity.class);
			ipIntent.setAction(Intent.ACTION_VIEW);
			ipIntent.setData(uri);
			this.startActivity(ipIntent);
    	} else {
    		Intent it = new Intent(Intent.ACTION_VIEW);
    		it.setDataAndType(uri, "image/*");
    		startActivity(it);    	
   		}
    }
	private void loadImagesFromPhone(){
		Log.d(TAG, "LoadImagesFromPhone");
		mImagesFromPhone.clear();
		File f = new File(mSdramSaveLocation);
		if (f.exists()){
		File[] phoneFiles = f.listFiles();
		for(int i = 0; i < phoneFiles.length; i++){
			if (phoneFiles[i].isFile()){
				final ImageObjectHelper helper = new ImageObjectHelper();
				helper.file = phoneFiles[i];
				helper.galleryItemType = ImageObjectHelper.PHONE_PICTURE;
				
				if (!tryLoadThumb(helper))
				{
					String fExt = helper.getFileExt(helper.file.toString());
					if (fExt.equals("jpg") || fExt.equals("png")) {
						Bitmap thumb = null;
						final int IMAGE_MAX_SIZE = 30000; // 1.2MP
						
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = true;
						thumb = BitmapFactory.decodeFile(helper.file.getAbsolutePath(), options);
		
				        int scale = 1;
				        while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
				            scale++;
				        }
				        Log.d(TAG, "scale = " + scale + ", orig-width: " + options.outWidth       + ", orig-height: " + options.outHeight);
		
				        if (scale > 1) {
				            scale--;
					        options = new BitmapFactory.Options();
					        options.inSampleSize = scale;
					        thumb = BitmapFactory.decodeFile(helper.file.getAbsolutePath(), options);
				        }
				        else
				        	thumb = BitmapFactory.decodeFile(helper.file.getAbsolutePath());
				        if (thumb != null) {
							FileOutputStream fOut;
							try {
								fOut = new FileOutputStream(helper.getThumbFilePath("jpg"));
								thumb.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
								fOut.flush();
								fOut.close();
								thumb.recycle();
							} catch (Exception e) {
							}				        	
				        }
					}
					else {
						// try jni to create a thumb
						String proba = helper.getThumbFilePath("").getAbsolutePath();
						if (NativeMethods.getInstance().loadRawImageThumb(helper.file.getAbsolutePath(), proba ))
						{
							
						}
					}
				}
				mImagesFromPhone.add(helper);
			}
		}
		}
		Log.d(TAG, "Images from phone - NotifyDataSetChanged");
		mImageGalleryAdapter.notifyDataSetChanged();
	}
	
	private boolean tryLoadThumb(ImageObjectHelper helper){
		boolean rezultat = helper.tryLoadThumb("png");
		if (!rezultat){
			rezultat = helper.tryLoadThumb("jpg");
			if (!rezultat)
				rezultat = helper.tryLoadThumb("ppm");
		}
			
		return rezultat;
	}     
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_image_browse, menu);
        mMenuItem = menu.findItem(R.id.menu_image_browse);
        mMenuItem.setActionProvider(mMenuProvider);
        return true;
    }

	
    @Override
    protected void onRestart() {
    	Log.d(TAG, "onRestart");
    	super.onRestart();
    }
    
    @Override
    protected void onStart() {
    	Log.d(TAG, "onStart");
    	super.onStart();
    }
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "onResume");
    	loadImagesFromPhone();
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause");
    	super.onPause();
    }
    
    @Override
    protected void onStop() {
    	Log.d(TAG, "onStop");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy");
    	super.onDestroy();
    }

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");
		super.onNewIntent(intent);
	}
    
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		Log.d(TAG, "onAttachFragment");
	}

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
			case Button0:
				break;
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
