package com.honeywell.firemanlocate.util;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.honeywell.firemanlocate.R;
import com.honeywell.firemanlocate.activity.ShowActivity;
import com.honeywell.firemanlocate.model.FiremanPosition;
import com.honeywell.firemanlocate.model.IPackage;
import com.honeywell.firemanlocate.model.Report;
import com.honeywell.firemanlocate.view.chart.ScatterChart;
import com.honeywell.firemanlocate.view.chart.ScatterChart2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Jama.Matrix;


/**
 * Created by Vincent on 28/7/15.
 */
public class MatrixUtil2 {
    private double[][] mDistanceArray;
    //    private int[] indexResult = null; //save max and third point index
    private ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();
    //    private ArrayList<FiremanPosition> mLastFiremanPositionArrayList = null;
    //2 通过上一次坐标点计算旋转坐标原点 返回旋转角度
//    double roataTheta = 0.0;
    double[] rk = new double[3];
//    double[][] pref = new double[3][3]; //3行2列矩阵  前3个坐标点缓存
//    double[][] arix = null;


    //遍历reportList，封装成distanceMapduixiang
    public static Map parseList(ArrayList<Report> mReportList, Map mDistanceMap) {

        for (int j = 0; j < mReportList.size(); j++) {
            IPackage iPackage = mReportList.get(j);
            for (int i = 0; i < ((Report) iPackage).getDataBlocks().length; i++) {
                if (!mDistanceMap.containsKey(((Report) iPackage).getDataBlocks()[i].getModuleAID())) {

                    Map subDistanceMap = new HashMap();
                    subDistanceMap.put(((Report) iPackage).getDataBlocks()[i].getModuleAID(), 0);
                    subDistanceMap.put(((Report) iPackage).getDataBlocks()[i].getModuleBID(), ((Report) iPackage).getDataBlocks()[i].getDistance());
                    MatrixUtil.mapSort(subDistanceMap); //内部Map排序
                    mDistanceMap.put(((Report) iPackage).getDataBlocks()[i].getModuleAID(), subDistanceMap);
                    MatrixUtil.mapSort(mDistanceMap);   //最外部Map排序
                } else {
                    Map subDistance = (Map) mDistanceMap.get(((Report) iPackage).getDataBlocks()[i].getModuleAID());
                    subDistance.put(((Report) iPackage).getDataBlocks()[i].getModuleBID(), ((Report) iPackage).getDataBlocks()[i].getDistance());
                    MatrixUtil.mapSort(subDistance);
                }
            }
        }
        return mDistanceMap;
    }

    public static ArrayList<FiremanPosition> calculatePointsPosition(Map mDistanceMap, ArrayList<FiremanPosition> mLastFiremanPositionArrayList) {
        double[][] pref = new double[3][3];
        double roataTheta =0.0;
        double[] rk = new double[3];
        ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();
        if (mDistanceMap == null) {

            return null;
        }

//        arix = new double[mDistanceArray.length][3];
        double[][] arix = new double[mDistanceMap.size()][3];
//        indexResult = MatrixUtil.calculateMaxDistant(mDistanceArray); // 1 计算最大三个点下标
        int[] indexResult = calculateMaxDistant2(mDistanceMap);
        if (mLastFiremanPositionArrayList != null) {
             roataTheta = calculateRotateAngle(mLastFiremanPositionArrayList, indexResult);      //计算旋转角度 如果有历史数据
        }


//        pref = MatrixUtil.calculateThreePoint(mDistanceArray, indexResult, pref, mLastFiremanPositionArrayList, roataTheta); //确定前三个点坐标
        pref = calculateThreePoint2(mDistanceMap, indexResult, pref, mLastFiremanPositionArrayList, roataTheta); //确定前三个点坐标
     //   arix = MatrixUtil.calculateOthersPointFrom2(arix, mDistanceArray, indexResult, pref, rk); //计算其他点坐标
        arix = calculateOthersPointFrom(arix, mDistanceMap, indexResult, pref,rk); //计算其他点坐标
        arix = MatrixUtil.transferAxis(arix, roataTheta);  //最后一次旋转，算出旋转坐标
        //将坐标换为对象
        for (int i = 0; i < arix.length; i++) {
            Log.i("arix :", "arix" + i + " x: " + arix[i][0]);
            Log.i("arix :", "arix" + i + " y: " + arix[i][1]);
            Log.i("arix :", "arix" + i + " z: " + arix[i][2]);
            mFiremanPositionArrayList.add(new FiremanPosition(arix[i][0], arix[i][1], arix[i][2]));
        }
     //   mLastFiremanPositionArrayList = saveFiremanPositionHistory(mFiremanPositionArrayList);
        return mFiremanPositionArrayList;
    }
    //保存历史firemanPosition对象
    //深度copy 保存历史数据
    public static ArrayList<FiremanPosition> saveFiremanPositionHistory(ArrayList<FiremanPosition> firemanPositionArray) {
        ArrayList<FiremanPosition> lastFiremanPositionArrayList = new ArrayList<FiremanPosition>(firemanPositionArray.size());
        Iterator<FiremanPosition> iterator = firemanPositionArray.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            FiremanPosition fp = iterator.next().clone();
            lastFiremanPositionArrayList.add(fp);
            double x = fp.getX();
            double y = fp.getY();
            double z = fp.getZ();
            Log.i("arix", "LastPosition" + i + " x; " + x + " y: " + y + " z: " + z);
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
        return new int[]{0, max_index, third_index};
    }

    public static int[] calculateMaxDistant2(Map distanceMap) {
        double max_data1 = 0.0;
        double max_data2 = 0.0;
        int max_index = 0;
        int third_index = 0;
        Object[] key = distanceMap.keySet().toArray();
        Object[] firstLineInnerKey = ((Map) distanceMap.get(key[0])).keySet().toArray();  //第一行key
        Object[] maxLineInnerKey = ((Map) distanceMap.get(key[max_index])).keySet().toArray(); //最大行key

        double theta = 0.0;
        for (int i = 0; i < ((Map) distanceMap.get(key[0])).size(); i++) {
            if (((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i])) > max_data1) {
                max_data1 = ((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i]));
                max_index = i;
            }
        }
        for (int i = 0; i < ((Map) distanceMap.get(key[0])).size(); i++) {
            theta = Math.acos((Math.pow(((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i])), 2)
                    + Math.pow(((int) ((Map) distanceMap.get(key[max_index])).get(maxLineInnerKey[i])), 2) - Math.pow(((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[max_index])), 2))
                    / (2 * ((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i])) * ((int) ((Map) distanceMap.get(key[max_index])).get(maxLineInnerKey[i]))));
            if (((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i])) != max_data1 && (((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i]))
                    + ((int) ((Map) distanceMap.get(key[max_index])).get(maxLineInnerKey[i])) > max_data2)
                    && theta >= 0.52 && theta <= 2.09) {
                max_data2 = ((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i])) + ((int) ((Map) distanceMap.get(key[max_index])).get(maxLineInnerKey[i]));
                third_index = i;
            }

        }
        return new int[]{0, max_index, third_index};
    }

    //通过下标计算第三个点坐标
    public static double[][] calculate3rdPointFrom2(double[][] pref, double distance12, double round1, double round2, ArrayList<FiremanPosition> mLastFiremanPositionArrayList, double roataTheta) {
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
            pref = chooseFiremanPosition3(pref, round1, theta1, theta2, mLastFiremanPositionArrayList, roataTheta);

        } else {

        }
        return pref;
    }

    //选取第三个坐标点
    public static double[][] chooseFiremanPosition3(double[][] pref, double round1, double theta1, double theta2, ArrayList<FiremanPosition> mLastFiremanPositionArrayList, double roataTheta) {
        double[] temp_pref1 = {pref[0][0] + round1 * Math.cos(theta1 + theta2), pref[0][1] + round1 * Math.sin(theta1 + theta2), 0};
        pref[2][0] = pref[0][0] + round1 * Math.cos(theta1 + theta2);
        pref[2][1] = pref[0][1] + round1 * Math.sin(theta1 + theta2);
        pref[2][2] = 0.0;

        if (mLastFiremanPositionArrayList == null) {

            return pref;
        }
        double[][] error_reftemp1 = MatrixUtil2.transferAxis(pref, roataTheta);
        double distance1 = Math.sqrt(
                Math.pow(error_reftemp1[2][0] - mLastFiremanPositionArrayList.get(2).getX(), 2) +
                        Math.pow(error_reftemp1[2][1] - mLastFiremanPositionArrayList.get(2).getY(), 2)
        );

        pref[2][0] = pref[0][0] + round1 * Math.cos(theta2 - theta1);
        pref[2][1] = pref[0][1] + round1 * Math.sin(theta2 - theta1);
        pref[2][2] = 0;
        double[] temp_pref2 = {pref[0][0] + round1 * Math.cos(theta2 - theta1), pref[0][1] + round1 * Math.sin(theta2 - theta1), 0};
        //firemanTransferPosition1 = transferAxis(firemanPositionTemp1);
        double[][] error_reftemp2 = MatrixUtil2.transferAxis(pref, roataTheta);
        double distance2 = Math.sqrt(
                Math.pow(error_reftemp2[2][0] - mLastFiremanPositionArrayList.get(2).getX(), 2) +
                        Math.pow(error_reftemp2[2][1] - mLastFiremanPositionArrayList.get(2).getY(), 2)
        );
        if (distance1 <= distance2) {
            pref[2][0] = temp_pref1[0];
            pref[2][1] = temp_pref1[1];
            pref[2][2] = temp_pref1[2];
        } else {
            pref[2][0] = temp_pref2[0];
            pref[2][1] = temp_pref2[1];
            pref[2][2] = temp_pref2[2];
        }
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
    public static double[][] calculateThreePoint2(Map distanceMap, int[] indexResult, double[][] pref, ArrayList<FiremanPosition> mLastFiremanPositionArrayList, double roataTheta) {
        //第一行key
        Object[] key = distanceMap.keySet().toArray();
        Object[] firstLineInnerKey = ((Map) distanceMap.get(key[0])).keySet().toArray();  //第一行key
        Object[] maxLineInnerKey = ((Map) distanceMap.get(key[indexResult[1]])).keySet().toArray(); //最大行key

        //第一个点坐标
        pref[0][0] = 0;
        pref[0][1] = 0;
        pref[0][2] = 0.0;
        //第二个点坐标
        pref[1][0] = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[1]])) == null ? 0 : ((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[1]]));
        pref[1][1] = 0;
        pref[1][2] = 0.0;
//        double distance12, double round1, double round2
        double distance12 = pref[1][0];
        double round1 = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[2]])) == null ? 0 : ((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[2]]));
        double round2 = (((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[indexResult[2]])) == null ? 0 : ((int) ((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[indexResult[2]]));
        pref = MatrixUtil.calculate3rdPointFrom2(pref,  //第三个点坐标
                distance12, round1,
                round2, mLastFiremanPositionArrayList, roataTheta);
        return pref;
    }

    // 确定了三个点计算剩余点坐标
    public static double[] calculateByAngle(int i, double[][] pref, double[] distance, double[] rk) {

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
    public static double calculateRotateAngle(List<FiremanPosition> mLastFiremanPositionArrayList, int[] indexResult) {
        if (mLastFiremanPositionArrayList == null) {
            return 0.0;
        } else {
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
    public static double[][] calculateOthersPointFrom(double[][] arix, Map distanceMap, int[] indexResult, double[][] pref, double[] rk) {
        //第一行key
        Object[] key = distanceMap.keySet().toArray();
        Object[] firstLineInnerKey = ((Map) distanceMap.get(key[0])).keySet().toArray();  //第一行key
        Object[] maxLineInnerKey = ((Map) distanceMap.get(key[indexResult[1]])).keySet().toArray(); //最大行key
        Object[] thirdLineInnerKey = ((Map) distanceMap.get(key[indexResult[2]])).keySet().toArray(); //第三个点 行key
        double[] distance = new double[3];
        distance[0] = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[1]])) == null ? 0 : ((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[1]]));              //01 and 02
        distance[1] = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[2]])) == null ? 0 : ((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[indexResult[2]]));             //02 and 03;
        distance[2] = (((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[indexResult[2]])) == null ? 0 : ((int) ((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[indexResult[2]])); //03 and 01
        for (int i = 0; i < 7; i++) {

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
                rk[0] = (((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i])) == null ? 0 : ((int) ((Map) distanceMap.get(key[0])).get(firstLineInnerKey[i]));
                rk[1] = (((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[i])) == null ? 0 : ((int) ((Map) distanceMap.get(key[indexResult[1]])).get(maxLineInnerKey[i]));
                rk[2] = (((Map) distanceMap.get(key[indexResult[2]])).get(thirdLineInnerKey[i])) == null ? 0 : ((int) ((Map) distanceMap.get(key[indexResult[2]])).get(thirdLineInnerKey[i]));
                double[] calculateResult = MatrixUtil.calculateByAngle(i, pref, distance, rk);
                arix[i][0] = calculateResult[0];
                arix[i][1] = calculateResult[1];
                arix[i][2] = calculateResult[2];
            }
        }
        return arix;
    }

    //知道前三个点计算其他点
    public static double[][] calculateOthersPointFrom2(double[][] arix, double[][] mDistanceArray, int[] indexResult, double[][] pref, double[] rk) {
        double[] distance = new double[3];
        distance[0] = mDistanceArray[0][indexResult[1]];              //01 and 02
        distance[1] = mDistanceArray[0][indexResult[2]];              //02 and 03;
        distance[2] = mDistanceArray[indexResult[1]][indexResult[2]]; //03 and 01
        for (int i = 0; i < 7; i++) {

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
                rk[0] = mDistanceArray[indexResult[0]][i];
                rk[1] = mDistanceArray[indexResult[1]][i];
                rk[2] = mDistanceArray[indexResult[2]][i];
                double[] calculateResult = MatrixUtil2.calculateByAngle(i, pref, distance, rk);
                arix[i][0] = calculateResult[0];
                arix[i][1] = calculateResult[1];
                arix[i][2] = calculateResult[2];
            }
        }
        return arix;
    }

    //计算map按key值排序
    public static Map mapSort(Map map) {
        Object[] key = map.keySet().toArray();
        Arrays.sort(key);

        return map;
    }

}
