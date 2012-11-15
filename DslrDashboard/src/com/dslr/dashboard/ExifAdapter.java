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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ExifAdapter extends BaseAdapter {

	private ArrayList<ExifDataHelper> _items;
	
	public ArrayList<ExifDataHelper> items(){
		return _items;
	}
    public Context context;
    public LayoutInflater inflater;
	
    public ExifAdapter(Context context, ArrayList<ExifDataHelper> arrayList){
        super();
        
        this.context = context;
        this._items = arrayList;
        
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }    
    
    public void changeItems(ArrayList<ExifDataHelper> arrayList){
    	_items = arrayList;
    	notifyDataSetChanged();
    }
    
	public int getCount() {
        return _items.size();
	}

	public Object getItem(int position) {
        return _items.get(position);
	}

	public long getItemId(int position) {
        return position;
	}

	@Override
	public boolean hasStableIds() {
			return true;
	}
	
	@Override
	public int getItemViewType(int position) {
		return IGNORE_ITEM_VIEW_TYPE;
	}
	@Override
	public int getViewTypeCount() {
		return 1;
	}

    public static class ViewHolder
    {
        TextView txtExifName;
        TextView txtExifValue;
        
    }
	
	public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView==null)
        {
            holder = new ViewHolder();
            
            convertView = inflater.inflate(R.layout.exif_list_item, null);
 
            holder.txtExifName = (TextView) convertView.findViewById(R.id.txtexifdescription);
            holder.txtExifValue = (TextView) convertView.findViewById(R.id.txtexifvalue);
            
            convertView.setTag(holder);
        }
        else
            holder=(ViewHolder)convertView.getTag();
 
        ExifDataHelper helper = _items.get(position);
        holder.txtExifName.setText(helper.mExifDescription);
        holder.txtExifValue.setText(helper.mExifValue);
        
        return convertView;
	}

} 