package com.honeywell.firemanlocate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.honeywell.firemanlocate.R;
import com.honeywell.firemanlocate.model.FiremanPosition;
import com.honeywell.firemanlocate.model.IPackage;
import com.honeywell.firemanlocate.model.Report;
import com.honeywell.firemanlocate.util.MatrixUtil;
import com.honeywell.firemanlocate.view.chart.ScatterChart;
import com.honeywell.firemanlocate.view.chart.ScatterChart2;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.honeywell.firemanlocate.config.Constant;

public class MainActivity extends BaseActivity {


    private static final String FILE_NAME = "distances.txt";

    private double[][] mDistanceArray;
    private int[] indexResult = null; //save max and third point index
    private ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();
    private ArrayList<FiremanPosition> mLastFiremanPositionArrayList = null;
    private Button mCalculateButton;
    private Button mOldCalculateButton;
    private Button mLoadFileButton;
    //2 通过上一次坐标点计算旋转坐标原点 返回旋转角度
    double roataTheta = 0.0;
    double[] rk = new double[3];
    double[][] pref = new double[3][3]; //3行2列矩阵  前3个坐标点缓存
    double[][] arix = null;
    private Map mDistanceMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<Report> mReportList = (ArrayList<Report>) getIntent().getSerializableExtra(ShowActivity.send_parameter);
        mCalculateButton = (Button) findViewById(R.id.calculate_button);
        mOldCalculateButton = (Button) findViewById(R.id.old_calculate_button);
        mLoadFileButton = (Button) findViewById(R.id.load_button);
        if (mReportList != null) parseList(mReportList);
    }

    //遍历reportList，封装成distanceMapduixiang
    private void parseList(List<Report> mReportList) {
        mDistanceMap = new HashMap();
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        mCalculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFiremanPositionArrayList.clear();
                calculatePointsPosition(false); //计算
            }
        });
        mLoadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDataFromFile(Constant.demoDistant);
            }
        });
        mOldCalculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFiremanPositionArrayList.clear();
                loadDataFromFile(Constant.secondDistan);
                calculatePointsPosition(false); //计算
            }
        });
    }

    private void loadDataFromFile(String fileName) {
        // String jsonString = SDUtil.readFileUseBufferReader(FILE_NAME);

        try {
            //  JSONArray responseArray = new JSONArray(jsonString);
            JSONArray responseArray = new JSONArray(fileName);
            mDistanceArray = new double[responseArray.length()][];
            for (int i = 0; i < responseArray.length(); i++) {
                JSONArray distanceArray = responseArray.getJSONArray(i);
                mDistanceArray[i] = new double[distanceArray.length()];
                for (int j = 0; j < distanceArray.length(); j++) {
                    mDistanceArray[i][j] = distanceArray.getDouble(j);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //按钮点击事件计算所有位置
    private void calculatePointsPosition(boolean isOld) {

        if (mDistanceArray == null) {
            Toast.makeText(this, R.string.load_file_first, Toast.LENGTH_SHORT).show();
            return;
        }

         arix = new double[mDistanceArray.length][3];
//        arix = new double[mDistanceMap.size()][3];
        indexResult = MatrixUtil.calculateMaxDistant(mDistanceArray); // 1 计算最大三个点下标
        // indexResult = MatrixUtil.calculateMaxDistant2(mDistanceMap);
        if (mLastFiremanPositionArrayList != null) {
            roataTheta = MatrixUtil.calculateRotateAngle(mLastFiremanPositionArrayList, indexResult);      //计算旋转角度 如果有历史数据
        }


        pref = MatrixUtil.calculateThreePoint(mDistanceArray, indexResult, pref, mLastFiremanPositionArrayList, roataTheta); //确定前三个点坐标
        //pref = MatrixUtil.calculateThreePoint2(mDistanceMap, indexResult, pref, mLastFiremanPositionArrayList, roataTheta); //确定前三个点坐标
        arix = MatrixUtil.calculateOthersPointFrom2(arix, mDistanceArray, indexResult, pref, rk); //计算其他点坐标
//        arix = MatrixUtil.calculateOthersPointFrom(arix, mDistanceMap, indexResult, pref,rk); //计算其他点坐标
        arix = MatrixUtil.transferAxis(arix, roataTheta);  //最后一次旋转，算出旋转坐标
        //将坐标换为对象
        for (int i = 0; i < arix.length; i++) {
            Log.i("arix :", "arix" + i + " x: " + arix[i][0]);
            Log.i("arix :", "arix" + i + " y: " + arix[i][1]);
            Log.i("arix :", "arix" + i + " z: " + arix[i][2]);
            mFiremanPositionArrayList.add(new FiremanPosition(arix[i][0], arix[i][1], arix[i][2]));
        }
        mLastFiremanPositionArrayList = saveFiremanPositionHistory(mFiremanPositionArrayList);
        if (isOld) {
            Intent intent = (new ScatterChart2(mFiremanPositionArrayList)).execute(this);
            startActivity(intent);
        } else {
            Intent intent = (new ScatterChart(mFiremanPositionArrayList)).execute(this);
            startActivity(intent);
        }
    }

    //保存历史firemanPosition对象
    //深度copy 保存历史数据
    private ArrayList<FiremanPosition> saveFiremanPositionHistory(ArrayList<FiremanPosition> firemanPositionArray) {
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


}
