package com.honeywell.firemanlocate.util;

import android.util.Log;

import com.honeywell.firemanlocate.model.FiremanPosition;
import com.honeywell.firemanlocate.model.IPackage;
import com.honeywell.firemanlocate.model.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Jama.Matrix;


/**
 * Created by Vincent on 28/7/15.
 */
public class MatrixUtil2 {
    private double[][] mDistanceArray;
    private ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();
    //2 通过上一次坐标点计算旋转坐标原点 返回旋转角度
    double[] rk = new double[3];

    //遍历reportList，封装成distanceMapduixiang
    public static Map parseList(List<Report> mReportList, Map<Integer, TreeMap> mDistanceMap) {
        HashSet<Integer> keySet = new HashSet<>();
        for (int j = 0; j < mReportList.size(); j++) {
            IPackage iPackage = mReportList.get(j);
            for (int i = 0; i < ((Report) iPackage).getDataBlocks().length; i++) {
                keySet.add(((Report) iPackage).getDataBlocks()[i].getModuleAID());
                keySet.add(((Report) iPackage).getDataBlocks()[i].getModuleBID());
                if (!mDistanceMap.containsKey(((Report) iPackage).getDataBlocks()[i].getModuleAID())) {
                    TreeMap subDistanceMap = new TreeMap();
                    subDistanceMap.put(((Report) iPackage).getDataBlocks()[i].getModuleAID(), 0f);
                    subDistanceMap.put(((Report) iPackage).getDataBlocks()[i].getModuleBID(), (((Report) iPackage).getDataBlocks()[i].getDistance()) / 100);
                    mDistanceMap.put(((Report) iPackage).getDataBlocks()[i].getModuleAID(), subDistanceMap);
                    Log.i("roataTheta", "" + ((Report) iPackage).getDataBlocks()[i].getModuleAID() + "to" + ((Report) iPackage).getDataBlocks()[i].getModuleBID() + ": " + (((Report) iPackage).getDataBlocks()[i].getDistance()));
                } else {
                    Map subDistance = (Map) mDistanceMap.get(((Report) iPackage).getDataBlocks()[i].getModuleAID());
                    subDistance.put(((Report) iPackage).getDataBlocks()[i].getModuleBID(), (((Report) iPackage).getDataBlocks()[i].getDistance()) / 100);
                    Log.i("roataTheta", "" + ((Report) iPackage).getDataBlocks()[i].getModuleAID() + "to" + ((Report) iPackage).getDataBlocks()[i].getModuleBID() + ": " + (((Report) iPackage).getDataBlocks()[i].getDistance()));
                }
            }
        }
        return addZero(keySet, mDistanceMap);
    }

    //补充0
    private static Map addZero(HashSet keySet, Map<Integer, TreeMap> mDistanceMap) {
        Object[] mainKeyArray = mDistanceMap.keySet().toArray();
        Object[] keySetArray = keySet.toArray();
        //补充已有行对应的列
        for (TreeMap value : mDistanceMap.values()) {
            for (int i = 0; i < keySetArray.length; i++) {
                if (!value.containsKey(keySetArray[i])) {
                    value.put(keySetArray[i], 0f);
                }
            }
        }
        //补充行
        for (int i = 0; i < keySetArray.length; i++) {
            if (!mDistanceMap.containsKey(keySetArray[i])) {
                TreeMap lineMap = new TreeMap();
                for (int j = 0; j < keySetArray.length; j++) {
                    lineMap.put(keySetArray[j], 0f);
                }
                mDistanceMap.put((Integer) keySetArray[i], lineMap);
            }
        }
        for (int i = 0; i < mDistanceMap.size(); i++) {
            for (int j = 0; j < mDistanceMap.size(); j++) {
//                Log.i("roataTheta", "new " + keySetArray[i] + "to" + keySetArray[j] + ": " + mDistanceMap.get(keySetArray[i]).get(keySetArray[j]));
            }
        }
        //处理是0的点
        for (int i = 0; i < keySetArray.length; i++) {
            TreeMap treeMap = mDistanceMap.get(keySetArray[i]);
            for (int j = 0; j < keySetArray.length; j++) {
                float d1 = (Float) treeMap.get(keySetArray[j]);
                TreeMap treeMap2 = mDistanceMap.get(keySetArray[j]);
                float d2 = (Float) treeMap2.get(keySetArray[i]);
                if (d1 != 0 && d2 != 0) {
                    float d = (d1 + d2) / 2;
                    treeMap.put(keySetArray[j], d);
                    treeMap2.put(keySetArray[i], d);
                } else {
                    float d = d1 + d2;
                    treeMap.put(keySetArray[j], d);
                    treeMap2.put(keySetArray[i], d);
                }
            }
        }
        return mDistanceMap;

    }

    public static ArrayList<FiremanPosition> calculatePointsPosition(Map mDistanceMap, ArrayList<FiremanPosition> mLastFiremanPositionArrayList, Map<String, TreeMap<Integer, ArrayList>> mKalmanMap) {
        double[][] pref = new double[3][3];
        double roataTheta = 0.0;
        double[] rk = new double[3];
        ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();
        if (mDistanceMap == null) {
            return null;
        }
        double[][] arix = new double[mDistanceMap.size()][3];
        int[] indexResult = calculateMaxDistant2(mDistanceMap);
        Log.i("Vincent", "ref1:" + indexResult[0]);
        Log.i("Vincent", "ref2:" + indexResult[1]);
        Log.i("Vincent", "ref3:" + indexResult[2]);
        if (mLastFiremanPositionArrayList != null) {
            roataTheta = calculateRotateAngle(mLastFiremanPositionArrayList, indexResult);      //计算旋转角度 如果有历史数据
            Log.i("roataTheta", "roataTheta: " + roataTheta);
            Log.i("Vincent", "roataTheta: " + roataTheta);
        }
        pref = calculateThreePoint2(mDistanceMap, indexResult, pref, mLastFiremanPositionArrayList, roataTheta); //确定前三个点坐标
        arix = calculateOthersPointFrom(arix, mDistanceMap, indexResult, pref, rk); //计算其他点坐标
        arix = transferAxis(arix, roataTheta);  //最后一次旋转，算出旋转坐标
        //将坐标换为对象
        Object[] keyset = mDistanceMap.keySet().toArray();
        //add by Vincent
//        double cmdX = arix[2][0];
//        double cmdY = arix[2][1];
        //end
        for (int i = 0; i < arix.length; i++) {
            Log.i("arix :", "arix" + i + " x: " + arix[i][0]);
            Log.i("arix :", "arix" + i + " y: " + arix[i][1]);
            Log.i("arix :", "arix" + i + " z: " + arix[i][2]);
            String title = String.format("%x", (((Integer) keyset[i]) & 0xF));
            Log.i("2232323", title);
            if ("2".equals(title)) {
                title = "Exit1";
            } else if ("3".equals(title)) {
                title = "Exit2";
            } else if ("4".equals(title)) {
                title = "Cmd";
            }
            mFiremanPositionArrayList.add(new FiremanPosition(arix[i][0], arix[i][1] , arix[i][2], title));
        }
        for (int i = 0; i < mFiremanPositionArrayList.size(); i++) {
            Log.i("roataTheta", "x" + i + mFiremanPositionArrayList.get(i).getX());
            Log.i("roataTheta", "y" + i + mFiremanPositionArrayList.get(i).getY());
        }
        return mFiremanPositionArrayList;
    }

    private static double average(ArrayList<Integer> list) {
        double sumDistance = 0.0;
        for (int i = 0; i < list.size(); i++) {
            sumDistance += list.get(i);
        }
        return sumDistance / list.size();
    }

    //保存历史firemanPosition对象
    //深度copy 保存历史数据
    public static ArrayList<FiremanPosition> saveFiremanPositionHistory(ArrayList<FiremanPosition> firemanPositionArray) {
        ArrayList<FiremanPosition> lastFiremanPositionArrayList = new ArrayList<FiremanPosition>();
        Iterator<FiremanPosition> iterator = firemanPositionArray.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            FiremanPosition fp = iterator.next().clone();
            lastFiremanPositionArrayList.add(fp);
            double x = fp.getX();
            double y = fp.getY();
            double z = fp.getZ();
            Log.i("Vincent", "LastPosition" + i + " x; " + x + " y: " + y + " z: " + z);
            i++;
        }
        return lastFiremanPositionArrayList;
    }

    //矩阵转置
    public static double[][] exchange(double[][] arrayA, double[][] arrayB) {
        for (int i = 0; i < arrayA.length; i++) {
            for (int j = 0; j < arrayA[i].length; j++) {
                arrayB[j][i] = arrayA[i][j];
            }
        }
        return arrayB;
    }

    //坐标轴旋转
    public static double[][] transferAxis(double[][] pref, double roataTheta) {
        double[][] T = {{Math.cos(roataTheta), -Math.sin(roataTheta), 0},
                {Math.sin(roataTheta), Math.cos(roataTheta), 0}, {0, 0, 1}};
        double[][] p_rotate = new double[pref.length][pref[0].length];
        double[][] pref_transfer = new double[pref[0].length][pref.length];
        pref_transfer = MatrixUtil2.exchange(pref, pref_transfer);
        for (int i = 0; i < pref.length; i++) {
            p_rotate[i][0] = T[0][0] * pref_transfer[0][i] + T[0][1] * pref_transfer[1][i] + T[0][2] * pref_transfer[2][i];
            p_rotate[i][1] = T[1][0] * pref_transfer[0][i] + T[1][1] * pref_transfer[1][i] + T[1][2] * pref_transfer[2][i];
            p_rotate[i][2] = T[2][0] * pref_transfer[0][i] + T[2][1] * pref_transfer[1][i] + T[2][2] * pref_transfer[2][i];
        }
        return p_rotate;
    }

    //1 计算最大和第三个点下标
    public static int[] calculateMaxDistant(double[][] distanceArray) {
        double max_data1 = 0.0;
        double max_data2 = 0.0;
        int max_index = 0;
        int third_index = 0;
        double theta = 0.0;
        for (int i = 0; i < distanceArray[0].length; i++) {
            if (distanceArray[0][i] > max_data1) {
                max_data1 = distanceArray[0][i];
                max_index = i;
            }
        }
        for (int i = 0; i < distanceArray[0].length; i++) {
            theta = Math.acos((Math.pow(distanceArray[0][i], 2) + Math.pow(distanceArray[max_index][i], 2) - Math.pow(distanceArray[0][max_index], 2))
                    / (2 * distanceArray[0][i] * distanceArray[max_index][i]));
            if (distanceArray[0][i] != max_data1 && (distanceArray[0][i] + distanceArray[max_index][i] > max_data2)
                    && theta >= 0.52 && theta <= 2.09) {
                max_data2 = distanceArray[0][i] + distanceArray[max_index][i];
                third_index = i;
            }
        }
        if (third_index == 0) {
            for (int i = 0; i < distanceArray[0].length; i++) {
                if (distanceArray[0][i] != max_data1 && (distanceArray[0][i] + distanceArray[max_index][i] > max_data2)
                        ) {
                    max_data2 = distanceArray[0][i] + distanceArray[max_index][i];
                    third_index = i;
                }
            }
        }
        return new int[]{0, max_index, third_index};
    }

    public static int[] calculateMaxDistant2(Map distanceMap) {
        double max_data1 = 0.0;
        double max_data2 = 0.0;
        int max_index = 1;
        int third_index = 0;
        Object[] key = distanceMap.keySet().toArray();
        Object[] firstLineInnerKey = ((Map) distanceMap.get(key[0])).keySet().toArray();  //第一行key
        double theta = 0.0;
//        for (int i = 0; i < ((Map) distanceMap.get(key[0])).size(); i++) {
//            if (((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i])) > max_data1) {
//                max_data1 = ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i]));
//                max_index = i;
//            }
//        }
//        if (key.length <= max_index) {
//            max_index = key.length - 1;
//        }
        Object[] maxLineInnerKey = ((Map) distanceMap.get(key[max_index])).keySet().toArray(); //最大行key
        Float d3 = ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[max_index]));
        for (int i = 0; i < ((Map) distanceMap.get(key[0])).size(); i++) {
            Float d1 = ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i]));
            Float d2 = 0f;
            if (i < ((Map) distanceMap.get(key[max_index])).size()) {
                d2 = ((Float) ((Map) distanceMap.get(key[max_index])).get(maxLineInnerKey[i]));
            }
            theta = Math.acos(
                    (
                            Math.pow(d1 != null ? d1 : 0, 2)
                                    + Math.pow(d2 != null ? d2 : 0, 2)
                                    - Math.pow(d3 != null ? d3 : 0, 2)
                    )
                            / (2 *
                            (d1 != null ? d1 : 0)
                            * (d2 != null ? d2 : 0)
                    )
            );
            Log.i("roataTheta", "xiaobiaojiaodu: " + theta);
            if (d1 != max_data1 && (d1
                    + d2 > max_data2)
                    && theta >= 0.52 && theta <= 2.09) {
                max_data2 = d1 + d2;
                third_index = i;
            }
        }
        if (third_index == 0) {
            for (int i = 0; i < ((Map) distanceMap.get(key[0])).size(); i++) {
                Float d1 = ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i]));
                Float d2 = 0.0f;
                if (i < ((Map) distanceMap.get(key[max_index])).size()) {
                    d2 = ((Float) ((Map) distanceMap.get(key[max_index])).get(maxLineInnerKey[i]));
                }
                if (d1 != max_data1 && (d1
                        + d2 > max_data2)
                        ) {
                    max_data2 = d1 + d2;
                    third_index = i;
                }
            }
        }
        return new int[]{0, max_index, third_index};
    }

    //通过下标计算第三个点坐标
    public static double[][] calculate3rdPointFrom2(double[][] pref, double distance12,
                                                    double round1, double round2, ArrayList<FiremanPosition> mLastFiremanPositionArrayList,
                                                    double roataTheta, int[] indexResult) {
        double rangle = 0.01;
        while (distance12 > round1 + round2) {
            //  distance12 = distance12 - 0.2;
            round1 += rangle;
            round2 += rangle;
        }
        while (distance12 < Math.abs(round1 - round2)) {
            if (round1 > round2) {
                round1 -= rangle;
                round2 += rangle;
            } else {
                round1 += rangle;
                round2 -= rangle;
            }
        }
        if (distance12 <= (round1 + round2)) {
            double theta1 = Math.acos((Math.pow(round1, 2) + Math.pow(distance12, 2) - Math.pow(round2, 2)) / (2 *
                    round1 * distance12));

            double theta2 = Math.atan((
                    (pref[1][1] - pref[0][1])
            ) / (pref[1][0]
                    - pref[0][0]));
            pref = chooseFiremanPosition3(pref, round1, theta1, theta2, mLastFiremanPositionArrayList, roataTheta, indexResult);

        } else {

        }
        return pref;
    }

    //选取第三个坐标点
    public static double[][] chooseFiremanPosition3(double[][] pref, double round1,
                                                    double theta1, double theta2, ArrayList<FiremanPosition> mLastFiremanPositionArrayList,
                                                    double roataTheta, int[] indexResult) {
        double[] temp_pref1 = {pref[0][0] + round1 * Math.cos(theta1 + theta2), pref[0][1] + round1 * Math.sin(theta1 + theta2), 0};
        pref[2][0] = pref[0][0] + round1 * Math.cos(theta1 + theta2);
        pref[2][1] = pref[0][1] + round1 * Math.sin(theta1 + theta2);
        pref[2][2] = 0.0;

        if (mLastFiremanPositionArrayList == null || mLastFiremanPositionArrayList.size() < 3) {
            pref[2][0] = pref[0][0] + round1 * Math.cos(theta2 - theta1);
            pref[2][1] = pref[0][1] + round1 * Math.sin(theta2 - theta1);
            pref[2][2] = 0;
            Log.i("roataTheta", "第三个坐标点x：" + pref[2][0]);
            Log.i("roataTheta", "第三个坐标点y：" + pref[2][1]);
            Log.i("roataTheta", "第三个坐标点z：" + pref[2][2]);
            return pref;
        }
        double[][] error_reftemp1 = MatrixUtil2.transferAxis(pref, roataTheta);
        //Log.i("Vincent","error_reftemp1 x3: "+error_reftemp1[2][0]);
        //Log.i("Vincent", "error_reftemp1 y3: " + error_reftemp1[2][1]);
        Log.i("Vincent", "last x3: " + mLastFiremanPositionArrayList.get(2).getX());
        Log.i("Vincent", "last y3: " + mLastFiremanPositionArrayList.get(2).getY());
        double distance1 = Math.sqrt(
                Math.pow(error_reftemp1[2][0] - mLastFiremanPositionArrayList.get(indexResult[2]).getX(), 2) +
                        Math.pow(error_reftemp1[2][1] - mLastFiremanPositionArrayList.get(indexResult[2]).getY(), 2)
        );
        pref[2][0] = pref[0][0] + round1 * Math.cos(theta2 - theta1);
        pref[2][1] = pref[0][1] + round1 * Math.sin(theta2 - theta1);
        pref[2][2] = 0;
        double[] temp_pref2 = {pref[0][0] + round1 * Math.cos(theta2 - theta1), pref[0][1] + round1 * Math.sin(theta2 - theta1), 0};
        double[][] error_reftemp2 = MatrixUtil2.transferAxis(pref, roataTheta);
//        Log.i("Vincent","error_reftemp2 x3: "+error_reftemp2[2][0]);
//        Log.i("Vincent", "error_reftemp2 y3: " + error_reftemp2[2][1]);
        Log.i("Vincent", "last x3: " + mLastFiremanPositionArrayList.get(2).getX());
        Log.i("Vincent", "last y3: " + mLastFiremanPositionArrayList.get(2).getY());
        double distance2 = Math.sqrt(
                Math.pow(error_reftemp2[2][0] - mLastFiremanPositionArrayList.get(indexResult[2]).getX(), 2) +
                        Math.pow(error_reftemp2[2][1] - mLastFiremanPositionArrayList.get(indexResult[2]).getY(), 2)
        );
        Log.i("Vincent", "distance1: " + distance1);
        Log.i("Vincent", "distance2: " + distance2);
        if (distance1 <= distance2) {
            pref[2][0] = temp_pref1[0];
            pref[2][1] = temp_pref1[1];
            pref[2][2] = temp_pref1[2];
        } else {
            pref[2][0] = temp_pref2[0];
            pref[2][1] = temp_pref2[1];
            pref[2][2] = temp_pref2[2];
        }
        Log.i("roataTheta", "第三个坐标点x：" + pref[2][0]);
        Log.i("roataTheta", "第三个坐标点y：" + pref[2][1]);
        Log.i("roataTheta", "第三个坐标点z：" + pref[2][2]);
        return pref;
    }

    //计算前3个点坐标
//    public static double[][] calculateThreePoint(double[][] mDistanceArray, int[] indexResult, double[][] pref, ArrayList<FiremanPosition> mLastFiremanPositionArrayList, double roataTheta) {
//        //第一个点坐标
//        pref[0][0] = 0;
//        pref[0][1] = 0;
//        pref[0][2] = 0.0;
//        //第二个点坐标
//        pref[1][0] = mDistanceArray[0][indexResult[1]];
//        pref[1][1] = 0;
//        pref[1][2] = 0.0;
//
//        pref = MatrixUtil2.calculate3rdPointFrom2(pref,  //第三个点坐标
//                mDistanceArray[0][indexResult[1]], mDistanceArray[0][indexResult[2]],
//                mDistanceArray[indexResult[1]][indexResult[2]], mLastFiremanPositionArrayList, roataTheta);
//        return pref;
//    }
    //计算前3个点坐标
    public static double[][] calculateThreePoint2(Map distanceMap, int[] indexResult,
                                                  double[][] pref, ArrayList<FiremanPosition> mLastFiremanPositionArrayList,
                                                  double roataTheta) {
        //第一行key
        Object[] key = distanceMap.keySet().toArray();
        Object[] firstLineInnerKey = ((Map) distanceMap.get(key[0])).keySet().toArray();  //第一行key
        Object[] maxLineInnerKey = ((Map) distanceMap.get(key[indexResult[1]])).keySet().toArray(); //最大行key
        //第一个点坐标
        pref[0][0] = 0;
        pref[0][1] = 0;
        pref[0][2] = 0.0;
        //第二个点坐标
        pref[1][0] = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[1]])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[1]]));
        pref[1][1] = 0;
        pref[1][2] = 0.0;
//        double distance12, double round1, double round2
        double distance12 = pref[1][0];
        double round1 = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[2]])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[2]]));
        double round2 = (((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[indexResult[2]])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[indexResult[2]]));
        pref = calculate3rdPointFrom2(pref,  //第三个点坐标
                distance12, round1,
                round2, mLastFiremanPositionArrayList, roataTheta, indexResult);
        return pref;
    }

    // 确定了三个点计算剩余点坐标
    public static double[] calculateByAngle(int i, double[][] pref, double[] distance,
                                            double[] rk) {

        double d12 = distance[0];
        double d23 = distance[1];
        double d31 = distance[2];
        double r1k = rk[0];
        double r2k = rk[1];
        double r3k = rk[2];
        double x1 = pref[0][0];
        double x2 = pref[1][0];
        double x3 = pref[2][0];
        double y1 = pref[0][1];
        double y2 = pref[1][1];
        double y3 = pref[2][1];
        double[][] B = new double[2][1];
        B[0][0] = 0.5 * (Math.pow(x2, 2) + Math.pow(y2, 2) - Math.pow(r2k, 2) - (Math.pow(x1, 2) + Math.pow(y1, 2) - Math.pow(r1k, 2)));
        B[1][0] = 0.5 * (Math.pow(x3, 2) + Math.pow(y3, 2) - Math.pow(r3k, 2) - (Math.pow(x1, 2) + Math.pow(y1, 2) - Math.pow(r1k, 2)));
        double[][] A = new double[][]{{x2 - x1, y2 - y1}, {x3 - x1, y3 - y1}};
        Matrix aMatrix = new Matrix(A);
        Matrix bMatrix = new Matrix(B);
        Matrix xMatrix = aMatrix.solve(bMatrix);
        double[][] X = xMatrix.getArray();
        Log.i("helloworld", "X: " + X[0][0]);
        Log.i("helloworld", "Y: " + X[1][0]);
        double[] angleArix = new double[3];
        angleArix[0] = X[0][0];
        angleArix[1] = X[1][0];
        angleArix[2] = 0;
        return angleArix;
    }

    //计算旋转角度

    public static double calculateRotateAngle
            (List<FiremanPosition> mLastFiremanPositionArrayList, int[] indexResult) {
        if (mLastFiremanPositionArrayList == null) {
            return 0.0;
        } else {
            if (mLastFiremanPositionArrayList.size() <= indexResult[1]) return 0.0;
            double theta = Math.atan(
                    (mLastFiremanPositionArrayList.get(indexResult[1]).getY() - mLastFiremanPositionArrayList.get(indexResult[0]).getY())
                            / (mLastFiremanPositionArrayList.get(indexResult[1]).getX() - mLastFiremanPositionArrayList.get(indexResult[0]).getX())
            );
            if (mLastFiremanPositionArrayList.get(indexResult[1]).getX() - mLastFiremanPositionArrayList.get(indexResult[0]).getX() > 0) {
                return theta;
            } else {
                return Math.PI + theta;
            }
        }
    }

    //计算其他坐标点
    public static double[][] calculateOthersPointFrom(double[][] arix, Map distanceMap,
                                                      int[] indexResult, double[][] pref, double[] rk) {
        //第一行key
        Object[] key = distanceMap.keySet().toArray();
        Object[] firstLineInnerKey = ((Map) distanceMap.get(key[0])).keySet().toArray();  //第一行key
        Object[] maxLineInnerKey = ((Map) distanceMap.get(key[indexResult[1]])).keySet().toArray(); //最大行key
        Object[] thirdLineInnerKey = ((Map) distanceMap.get(key[indexResult[2]])).keySet().toArray(); //第三个点 行key
        double[] distance = new double[3];
        distance[0] = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[1]])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[1]]));              //01 and 02
        distance[1] = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[2]])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[2]]));             //02 and 03;
        distance[2] = (((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[indexResult[2]])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[indexResult[2]])); //03 and 01
        for (int i = 0; i < arix.length; i++) {

            if (i == indexResult[0]) {
                arix[i][0] = pref[0][0]; //x坐标
                arix[i][1] = pref[0][1]; //y
                arix[i][2] = pref[0][2]; //z
            } else if (i == indexResult[1]) {
                arix[i][0] = pref[1][0]; //x坐标
                arix[i][1] = pref[1][1]; //y
                arix[i][2] = pref[1][2]; //z
            } else if (i == indexResult[2]) {
                arix[i][0] = pref[2][0]; //x坐标
                arix[i][1] = pref[2][1]; //y
                arix[i][2] = pref[2][2]; //z
            } else {
                if (i < firstLineInnerKey.length) {
                    rk[0] = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i]));
                } else {
                    rk[0] = 0;
                }
                if (i < maxLineInnerKey.length) {
                    rk[1] = (((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[i])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[i]));
                } else {
                    rk[1] = 0;
                }
                if (i < thirdLineInnerKey.length) {
                    rk[2] = (((Map) distanceMap.get(key[indexResult[2]])).get(thirdLineInnerKey[i])) == null ? 0 : ((Float) ((Map) distanceMap.get(key[indexResult[2]])).get(thirdLineInnerKey[i]));
                } else {
                    rk[2] = 0;
                }

                double[] calculateResult = calculateByAngle(i, pref, distance, rk);
                arix[i][0] = calculateResult[0];
                arix[i][1] = calculateResult[1];
                arix[i][2] = calculateResult[2];
            }
        }
        return arix;
    }

    //知道前三个点计算其他点
//    public static double[][] calculateOthersPointFrom2(double[][] arix, double[][] mDistanceArray, int[] indexResult, double[][] pref, double[] rk) {
//        double[] distance = new double[3];
//        distance[0] = mDistanceArray[0][indexResult[1]];              //01 and 02
//        distance[1] = mDistanceArray[0][indexResult[2]];              //02 and 03;
//        distance[2] = mDistanceArray[indexResult[1]][indexResult[2]]; //03 and 01
//        for (int i = 0; i < 7; i++) {
//
//            if (i == indexResult[0]) {
//                arix[i][0] = pref[0][0]; //x坐标
//                arix[i][1] = pref[0][1]; //y
//                arix[i][2] = pref[0][2]; //z
//            } else if (i == indexResult[1]) {
//                arix[i][0] = pref[1][0]; //x坐标
//                arix[i][1] = pref[1][1]; //y
//                arix[i][2] = pref[1][2]; //z
//            } else if (i == indexResult[2]) {
//                arix[i][0] = pref[2][0]; //x坐标
//                arix[i][1] = pref[2][1]; //y
//                arix[i][2] = pref[2][2]; //z
//            } else {
//                rk[0] = mDistanceArray[indexResult[0]][i];
//                rk[1] = mDistanceArray[indexResult[1]][i];
//                rk[2] = mDistanceArray[indexResult[2]][i];
//                double[] calculateResult = MatrixUtil2.calculateByAngle(i, pref, distance, rk);
//                arix[i][0] = calculateResult[0];
//                arix[i][1] = calculateResult[1];
//                arix[i][2] = calculateResult[2];
//            }
//        }
//        return arix;
//    }

    //计算map按key值排序
    public static Map mapSort(Map map) {
        Object[] key = map.keySet().toArray();
        Arrays.sort(key);

        return map;
    }

}
