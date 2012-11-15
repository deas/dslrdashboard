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

import android.content.Context;
import android.content.DialogInterface;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

public class ImagePreviewActionProvider extends ActionProvider {

	public enum ImageMenuButtonEnum	{
		Download,
		SelectAll,
		Delete,
		LoadInfos
	}
	
	public interface ImageMenuClickListener {
		public void onImageButtonClick(ImageMenuButtonEnum btnEnum);
	}
	private ImageMenuClickListener mImageMenuClickListener;
	
	public void setImageMenuClickListener(ImageMenuClickListener listener) {
		mImageMenuClickListener = listener;
	}
	
	private Context mContext;
	private CheckableImageView mLoadInfos, mDownload, mSelectAll, mDelete;
	
	private boolean mIsEnabled = true;
	private boolean mDownloadVisible = false;
	private boolean mDownloadImageEnabled = false;
	private boolean mSelectionModeEnabled = false;
	
	public boolean getIsEnabled() {
		return mIsEnabled;
	}
	public void setIsEnabled(boolean value) {
		mIsEnabled = value;
	}
	public boolean getDownloadImageEnabled() {
		return mDownloadImageEnabled;
	}
	public void setDownloadImageEnabled(boolean value) {
		mDownloadImageEnabled = value;
	}
	public boolean getDownloadVisible() {
		return mDownloadVisible;
	}
	public void setDownloadVisible(boolean value) {
		mDownloadVisible = value;
		setDownloadVisibility();
	}
	public boolean getIsSelectionModeEnabled() {
		return mSelectionModeEnabled;
	}
	public void setIsSelectionModeEnabled(boolean value) {
		mSelectionModeEnabled = value;
		setSelectionMode();
	}
	
	public ImagePreviewActionProvider(Context context) {
		super(context);
		mContext = context;
	}

	private void setDownloadVisibility() {
		if (mDownload != null) {
			mDownload.setVisibility(mDownloadVisible ? View.VISIBLE : View.GONE);
			mDownload.setChecked(mDownloadImageEnabled);
		}
		if (mLoadInfos != null)
			mLoadInfos.setVisibility(mDownloadVisible ? View.VISIBLE : View.GONE);
	}
	private void setSelectionMode() {
		if (mSelectAll != null)
			mSelectAll.setChecked(mSelectionModeEnabled);
	}
	
	@Override
	public View onCreateActionView() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.image_preview_menu_provider,null);
        
        mLoadInfos = (CheckableImageView)view.findViewById(R.id.load_object_info);
        mDownload = (CheckableImageView)view.findViewById(R.id.download_img);
        setDownloadVisibility();
        mSelectAll = (CheckableImageView)view.findViewById(R.id.select_all);
        setSelectionMode();
        mDelete = (CheckableImageView)view.findViewById(R.id.delete_selected);
        
        
        mDownload.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				if (mIsEnabled) {
					mDownloadImageEnabled = !mDownloadImageEnabled;
					setDownloadVisibility();
				}
				return true;
			}
		});
        mDownload.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mIsEnabled) {
					if (mImageMenuClickListener != null)
						mImageMenuClickListener.onImageButtonClick(ImageMenuButtonEnum.Download);
				}
			}
		});
        mSelectAll.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				if (mIsEnabled) {
					mSelectionModeEnabled = !mSelectionModeEnabled;
					setSelectionMode();
				}
				return true;
			}
		});
        mSelectAll.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mIsEnabled) {
					if (mImageMenuClickListener != null)
						mImageMenuClickListener.onImageButtonClick(ImageMenuButtonEnum.SelectAll);
				}
			}
		});
        mDelete.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mIsEnabled)
					showDeleteDialog();
			}
		});
        mLoadInfos.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mIsEnabled) {
					if (mImageMenuClickListener != null)
						mImageMenuClickListener.onImageButtonClick(ImageMenuButtonEnum.LoadInfos);
				}
			}
		});
		return view;
	}
	
	private void showDeleteDialog() {
    	CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
    	customBuilder.setTitle("Image deletion")
    		.setMessage("Delete selected images)") 
    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
    		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (mImageMenuClickListener != null)
						mImageMenuClickListener.onImageButtonClick(ImageMenuButtonEnum.Delete);
				}
			});
    	CustomDialog dialog = customBuilder.create();
    	dialog.show(); 	}

}
