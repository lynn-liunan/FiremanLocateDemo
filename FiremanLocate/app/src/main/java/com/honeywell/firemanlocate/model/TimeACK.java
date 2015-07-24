package com.honeywell.firemanlocate.model;

import com.honeywell.firemanlocate.util.ByteUtil;

/**
 * Created by lynnliu on 7/8/15.
 */
public class TimeACK implements IPackage {

    private byte mType = (byte) (DataType.TIME_ACK.ordinal() + 1);

    private byte mReserved = 0;

    private short mModuleID = 0;

    private int mTimeStamp = 0;

    private short mTimeMilliseconds = 0;

    public TimeACK(byte[] ackBytes) {
        byte[] mModuleIDBytes = new byte[2];
        byte[] mTimeStampBytes = new byte[2];
        byte[] mTimeMillisecondsBytes = new byte[2];
        System.arraycopy(ackBytes, 2, mModuleIDBytes, 0, 2);
        System.arraycopy(ackBytes, 4, mTimeStampBytes, 0, 4);
        System.arraycopy(ackBytes, 8, mTimeMillisecondsBytes, 0, 2);
        setReserved(ackBytes[1]);
        setModuleID(ByteUtil.bytesToShort(mModuleIDBytes));
        setTimeStamp(ByteUtil.bytesToInt(mTimeStampBytes));
        setTimeMilliseconds(ByteUtil.bytesToShort(mTimeMillisecondsBytes));
    }

    public byte getType() {
        return mType;
    }

    public void setType(byte type) {
        mType = type;
    }

    public byte getReserved() {
        return mReserved;
    }

    public void setReserved(byte reserved) {
        mReserved = reserved;
    }

    public short getModuleID() {
        return mModuleID;
    }

    public void setModuleID(short moduleID) {
        mModuleID = moduleID;
    }

    public int getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        mTimeStamp = timeStamp;
    }

    public short getTimeMilliseconds() {
        return mTimeMilliseconds;
    }

    public void setTimeMilliseconds(short timeMilliseconds) {
        mTimeMilliseconds = timeMilliseconds;
    }

    @Override
    public String getPrintableString() {
        String printStr = "ACK: \n";
        printStr += "Type: " + mType + ", ";
        printStr += "Reserved: " + mReserved + ", ";
        printStr += "ModuleID: " + mModuleID + ", ";
        printStr += "TimeStamp: " + mTimeStamp + ", ";
        printStr += "TimeMilliseconds: " + mTimeMilliseconds + "\n";
        return printStr;
    }
}
