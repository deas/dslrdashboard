package com.dslr.dashboard;

public interface IDslrActivity {
	public PtpDevice getPtpDevice();
	public void toggleFullScreen();
	public boolean getIsFullScreen();
	public void toggleLvLayout();
	public void toggleLvLayout(boolean showLvLayout);
	public boolean getIsLvLayoutEnabled();
	public void zoomLiveView(boolean up);
}
