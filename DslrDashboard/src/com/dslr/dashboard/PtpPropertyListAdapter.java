package com.dslr.dashboard;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PtpPropertyListAdapter extends BaseAdapter {

	private ArrayList<PtpPropertyListItem> mItemList;
    private Context mContext;
    private LayoutInflater mInflater;
    private int mSelectedItem;
 
    public PtpPropertyListAdapter(Context context, ArrayList<PtpPropertyListItem> itemList, int selectedItem){
        super();
        
        mSelectedItem = selectedItem;
        mContext = context;
        mItemList = itemList;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
  	
	public int getCount() {
        return mItemList.size(); 	}

	public Object getItem(int position) {
        return mItemList.get(position); 	}

	public long getItemId(int position) {
		return 0;
	}

    public static class ViewHolder
    {
        ImageView mImageViewLogo;
        TextView mTextViewTitle;
    } 
    
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null)
        {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.property_list_item, null);
 
            holder.mImageViewLogo = (ImageView) convertView.findViewById(R.id.imgViewLogo);
            holder.mTextViewTitle = (TextView) convertView.findViewById(R.id.txtViewTitle);
 
            convertView.setTag(holder);
        }
        else
            holder=(ViewHolder)convertView.getTag();
 
        PtpPropertyListItem listItem = (PtpPropertyListItem) mItemList.get(position);
        
       
        if (listItem.getImage() > 0) {
        	holder.mImageViewLogo.setImageResource(listItem.getImage());
        	holder.mImageViewLogo.setVisibility(View.VISIBLE);
        }
        else {
        	holder.mImageViewLogo.setImageDrawable(null);
        	holder.mImageViewLogo.setVisibility(View.GONE);
        }
        
        if (listItem.getNameId() > 0)
        	holder.mTextViewTitle.setText(listItem.getNameId());
        else
        	holder.mTextViewTitle.setText(listItem.getTitle());
 
        return convertView; 	}

}
