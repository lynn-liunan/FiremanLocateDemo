package com.honeywell.firemanlocate.model;

import com.honeywell.firemanlocate.util.ByteUtil;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lynnliu on 7/8/15.
 */
public class Report implements IPackage,Serializable {

    private static final int BLOCK_LENGTH = 16;

    private boolean isReportValid = false;

    private byte mType = (byte) (DataType.REPORT.ordinal() + 1);

    private byte mReportsNumber = 0;

    private DataBlock[] mDataBlocks;

//    private Map mDistanceMap = new HashMap<>(); //存位置关系和距离

//    private Map mDistanceSubMap; //存列
//
//    private List<Map> mDistanceArray; //map类型的矩阵
//
//    private List<Map> mDistanceSubArray; //子maplist

    public Report(byte[] reportBytes) {
        if (reportBytes.length <= 2)
            return;
        isReportValid = true;
        mReportsNumber = reportBytes[1];
        mDataBlocks = new DataBlock[mReportsNumber];
        for (int i = 0; i < mReportsNumber; i++) {
            byte[] block = new byte[BLOCK_LENGTH];
            System.arraycopy(reportBytes, i * BLOCK_LENGTH + 2, block, 0, 16);
            DataBlock dataBlock = new DataBlock(block);
            mDataBlocks[i] = dataBlock;
//            if (!mDistanceMap.containsKey(dataBlock.getModuleAID())) {
//
//                Map subDistanceMap = new HashMap();
//                subDistanceMap.put(dataBlock.getModuleAID(),0);
//                subDistanceMap.put(dataBlock.getModuleBID(), dataBlock.getDistance());
//                MatrixUtil.mapSort(subDistanceMap); //内部Map排序
//                mDistanceMap.put(dataBlock.getModuleAID(), subDistanceMap);
//                MatrixUtil.mapSort(mDistanceMap);   //最外部Map排序
//            } else {
//                Map subDistance = (Map) mDistanceMap.get(dataBlock.getModuleAID());
//                subDistance.put(dataBlock.getModuleBID(), dataBlock.getDistance());
//                MatrixUtil.mapSort(subDistance);
//            }
        }
    }

//    public Map getmDistanceMap() {
//        return mDistanceMap;
//    }
//
//    public void setmDistanceMap(Map mDistanceMap) {
//        this.mDistanceMap = mDistanceMap;
//    }

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

    public DataBlock[] getDataBlocks() {
        return mDataBlocks;
    }

    public void setDataBlocks(DataBlock[] dataBlocks) {
        mDataBlocks = dataBlocks;
    }

    @Override
    public String getPrintableString() {
        String printStr = "\nReports: \n";
        printStr += "Type: " + mType + ", ";
        printStr += "ReportsNumber: " + mReportsNumber + "\n";
        if (isReportValid) {
            for (int i = 0; i < mReportsNumber; i++) {
                printStr += mDataBlocks[i].getPrintableString();
            }
        }
        return printStr;
    }

    public class DataBlock implements IPackage ,Serializable{

        private short mSequenceNumber = 0;

        private int mTimeStamp = 0;

        private short mTimeMilliseconds = 0;

        private int mModuleAID = 0;

        private int mModuleBID = 0;

        private int mDistance = 0;

        private byte mBattery = 0;

        private byte mReserved = 0;

        public DataBlock(byte[] block) {
            byte[] mSequenceNumberBytes = new byte[2];
            byte[] mTimeStampBytes = new byte[4];
            byte[] mTimeMillisecondsBytes = new byte[2];
            byte[] mModuleAIDBytes = new byte[2];
            byte[] mModuleBIDBytes = new byte[2];
            byte[] mDistanceBytes = new byte[2];
            System.arraycopy(block, 0, mSequenceNumberBytes, 0, 2);
            System.arraycopy(block, 2, mTimeStampBytes, 0, 4);
            System.arraycopy(block, 6, mTimeMillisecondsBytes, 0, 2);
            System.arraycopy(block, 8, mModuleAIDBytes, 0, 2);
            System.arraycopy(block, 10, mModuleBIDBytes, 0, 2);
            System.arraycopy(block, 12, mDistanceBytes, 0, 2);
            setSequenceNumber(ByteUtil.bytesToShort(mSequenceNumberBytes));
            setTimeStamp(ByteUtil.bytesToInt(mTimeStampBytes));
            setTimeMilliseconds(ByteUtil.bytesToShort(mTimeMillisecondsBytes));
            setModuleAID(ByteUtil.bytesToShort(mModuleAIDBytes));
            setModuleBID(ByteUtil.bytesToShort(mModuleBIDBytes));
            setDistance(ByteUtil.bytesToShort(mDistanceBytes));
            setBattery(block[14]);
            setReserved(block[15]);
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

        public int getModuleAID() {
            return mModuleAID;
        }

        public void setModuleAID(short moduleAID) {
            mModuleAID = moduleAID;
        }

        public int getModuleBID() {
            return mModuleBID;
        }

        public void setModuleBID(short moduleBID) {
            mModuleBID = moduleBID;
        }

        public int getDistance() {
            return mDistance;
        }

        public void setDistance(int distance) {
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

        @Override
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
}
