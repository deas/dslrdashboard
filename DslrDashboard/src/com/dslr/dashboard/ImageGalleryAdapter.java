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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageGalleryAdapter extends BaseAdapter {

	private final static String TAG = "ImageGalleryAdapter";

	public interface SelectionChangedListener {
		public void onSelectionChanged(
				ArrayList<ImageObjectHelper> selectedItems);
	}

	public interface ImageItemClickedListener {
		public void onImageItemClicked(ImageObjectHelper obj);
	}

	private SelectionChangedListener _selectionChangedListener;

	public void setOnSelectionChanged(SelectionChangedListener listener) {
		_selectionChangedListener = listener;
	}

	private ImageItemClickedListener _imageItemClickedListener;

	public void setOnImageItemClicked(ImageItemClickedListener listener) {
		_imageItemClickedListener = listener;
	}

	private ArrayList<ImageObjectHelper> _items;

	public ArrayList<ImageObjectHelper> items() {
		return _items;
	}

	public Context context;
	public LayoutInflater inflater;

	public ImageGalleryAdapter(Context context,
			ArrayList<ImageObjectHelper> arrayList) {
		super();

		// Log.d(TAG, "Costructor");

		this.context = context;
		this._items = arrayList;

		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		BitmapManager.INSTANCE.setPlaceholder(BitmapFactory.decodeResource(
				context.getResources(), R.drawable.ic_launcher));

	}

	public void changeItems(ArrayList<ImageObjectHelper> arrayList) {
		// Log.d(TAG, "changeItems");
		_items = arrayList;
		notifyDataSetChanged();
	}

	public void addImgItem(ImageObjectHelper item) {
		// Log.d(TAG, "addImgItem");
		_items.add(item);
		this.notifyDataSetChanged();
	}

	public void selectAll() {
		// Log.d(TAG, "selectAll");
		for (ImageObjectHelper item : _items) {
			item.isChecked = true;
		}
		notifyDataSetChanged();
	}

	public void invert() {
		Log.d(TAG, "invert");
		for (ImageObjectHelper item : _items) {
			item.isChecked = !item.isChecked;
		}
		notifyDataSetChanged();
	}

	public int getCount() {
		// Log.d(TAG, "getCount: " + _items.size());
		return _items.size();
	}

	public Object getItem(int position) {
		// Log.d(TAG, "getItem: " + position);
		return _items.get(position);
	}

	public long getItemId(int position) {
		// Log.d(TAG, "getItemId: " + position);
		return position;
	}

	@Override
	public boolean hasStableIds() {
		// Log.d(TAG, "hasSTableIds");
		return true;
	}

	@Override
	public int getItemViewType(int position) {
		return IGNORE_ITEM_VIEW_TYPE;
	}

	@Override
	public int getViewTypeCount() {
		// Log.d(TAG, "getViewTypeCount");
		return 1;
	}

	public static class ViewHolder {
		CheckableLinearLayout itemLayout;
		ImageView thumbImage;
		TextView imgName;
		//CheckBox checkBox;

	}

	public View getView(int position, View convertView, ViewGroup parent) {

		// Log.d(TAG, "getView");
		final ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();

			convertView = inflater.inflate(R.layout.img_preview_item, null);

			holder.itemLayout = (CheckableLinearLayout) convertView.findViewById(R.id.img_item_layout);
			holder.imgName = (TextView) convertView.findViewById(R.id.imgName);
			holder.thumbImage = (ImageView) convertView.findViewById(R.id.thumbImage);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		ImageObjectHelper helper = _items.get(position);

		
		holder.thumbImage.setId(position);
		
		holder.itemLayout.setChecked(helper.isChecked);
		switch (helper.galleryItemType) {
		case ImageObjectHelper.DSLR_PICTURE:
			holder.imgName.setText(helper.objectInfo.filename);

			break;

		case ImageObjectHelper.PHONE_PICTURE:
			holder.imgName.setText(helper.file.getName());

			break;
		}
		BitmapManager.INSTANCE.loadBitmap(helper.file.getAbsolutePath(),
				holder.thumbImage);

		return convertView;
	}

}