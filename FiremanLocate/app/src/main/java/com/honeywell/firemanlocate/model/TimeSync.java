package com.honeywell.firemanlocate.model;

import com.honeywell.firemanlocate.util.TimeUtil;

/**
 * Created by lynnliu on 7/8/15.
 */
public class TimeSync implements IPackage {

    private byte mType = (byte) (DataType.TIME_SYNC.ordinal() + 1);

    private byte mReserved = 0;

    private short mTimeDiffer = 0;

    private int mTimeStamp = 0;

    private short mTimeMilliseconds = 0;

    private Object[] mDataArray = new Object[5];

    public static final int DATA_LENGTH = 10;

    public TimeSync() {
        mDataArray[0] = mType;
        mDataArray[1] = mReserved;
        mDataArray[2] = mTimeDiffer;
        Object[] timeDiffer = TimeUtil.getTimestamp();
        mDataArray[3] = mTimeStamp = (int) timeDiffer[0];
        mDataArray[4] = mTimeMilliseconds = (short) timeDiffer[1];
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

    public short getTimeDiffer() {
        return mTimeDiffer;
    }

    public void setTimeDiffer(short timeDiffer) {
        mTimeDiffer = timeDiffer;
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

    public Object[] getDataArray() {
        return mDataArray;
    }

    public void setDataArray(Object[] mDataArray) {
        this.mDataArray = mDataArray;
    }

    @Override
    public String getPrintableString() {
        String printStr = "\nTime Sync: \n";
        printStr += "Type: " + mType + ", ";
        printStr += "Reserved: " + mReserved + ", ";
        printStr += "TimeDiffer: " + mTimeDiffer + ", ";
        printStr += "TimeStamp: " + mTimeStamp + ", ";
        printStr += "TimeMilliseconds: " + mTimeMilliseconds + "\n";
        return printStr;
    }
}
