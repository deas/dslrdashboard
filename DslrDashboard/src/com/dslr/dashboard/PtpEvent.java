package com.dslr.dashboard;

public class PtpEvent {
    /** EventCode: */
    public static final int Undefined = 0x4000;
    /** EventCode: */
    public static final int CancelTransaction = 0x4001;
    /** EventCode: */
    public static final int ObjectAdded = 0x4002;
    /** EventCode: */
    public static final int ObjectRemoved = 0x4003;
    /** EventCode: */
    public static final int StoreAdded = 0x4004;
    /** EventCode: */
    public static final int StoreRemoved = 0x4005;
    /** EventCode: */
    public static final int DevicePropChanged = 0x4006;
    /** EventCode: */
    public static final int ObjectInfoChanged = 0x4007;
    /** EventCode: */
    public static final int DeviceInfoChanged = 0x4008;
    /** EventCode: */
    public static final int RequestObjectTransfer = 0x4009;
    /** EventCode: */
    public static final int StoreFull = 0x400a;
    /** EventCode: */
    public static final int DeviceReset = 0x400b;
    /** EventCode: */
    public static final int StorageInfoChanged = 0x400c;
    /** EventCode: */
    public static final int CaptureComplete = 0x400d;
    /** EventCode: a status event was dropped (missed an interrupt) */
    public static final int UnreportedStatus = 0x400e;
    /** EventCode: ObjectAddedInSdram */
    public static final int ObjectAddedInSdram = 0xc101;
    /** EventCode: CaptureCompleteRecInSdram */
    public static final int CaptureCompleteRecInSdram = 0xc102;
    /** EventCode: PreviewImageAdded */
    public static final int PreviewImageAdded = 0xc103;
}
