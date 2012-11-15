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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParserException;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DslrHelper {
	private static final String TAG = "DslrHelper";

	private static DslrHelper mInstance = null;

	public static DslrHelper getInstance() {
		if (mInstance == null)
			mInstance = new DslrHelper();
		return mInstance;
	}

	private DslrHelper() {
	}

	private DslrProperties mDslrProperties;
	private boolean mIsInitialized = false;
	private int mVendorId = 0;
	private int mProductId = 0;
	private PtpDevice mPtpDevice = null;

	public boolean getIsInitialized() {
		return mIsInitialized;
	}

	public PtpDevice getPtpDevice() {
		return mPtpDevice;
	}

	public int getVendorId() {
		return mVendorId;
	}

	public int getProductId() {
		return mProductId;
	}

	public void loadDslrProperties(Context context, PtpDevice ptpDevice,
			int vendorId, int productId) {
		if (context == null || ptpDevice == null)
			return;

		mPtpDevice = ptpDevice;
		mVendorId = vendorId;
		mProductId = productId;

		mDslrProperties = new DslrProperties(vendorId, productId);
		String deviceId = null;
		int propertyTitle = 0;
		String productName = String.format("%04X%04X", vendorId, productId)
				.toLowerCase();
		boolean addItems = true;

		Log.d(TAG, "Loading DSLR properties for: " + productName);

		Resources res = context.getResources();

		XmlResourceParser devices = context.getResources().getXml(
				R.xml.propertyvalues);

		int eventType = -1;

		DslrProperty dslrProperty = null;

		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_DOCUMENT) {
			} else if (eventType == XmlResourceParser.START_TAG) {

				String strName = devices.getName();

				if (strName.equals("device")) {

				} else if (strName.equals("ptpproperty")) {

					int propertyCode = Integer.parseInt(
							devices.getAttributeValue(null, "id"), 16);

					deviceId = devices.getAttributeValue(null, "deviceId");

					addItems = false;
					if (deviceId != null) {
						if (deviceId.toLowerCase().equals(productName)) {
							addItems = true;
						}
					} else {
						addItems = true;
					}
					
					if (addItems) {
						dslrProperty = mDslrProperties
								.addProperty(propertyCode);
						dslrProperty.setPropertyTitle( devices.getAttributeResourceValue(null, "title", 0));
						
					}
					
//					if (deviceId != null
//							&& deviceId.toLowerCase().equals(productName)) {
//						dslrProperty = mDslrProperties
//								.addProperty(propertyCode);
//						addItems = true;
//					} else {
//						dslrProperty = mDslrProperties
//								.getProperty(propertyCode);
//						if (dslrProperty == null) {
//							dslrProperty = mDslrProperties
//									.addProperty(propertyCode);
//							addItems = true;
//						} else
//							addItems = false;
//					}
					

				} else if (strName.equals("item")) {

					if (addItems) {

						int valueId = devices.getAttributeResourceValue(null,
								"value", 0);
						int nameId = devices.getAttributeResourceValue(null,
								"name", 0);
						int resId = devices.getAttributeResourceValue(null,
								"img", 0);

						Object value = null;

						String valueType = res.getResourceTypeName(valueId);
						if (valueType.equals("string")) {
							value = res.getString(valueId);
						} else if (valueType.equals("integer")) {
							value = res.getInteger(valueId);
						}

						dslrProperty.addPropertyValue(value, nameId, resId);
					}

				} 
			}
			try {
				eventType = devices.next();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mIsInitialized = true;
		Log.d(TAG, "Document End");
	}

	public void createDialog(Context context, final int propertyCode,
			String dialogTitle, final ArrayList<PtpPropertyListItem> items,
			int selectedItem, DialogInterface.OnClickListener listener) {
		createDialog(context, propertyCode, dialogTitle, items, 0,
				selectedItem, listener);
	}

	public void createDialog(Context context, final int propertyCode,
			String dialogTitle, final ArrayList<PtpPropertyListItem> items,
			final int offset, int selectedItem,
			DialogInterface.OnClickListener listener) {
		if (!mIsInitialized)
			return;
		DialogInterface.OnClickListener mListener = new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, items.get(which).getValue().getClass().toString());

				Integer value = null;
				Object val = items.get(which).getValue();

				if (val instanceof Long)
					val = (Long) val - offset;
				else if (val instanceof Integer)
					val = (Integer) val - offset;

				if (val != null) {
					// value = value - offset;
					mPtpDevice.setDevicePropValueCmd(propertyCode, val);
				}
			}
		};
		if (listener != null)
			mListener = listener;
		
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
		customBuilder.setTitle(dialogTitle).setListItems(items, selectedItem)
				.setListOnClickListener(mListener);

		CustomDialog dialog = customBuilder.create();
		dialog.show();

	}

	// public void createDialog(FragmentManager fragmentManager, final int
	// propertyCode, String dialogTitle, final ArrayList<PtpPropertyListItem>
	// items, int selectedItem, DialogInterface.OnClickListener listener) {
	// Log.d(TAG, "CreateDialog");
	// if (!mIsInitialized)
	// return;
	// DialogInterface.OnClickListener mListener = new
	// DialogInterface.OnClickListener() {
	//
	// public void onClick(DialogInterface dialog, int which) {
	// mPtpDevice.setDevicePropValueCmd(propertyCode,
	// items.get(which).getValue());
	// }
	// };
	// if (listener != null)
	// mListener = listener;
	// PtpPropertyDialog dialog = new PtpPropertyDialog(items, selectedItem,
	// dialogTitle, mListener);
	//
	// dialog.show(fragmentManager.beginTransaction(), "dialog");
	// }
	public void createDslrDialog(Context context, final int propertyCode,
			String dialogTitle, DialogInterface.OnClickListener listener) {
		createDslrDialog(context, propertyCode, 0, dialogTitle, listener);
	}

	public void createDslrDialog(Context context, final int propertyCode,
			final int offset, String dialogTitle,
			DialogInterface.OnClickListener listener) {
		if (!mIsInitialized)
			return;
		PtpProperty property = mPtpDevice.getPtpProperty(propertyCode);
		if (property == null)
			return;

		DslrProperty dslrProperty = mDslrProperties.getProperty(property
				.getPropertyCode());
		if (dslrProperty == null)
			return;
		ArrayList<PtpPropertyListItem> items = null;
		int selectedItem = -1;
		Vector<?> propValues = property.getEnumeration();
		if (propValues != null) {

			items = new ArrayList<PtpPropertyListItem>();
			selectedItem = propValues.indexOf(property.getValue());

			for (int i = 0; i < propValues.size(); i++) {
				PtpPropertyListItem item = dslrProperty
						.getPropertyByValue(propValues.get(i + offset));
				if (item != null)
					items.add(item);
			}
		} else {
			int vFrom = 0;
			int vTo = 0;
			int vStep = 1;

			PtpProperty.PtpRange range = property.getRange();
			if (range != null) {
				try {
					vFrom = (Integer) range.getMinimum();
					vTo = (Integer) range.getMaximum();
					vStep = (Integer) range.getIncrement();

					items = new ArrayList<PtpPropertyListItem>();

					for (int i = vFrom; i <= vTo; i += vStep) {
						PtpPropertyListItem item = dslrProperty
								.getPropertyByValue(i + offset);
						if (item != null)
							items.add(item);
					}

				} catch (Exception e) {

				}
			} else
				items = dslrProperty.valueNames();
			selectedItem = dslrProperty.indexOfValue(property.getValue());
		}
		String title = dialogTitle;
		if (dslrProperty.propertyTitle() != 0)
			title = (String)context.getText(dslrProperty.propertyTitle());
		createDialog(context, propertyCode, title, items, selectedItem,
				listener);
	}

	// public void createDslrDialog(FragmentManager fragmentManager, final int
	// propertyCode, String dialogTitle, DialogInterface.OnClickListener
	// listener){
	// if (!mIsInitialized)
	// return;
	// PtpProperty property = mPtpDevice.getPtpProperty(propertyCode);
	// if (property == null)
	// return;
	//
	// DslrProperty dslrProperty =
	// mDslrProperties.getProperty(property.getPropertyCode());
	// if (dslrProperty == null)
	// return;
	// ArrayList<PtpPropertyListItem> items = null;
	// int selectedItem = -1;
	// Vector<?> propValues = property.getEnumeration();
	// if (propValues != null){
	//
	// items = new ArrayList<PtpPropertyListItem>();
	// selectedItem = propValues.indexOf(property.getValue());
	//
	//
	// for(int i = 0; i < propValues.size(); i++){
	// PtpPropertyListItem item =
	// dslrProperty.getPropertyByValue(propValues.get(i));
	// if (item != null)
	// items.add(item);
	// }
	// }
	// else {
	// int vFrom = 0;
	// int vTo = 0;
	// int vStep = 1;
	//
	// PtpProperty.PtpRange range = property.getRange();
	// if (range != null) {
	// try {
	// vFrom = (Integer)range.getMinimum();
	// vTo = (Integer)range.getMaximum();
	// vStep = (Integer)range.getIncrement();
	//
	// items = new ArrayList<PtpPropertyListItem>();
	//
	// for(int i = vFrom; i <= vTo; i += vStep){
	// PtpPropertyListItem item = dslrProperty.getPropertyByValue(i);
	// if (item != null)
	// items.add(item);
	// }
	//
	// } catch (Exception e) {
	//
	// }
	// }
	// else
	// items = dslrProperty.valueNames();
	// selectedItem = dslrProperty.indexOfValue(property.getValue());
	// }
	// createDialog(fragmentManager, propertyCode, dialogTitle, items,
	// selectedItem, listener);
	// }

	public void setDslrImg(ImageView view, PtpProperty property) {
		setDslrImg(view, 0, property);
	}

	public void setDslrImg(ImageView view, int offset, PtpProperty property) {
		setDslrImg(view, offset, property, true);
	}

	public void setDslrImg(ImageView view, int propertyCode) {
		setDslrImg(view, 0, propertyCode);
	}

	public void setDslrImg(ImageView view, int offset, int propertyCode) {
		if (mIsInitialized && mPtpDevice != null) {
			setDslrImg(view, offset, mPtpDevice.getPtpProperty(propertyCode),
					true);
		}
	}

	public void setDslrImg(ImageView view, PtpProperty property,
			boolean setEnableStatus) {
		setDslrImg(view, 0, property, setEnableStatus);
	}

	public void setDslrImg(ImageView view, int offset, PtpProperty property,
			boolean setEnableStatus) {
		if (property != null && view != null) {
			DslrProperty prop = mDslrProperties.getProperty(property
					.getPropertyCode());
			if (prop == null)
				return;
			if (setEnableStatus)
				view.setEnabled(property.getIsWritable());
			if (property.getDataType() <= 0x000a) {
				Integer value = (Integer) property.getValue();
				view.setImageResource(prop.getImgResourceId(value + offset));
			} else
				view.setImageResource(prop.getImgResourceId(property.getValue()));
		}
	}

	public void setDslrTxt(TextView view, PtpProperty property) {
		try {
			DslrProperty prop = mDslrProperties.getProperty(property
					.getPropertyCode());
			if (prop == null)
				return;
			view.setEnabled(property.getIsWritable());
			int propRes = prop.getnameResourceId(property.getValue());
			if (propRes != 0)
				view.setText(propRes);
			else
				Log.d(TAG, String.format(
						"setDslrTxt value not found for property: %#x",
						property.getPropertyCode()));
		} catch (Exception e) {
			Log.d(TAG,
					String.format(
							"setDslrTxt exception, property: %#x exception: ",
							property.getPropertyCode())
							+ e.getMessage());
		}

	}

	public void showApertureDialog(Context context) {
		Log.d(TAG, "Show aperture dialog");
		if (!mIsInitialized)
			return;
		final PtpProperty property = mPtpDevice
				.getPtpProperty(PtpProperty.FStop);
		if (property != null) {
			final Vector<?> enums = property.getEnumeration();
			int selectedItem = enums.indexOf(property.getValue());
			if (enums != null) {
				final ArrayList<PtpPropertyListItem> items = new ArrayList<PtpPropertyListItem>();
				for (int i = 0; i < enums.size(); i++) {
					Integer value = (Integer) enums.get(i);
					Double val = ((double) value / 100);
					PtpPropertyListItem item = new PtpPropertyListItem();
					item.setImage(-1);
					item.setTitle(val.toString());
					item.setValue(enums.get(i));
					items.add(item);
				}
				createDialog(context, PtpProperty.FStop, "Select aperture",
						items, selectedItem, null);
			}

		}
	}

	public void showExposureTimeDialog(Context context) {
		if (!mIsInitialized)
			return;
		PtpProperty property = mPtpDevice
				.getPtpProperty(PtpProperty.ExposureTime);
		if (property != null) {
			final Vector<?> enums = property.getEnumeration();
			int selectedItem = enums.indexOf(property.getValue());
			if (enums != null) {
				ArrayList<PtpPropertyListItem> items = new ArrayList<PtpPropertyListItem>();
				for (int i = 0; i < enums.size(); i++) {
					String str;
					Long nesto = (Long) enums.get(i);
					if (nesto == 0xffffffffL)
						str = "Bulb";
					else {
						if (nesto >= 10000)
							str = String.format("%.1f \"",
									(double) nesto / 10000);
						else
							str = String.format("1/%.1f",
									10000 / (double) nesto);
					}
					PtpPropertyListItem item = new PtpPropertyListItem();
					item.setImage(-1);
					item.setTitle(str);
					item.setValue(enums.get(i));
					items.add(item);

				}
				createDialog(context, PtpProperty.ExposureTime,
						"Select exposure time", items, selectedItem, null);
			}

		}
	}

	public void showExposureCompensationDialog(Context context) {
		Log.d(TAG, "Show exposure compensation dialog");
		if (!mIsInitialized)
			return;

		final PtpProperty property = mPtpDevice
				.getPtpProperty(PtpProperty.ExposureBiasCompensation);
		if (property != null) {
			final Vector<?> enums = property.getEnumeration();
			int selectedItem = enums.indexOf(property.getValue());
			if (enums != null) {
				final ArrayList<PtpPropertyListItem> items = new ArrayList<PtpPropertyListItem>();
				for (int i = 0; i < enums.size(); i++) {
					Integer value = (Integer) enums.get(i);
					PtpPropertyListItem item = new PtpPropertyListItem();
					item.setImage(-1);
					item.setTitle(String.format("%+.1f EV",
							(double) value / 1000));
					item.setValue(enums.get(i));
					items.add(item);
				}
				createDialog(context, PtpProperty.ExposureBiasCompensation,
						"Select EV compensation", items, selectedItem, null);
			}

		}
	}

	public void showInternalFlashCompensationDialog(Context context) {
		Log.d(TAG, "Show exposure compensation dialog");
		if (!mIsInitialized)
			return;

		final PtpProperty property = mPtpDevice
				.getPtpProperty(PtpProperty.InternalFlashCompensation);
		if (property != null) {
			PtpProperty.PtpRange range = property.getRange();
			if (range != null) {
				int selectedItem = -1;
				int i = 0;
				ArrayList<PtpPropertyListItem> items = new ArrayList<PtpPropertyListItem>();
				for (int r = (Integer) range.getMinimum(); r <= (Integer) range
						.getMaximum(); r += (Integer) range.getIncrement()) {
					if (r == (Integer) property.getValue())
						selectedItem = i;

					PtpPropertyListItem item = new PtpPropertyListItem();
					item.setImage(-1);
					item.setTitle(String.format("%+.1f EV", (double) r / 6));
					item.setValue(r);
					items.add(item);
					i++;
				}
				createDialog(context, PtpProperty.InternalFlashCompensation,
						"Select Internal Flash Compensation", items,
						selectedItem, null);
			}
		}

	}

	// enable/disable controls in view group
	public void enableDisableControls(ViewGroup viewGroup, boolean enable) {
		enableDisableControls(viewGroup, enable, false);
	}
	public void enableDisableControls(ViewGroup viewGroup, boolean enable, boolean checkOnlyVisible) {
		if (viewGroup != null) {
			boolean change = true;
			if (viewGroup.getTag() != null) {
				Boolean old = (Boolean) viewGroup.getTag();
				if (old == enable)
					change = false;
			}
			if (change) {
				viewGroup.setTag(enable);
				for (int i = 0; i < viewGroup.getChildCount(); i++) {
					boolean checkView = viewGroup.getChildAt(i).getVisibility() == View.VISIBLE;
					if (!checkOnlyVisible)
						checkView = true;
					if (checkView) {
						if (enable) {
							if (viewGroup.getChildAt(i).getTag() != null) {
								viewGroup.getChildAt(i).setEnabled(
										(Boolean) viewGroup.getChildAt(i)
												.getTag());
								viewGroup.getChildAt(i).setTag(null);
							}
						} else {
							viewGroup.getChildAt(i).setTag(
									viewGroup.getChildAt(i).isEnabled());
							viewGroup.getChildAt(i).setEnabled(false);
						}
					}
				}
			}
		}
	}

}
