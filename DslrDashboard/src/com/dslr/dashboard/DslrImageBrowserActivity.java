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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import com.dslr.dashboard.ImagePreviewActionProvider.ImageMenuButtonEnum;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DslrImageBrowserActivity extends ActivityBase {

	private final static String TAG = "DslrImageBroseActivity";

	private DslrHelper mDslrHelper;
	private GridView mImageGallery;
	private TextView mProgressText, mDownloadFile;
	private ProgressBar mDownloadProgress;
	private LinearLayout mProgressLayout, mDownloadProgressLayout;
	private MenuItem mMenuItem;
	private ImagePreviewActionProvider mMenuProvider;

	private ImageGalleryAdapter mImageGalleryAdapter;
	private ArrayList<ImageObjectHelper> mImagesArray;
	private String mSdramSavingLocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"DSLR").getAbsolutePath();
	private boolean mUseInternalViewer = true;
	
	private boolean mIsCameraGalleryEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		
        String tmp = mPrefs.getString(PtpDevice.PREF_KEY_SDRAM_LOCATION, "");
        if (tmp.equals("")) {
        	Editor editor = mPrefs.edit();
        	editor.putString(PtpDevice.PREF_KEY_SDRAM_LOCATION, mSdramSavingLocation);
        	editor.commit();
        }
        else
        	mSdramSavingLocation = tmp;
		
		mUseInternalViewer = mPrefs.getBoolean(PtpDevice.PREF_KEY_GENERAL_INTERNAL_VIEWER, true);
		
		setContentView(R.layout.activity_dslr_image_browse);
		mDslrHelper = DslrHelper.getInstance();

        mMenuProvider = new ImagePreviewActionProvider(this);
        mMenuProvider.setDownloadVisible(true);
        mMenuProvider.setImageMenuClickListener(new ImagePreviewActionProvider.ImageMenuClickListener() {
			
			public void onImageButtonClick(ImageMenuButtonEnum btnEnum) {
				switch(btnEnum) {
				case Download:
					downloadSelectedImages();
					break;
				case SelectAll:
					for (ImageObjectHelper obj : mImagesArray) {
						obj.isChecked = true;
					}
					mImageGalleryAdapter.notifyDataSetChanged();
					break;
				case Delete:
					if (mIsCameraGalleryEnabled)
						deleteSelectedImages();
					else {
						deleteSelectedPhoneImages();
					}
					break;
				case LoadInfos:
					getObjectInfosFromCamera();
					break;
				case PhoneImages:
					switchToPhoneGallery();
					break;
				case CameraImages:
					switchToCameraGallery();
					break;
				}
			}
		});
		
		mProgressText = (TextView) findViewById(R.id.browsetxtLoading);
		mProgressLayout = (LinearLayout) findViewById(R.id.browseprogresslayout);
		mDownloadFile = (TextView)findViewById(R.id.downloadfile);
		mDownloadProgress = (ProgressBar)findViewById(R.id.downloadprogress);
		mDownloadProgressLayout = (LinearLayout)findViewById(R.id.downloadprogresslayout);
		
		mImagesArray = new ArrayList<ImageObjectHelper>();
		mImageGalleryAdapter = new ImageGalleryAdapter(this, mImagesArray);

		mImageGallery = (GridView) findViewById(R.id.img_gallery);
		mImageGallery.setAdapter(mImageGalleryAdapter);

        mImageGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		Log.d(TAG, "Position: " + position + " id: " + id);
        		ImageObjectHelper helper = (ImageObjectHelper)parent.getItemAtPosition(position);
        		if (helper != null) {
        			if (mMenuProvider.getIsSelectionModeEnabled()) {
        				helper.isChecked = !helper.isChecked;
        				mImageGalleryAdapter.notifyDataSetChanged();
        			}
        			else {
    					displayImage(helper);
        			}
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

	private void switchToPhoneGallery() {
		if (mIsCameraGalleryEnabled) {
			mIsCameraGalleryEnabled = false;
			mMenuProvider.setIsCameraGalleryEnabled(mIsCameraGalleryEnabled);
			loadImagesFromPhone();
		}
	}
	private void switchToCameraGallery() {
		if (!mIsCameraGalleryEnabled) {
			if (DslrHelper.getInstance().getPtpDevice() != null) {
				if (DslrHelper.getInstance().getPtpDevice().getIsPtpDeviceInitialized()) {
					mIsCameraGalleryEnabled = true;
					mMenuProvider.setIsCameraGalleryEnabled(mIsCameraGalleryEnabled);
					initDisplay();
				}
			}
		}
	}
	private void hideProgressBar() {
		runOnUiThread(new Runnable() {
			
			public void run() {
				mProgressLayout.setVisibility(View.GONE);
		    	mImageGallery.setEnabled(true);
		    	mMenuItem.setEnabled(true);
		    	mMenuProvider.setIsEnabled(true);
		    	mImageGalleryAdapter.notifyDataSetChanged();
			}
		});
	}
	private void showProgressBar(final String text, final boolean showDownloadProgress) {
		runOnUiThread(new Runnable() {
			
			public void run() {
		    	mProgressText.setText(text);
		    	mProgressLayout.setVisibility(View.VISIBLE);
		    	if (showDownloadProgress) {
		    		mDownloadProgressLayout.setVisibility(View.VISIBLE);
		    		mDownloadProgress.setProgress(0);
		    	}
		    	else
		    		mDownloadProgressLayout.setVisibility(View.GONE);
		    	mImageGallery.setEnabled(false);
		    	mMenuProvider.setIsEnabled(false);
		    	mMenuItem.setEnabled(false);
			}
		});
	}
	private void setProgressFileName(final String fileName, final int fileSize) {
		runOnUiThread(new Runnable() {
			
			public void run() {
				mDownloadFile.setText(fileName);
				mDownloadProgress.setProgress(0);
				mDownloadProgress.setMax(fileSize);
			}
		});
	}
	private void updateProgress(final ImageObjectHelper obj) {
		runOnUiThread(new Runnable() {
			
			public void run() {
				mDownloadProgress.setProgress(obj.progress);
			}
		});
	}
	
	private void downloadSelectedImages() {
    	showProgressBar("Downloading selected images", true);
    	
    	
    	new Thread(new Runnable() {
			
			public void run() {
				for (final ImageObjectHelper obj : mImagesArray) {
					if (obj.isChecked) {
						setProgressFileName(obj.objectInfo.filename, obj.objectInfo.objectCompressedSize);
						
						mDslrHelper.getPtpDevice().getObjectFromCamera(obj, new PtpPartialObjectProccessor.PtpPartialObjectProgressListener() {
							
							public void onProgress(int offset) {
								obj.progress = offset;
								updateProgress(obj);
							}
						});
						createThumb(obj);
						obj.isChecked = false;
					}
				}
				hideProgressBar();
			}
		}).start();
	}
	
	private void deleteSelectedImages() {
    	showProgressBar("Deleting selected images", true);
    	
    	
    	new Thread(new Runnable() {
			
			public void run() {
				for(int i = mImageGalleryAdapter.getCount()-1; i >= 0; i--){
					ImageObjectHelper item = mImageGalleryAdapter.items().get(i);
					if (item.isChecked) {
						setProgressFileName(item.objectInfo.filename, 0);
						mDslrHelper.getPtpDevice().deleteObjectCmd(item);
						mImageGalleryAdapter.items().remove(i);
					}
				}
				
				hideProgressBar();
			}
		}).start();
	}
	private void displayImage(ImageObjectHelper obj) {
		if (mIsCameraGalleryEnabled)
			displayDslrImage(obj);
		else
			displayDownloadedImage(obj);
	}
	
	private void displayDownloadedImage(ImageObjectHelper obj) {
		runOnUiThread(new Runnable() {
			
			public void run() {
				mProgressLayout.setVisibility(View.GONE);
			}
		});
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
	
	private void displayDslrImage(final ImageObjectHelper obj) {
		Log.d(TAG, "sdcard image clicked");
		
		if (mMenuProvider.getDownloadImageEnabled()) {
			File f = mDslrHelper.getPtpDevice().getObjectSaveFile(obj.objectInfo.filename, true);
			if (!f.exists()) {
				showProgressBar("Download image for display", true);
				new Thread(new Runnable() {
				
					public void run() {
						setProgressFileName(obj.objectInfo.filename, obj.objectInfo.objectCompressedSize);
						
						mDslrHelper.getPtpDevice().getObjectFromCamera(obj, new PtpPartialObjectProccessor.PtpPartialObjectProgressListener() {
							
							public void onProgress(int offset) {
								obj.progress = offset;
								updateProgress(obj);
							}
						});
						hideProgressBar();
						createThumb(obj);
						displayDownloadedImage(obj);
					}
				}).start();
			}
			else {
				obj.file = f;
				displayDownloadedImage(obj);
			}
		} else {

			
			byte[] buf = mDslrHelper.getPtpDevice().getLargeThumb(
					obj.objectInfo.objectId);
			if (buf != null) {

				Bitmap bmp = BitmapFactory.decodeByteArray(buf, 12,
						buf.length - 12);
				if (bmp != null) {
					Log.d(TAG, "Display DSLR image");
					Intent dslrIntent = new Intent(this,
							ImagePreviewActivity.class);

					ByteArrayOutputStream bs = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 85, bs);
					dslrIntent.putExtra("data", bs.toByteArray());

					this.startActivity(dslrIntent);
				}
			}
		}

	}

	private void initDisplay() {
		runOnUiThread(new Runnable() {

			public void run() {
				mProgressLayout.setVisibility(View.GONE);
				loadObjectInfosFromCamera();
			}
		});
	}

	
	private void loadObjectInfosFromCamera() {
		mImagesArray.clear();
		for (PtpStorageInfo store : mDslrHelper.getPtpDevice().getPtpStorages().values()) {

			for (PtpObjectInfo obj : store.objects.values()) {
				addObjectFromCamera(obj, false);
			}
		}
		Collections.sort(mImagesArray, new ImageObjectHelperComparator());
		mImageGalleryAdapter.notifyDataSetChanged();
	}

	private class ImageObjectHelperComparator implements
			Comparator<ImageObjectHelper> {

		public int compare(ImageObjectHelper lhs, ImageObjectHelper rhs) {
			return rhs.objectInfo.captureDate
					.compareTo(lhs.objectInfo.captureDate);
		}

	}

	private void addObjectFromCamera(PtpObjectInfo obj, boolean notifyDataSet) {
		boolean addThisObject = true;

		// if slot2 mode is not Sequential recording
		if (mDslrHelper.getPtpDevice().getSlot2Mode() > 0) {
			int storage = obj.storageId >> 16;
			addThisObject = (storage & mDslrHelper.getPtpDevice().getActiveSlot()) == storage; 
			// if this is not the active slot
//			if ((obj.storageId >> 16) != mDslrHelper.getPtpDevice().getActiveSlot())
//				addThisObject = false;
		}

		if (addThisObject) {
			switch (obj.objectFormatCode) {
			case 0x3000:
			case 0x3801:
				ImageObjectHelper imgObj = new ImageObjectHelper();
				imgObj.objectInfo = obj;
				imgObj.galleryItemType = ImageObjectHelper.DSLR_PICTURE;
				imgObj.file = new File(mSdramSavingLocation + "/.dslrthumbs/"
						+ obj.filename + ".jpg");
				mImagesArray.add(imgObj);
				if (notifyDataSet)
					mImageGalleryAdapter.notifyDataSetChanged();
				break;
			}
		}
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

	private void getObjectInfosFromCamera() {
		if (mDslrHelper.getIsInitialized()) {
			if (!mDslrHelper.getPtpDevice().getIsPtpObjectsLoaded()) {
				mProgressLayout.setVisibility(View.VISIBLE);
				new Thread(new Runnable() {

					public void run() {
						mDslrHelper.getPtpDevice().loadObjectInfos();
						initDisplay();
					}
				}).start();
			} else
				initDisplay();
		}
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		if (mIsCameraGalleryEnabled) {
			// we are in camera gallery mode
			if (mDslrHelper.getIsInitialized()) {
				initDisplay();
			}
		}
		else {
			loadImagesFromPhone();
		}
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

	
	private void loadImagesFromPhone(){
		Log.d(TAG, "LoadImagesFromPhone");
		mImagesArray.clear();
		File f = new File(mSdramSavingLocation);
		if (f.exists()){
		File[] phoneFiles = f.listFiles();
		for(int i = 0; i < phoneFiles.length; i++){
			if (phoneFiles[i].isFile()){
				final ImageObjectHelper helper = new ImageObjectHelper();
				helper.file = phoneFiles[i];
				helper.galleryItemType = ImageObjectHelper.PHONE_PICTURE;

				createThumb(helper);
				
				mImagesArray.add(helper);
			}
		}
		}
		Log.d(TAG, "Images from phone - NotifyDataSetChanged");
		mImageGalleryAdapter.notifyDataSetChanged();
	}
	
	private void createThumb(ImageObjectHelper helper) {
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

    private void deleteSelectedPhoneImages() {
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
