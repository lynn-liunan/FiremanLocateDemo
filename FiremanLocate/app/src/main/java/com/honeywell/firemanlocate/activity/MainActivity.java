package com.honeywell.firemanlocate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.honeywell.firemanlocate.R;
import com.honeywell.firemanlocate.model.FiremanPosition;
import com.honeywell.firemanlocate.util.SDUtil;
import com.honeywell.firemanlocate.view.chart.ScatterChart;
import com.honeywell.firemanlocate.view.chart.ScatterChart2;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String FILE_NAME = "distances.txt";

    private double[][] mDistanceArray;
    private ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();

    private Button mCalculateButton;
    private Button mOldCalculateButton;
    private Button mLoadFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCalculateButton = (Button) findViewById(R.id.calculate_button);
        mOldCalculateButton = (Button) findViewById(R.id.old_calculate_button);
        mLoadFileButton = (Button) findViewById(R.id.load_button);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mCalculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFiremanPositionArrayList.clear();
                calculatePointsPosition(false);
            }
        });
        mLoadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDataFromFile();
            }
        });
        mOldCalculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFiremanPositionArrayList.clear();
                calculatePointsPosition(true);
            }
        });
    }

    private void loadDataFromFile() {
        String jsonString = SDUtil.readFileUseBufferReader(FILE_NAME);
        try {
            JSONArray responseArray = new JSONArray(jsonString);
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

    private void calculatePointsPosition(boolean isOld) {
        if (mDistanceArray == null) {
            Toast.makeText(this, R.string.load_file_first, Toast.LENGTH_SHORT).show();
            return;
        }
        FiremanPosition firemanPosition0 = new FiremanPosition(0, 0);
        mFiremanPositionArrayList.add(firemanPosition0);
        FiremanPosition firemanPosition1 = new FiremanPosition(mDistanceArray[0][1], 0);
        mFiremanPositionArrayList.add(firemanPosition1);
        FiremanPosition firemanPosition2 = calculate3rdPointFrom2(firemanPosition0, firemanPosition1,
                mDistanceArray[0][1], mDistanceArray[0][2],
                mDistanceArray[1][2]);
        if (firemanPosition2 != null) {
            mFiremanPositionArrayList.add(firemanPosition2);
            for (int i = 3; i < mDistanceArray.length; i++) {
                FiremanPosition firemanPosition_i = calculate4thPointFrom3(firemanPosition0, firemanPosition1,
                        firemanPosition2, mDistanceArray[0][1], mDistanceArray[0][i], mDistanceArray[1][i],
                        mDistanceArray[2][i]);
                mFiremanPositionArrayList.add(firemanPosition_i);
            }
            if (isOld) {
                Intent intent = (new ScatterChart2(mFiremanPositionArrayList)).execute(this);
                startActivity(intent);
            } else {
                Intent intent = (new ScatterChart(mFiremanPositionArrayList)).execute(this);
                startActivity(intent);
            }
        } else {

        }
    }

    private FiremanPosition calculate4thPointFrom3(FiremanPosition firemanPosition1, FiremanPosition
            firemanPosition2, FiremanPosition firemanPosition3, double distance12, double round1, double round2,
                                                   double round3) {
        FiremanPosition firemanPosition = null;
        FiremanPosition firemanPosition_a;
        FiremanPosition firemanPosition_b;
        if (distance12 < (round1 + round2)) {
            double theta1 = Math.acos((Math.pow(round1, 2) + Math.pow(distance12, 2) - Math.pow(round2, 2)) / (2 *
                    round1 * distance12));
            double theta2 = Math.atan((firemanPosition2.getY() - firemanPosition1.getY()) / (firemanPosition2.getX()
                    - firemanPosition1.getX()));
            firemanPosition_a = new FiremanPosition();
            firemanPosition_a.setX(firemanPosition1.getX() + round1 * Math.cos(theta1 + theta2));
            firemanPosition_a.setY(firemanPosition1.getY() + round1 * Math.sin(theta1 + theta2));
            firemanPosition_b = new FiremanPosition();
            firemanPosition_b.setX(firemanPosition1.getX() + round1 * Math.cos(theta2 - theta1));
            firemanPosition_b.setY(firemanPosition1.getY() + round1 * Math.sin(theta2 - theta1));
            double distance1 = Math.sqrt(Math.pow(firemanPosition_a.getX() - firemanPosition3.getX(), 2) + Math.pow
                    (firemanPosition_a.getY() - firemanPosition3.getY(), 2));
            double distance2 = Math.sqrt(Math.pow(firemanPosition_b.getX() - firemanPosition3.getX(), 2) + Math.pow
                    (firemanPosition_b.getY() - firemanPosition3.getY(), 2));
            if (Math.abs(distance1 - round3) <= Math.abs(distance2 - round3)) {
                firemanPosition = firemanPosition_a;
            } else {
                firemanPosition = firemanPosition_b;
            }
        }
        return firemanPosition;
    }

    private FiremanPosition calculate3rdPointFrom2(FiremanPosition firemanPosition1, FiremanPosition
            firemanPosition2, double distance12, double round1, double round2) {
        FiremanPosition firemanPosition = null;
        if (distance12 <= (round1 + round2)) {
            double theta1 = Math.acos((Math.pow(round1, 2) + Math.pow(distance12, 2) - Math.pow(round2, 2)) / (2 *
                    round1 * distance12));
            double theta2 = Math.atan((firemanPosition2.getY() - firemanPosition1.getY()) / (firemanPosition2.getX()
                    - firemanPosition1.getX()));
            firemanPosition = new FiremanPosition();
            firemanPosition.setX(firemanPosition1.getX() + round1 * Math.cos(theta1 + theta2));
            firemanPosition.setY(firemanPosition1.getY() + round1 * Math.sin(theta1 + theta2));
        }
        return firemanPosition;
    }
}
