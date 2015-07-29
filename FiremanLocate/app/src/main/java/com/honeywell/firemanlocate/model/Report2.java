package com.honeywell.firemanlocate.model;

import com.honeywell.firemanlocate.util.ByteUtil;

/**
 * Created by Vincent on 29/7/15.
 */
public class Report2 implements IPackage {

    private static final int BLOCK_LENGTH = 16;

    private boolean isReportValid = false;

    private byte mType = (byte) (DataType.REPORT.ordinal() + 1);

    private byte mReportsNumber = 0;

    //block 属性

    private short mSequenceNumber = 0;

    private int mTimeStamp = 0;

    private short mTimeMilliseconds = 0;

    private short mModuleAID = 0;

    private short mModuleBID = 0;

    private short mDistance = 0;

    private byte mBattery = 0;

    private byte mReserved = 0;


    public Report2(byte[] reportBytes) {
        if (reportBytes.length <= 2)
            return;
        isReportValid = true;
        mReportsNumber = reportBytes[1];
        byte[] mSequenceNumberBytes = new byte[2];
        byte[] mTimeStampBytes = new byte[4];
        byte[] mTimeMillisecondsBytes = new byte[2];
        byte[] mModuleAIDBytes = new byte[2];
        byte[] mModuleBIDBytes = new byte[2];
        byte[] mDistanceBytes = new byte[2];
        System.arraycopy(reportBytes, 2, mSequenceNumberBytes, 0, 2);
        System.arraycopy(reportBytes, 4, mTimeStampBytes, 0, 4);
        System.arraycopy(reportBytes, 8, mTimeMillisecondsBytes, 0, 2);
        System.arraycopy(reportBytes, 10, mModuleAIDBytes, 0, 2);
        System.arraycopy(reportBytes, 12, mModuleBIDBytes, 0, 2);
        System.arraycopy(reportBytes, 14, mDistanceBytes, 0, 2);
        setSequenceNumber(ByteUtil.bytesToShort(mSequenceNumberBytes));
        setTimeStamp(ByteUtil.bytesToInt(mTimeStampBytes));
        setTimeMilliseconds(ByteUtil.bytesToShort(mTimeMillisecondsBytes));
        setModuleAID(ByteUtil.bytesToShort(mModuleAIDBytes));
        setModuleBID(ByteUtil.bytesToShort(mModuleBIDBytes));
        setDistance(ByteUtil.bytesToShort(mDistanceBytes));
        setBattery(reportBytes[16]);
        setReserved(reportBytes[17]);
    }

    public boolean isReportValid() {
        return isReportValid;
    }

    public void setIsReportValid(boolean isReportValid) {
        this.isReportValid = isReportValid;
    }

    public byte getType() {
        return mType;
    }

    public void setType(byte type) {
        mType = type;
    }

    public byte getReportsNumber() {
        return mReportsNumber;
    }

    public void setReportsNumber(byte reportsNumber) {
        mReportsNumber = reportsNumber;
    }

    public short getSequenceNumber() {
        return mSequenceNumber;
    }

    public void setSequenceNumber(short sequenceNumber) {
        mSequenceNumber = sequenceNumber;
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

    public short getModuleAID() {
        return mModuleAID;
    }

    public void setModuleAID(short moduleAID) {
        mModuleAID = moduleAID;
    }

    public short getModuleBID() {
        return mModuleBID;
    }

    public void setModuleBID(short moduleBID) {
        mModuleBID = moduleBID;
    }

    public short getDistance() {
        return mDistance;
    }

    public void setDistance(short distance) {
        mDistance = distance;
    }

    public byte getBattery() {
        return mBattery;
    }

    public void setBattery(byte battery) {
        mBattery = battery;
    }

    public byte getReserved() {
        return mReserved;
    }

    public void setReserved(byte reserved) {
        mReserved = reserved;
    }



    public String getPrintableString() {
        String printStr = "DataBlock: \n";
        printStr += "SequenceNumber: " + mSequenceNumber + ", ";
        printStr += "TimeStamp: " + mTimeStamp + ", ";
        printStr += "TimeMilliseconds: " + mTimeMilliseconds + ", ";
        printStr += "ModuleAID: " + mModuleAID + ", ";
        printStr += "ModuleBID: " + mModuleBID + ", ";
        printStr += "Distance: " + mDistance + ", ";
        printStr += "Battery: " + mBattery + ", ";
        printStr += "Reserved: " + mReserved + "\n";
        return printStr;
    }

}
