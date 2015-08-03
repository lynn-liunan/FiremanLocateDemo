/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.honeywell.firemanlocate.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.honeywell.firemanlocate.R;
import com.honeywell.firemanlocate.model.FiremanPosition;
import com.honeywell.firemanlocate.model.Report;
import com.honeywell.firemanlocate.util.MatrixUtil;
import com.honeywell.firemanlocate.util.MatrixUtil2;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class GraphicalActivity extends Activity implements OnClickListener {
    /**
     * The encapsulated graphical view.
     */
    private GraphicalView mGraphicaView;
    public static final String UPDATE_DRAWVIEW_ACTION = "draw_position_received_action";
    public static final String send_parameter = "draw_pramater";
    public static final String UPDATE_DATA = "update_data";
    private static final int SEND_RESULT = 11;
    private int[] mColorsArray = {Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color
            .RED, Color.RED, Color.RED, Color.RED, Color.RED};
    private PointStyle[] mPointStyles = {PointStyle.CIRCLE, PointStyle.CIRCLE, PointStyle.CIRCLE,
            PointStyle.CIRCLE, PointStyle.CIRCLE, PointStyle.CIRCLE};
    /**
     * The chart to be drawn.
     */
    private AbstractChart mChart;
    private RelativeLayout mGraphicalPanel;

    // scale button
    private ImageButton mZoomInButton;
    private ImageButton mZoomOutButton;
    private ImageButton mZoom1Button;

    //menu button
    private ImageButton mSwitchXYButton;


    private BroadcastReceiver mUpdateReceiver;

    private Map mDistanceMap = new HashMap<>(); //存位置关系和距离
    private ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();
    private ArrayList<FiremanPosition> mLastFiremanPositionArrayList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphical);

        mGraphicalPanel = (RelativeLayout) findViewById(R.id.graphical_panel);
        mZoomInButton = (ImageButton) findViewById(R.id.zoom_in);
        mZoomOutButton = (ImageButton) findViewById(R.id.zoom_out);
        mZoom1Button = (ImageButton) findViewById(R.id.zoom_1);
        mSwitchXYButton = (ImageButton) findViewById(R.id.switch_XY);

        mZoomInButton.setOnClickListener(this);
        mZoomOutButton.setOnClickListener(this);
        mZoom1Button.setOnClickListener(this);
        mSwitchXYButton.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        mChart = (AbstractChart) extras.getSerializable(ChartFactory.CHART);
        mGraphicaView = new GraphicalView(this, mChart);
        String title = extras.getString(ChartFactory.TITLE);
        if (title == null) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else if (title.length() > 0) {
            setTitle(title);
        }
        mGraphicalPanel.addView(mGraphicaView);
        mGraphicaView.invalidate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //service 给到的广播
        mUpdateReceiver = new UpdateViewReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_DRAWVIEW_ACTION);
        registerReceiver(mUpdateReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.zoom_in:
                mGraphicaView.zoomIn();
                break;
            case R.id.zoom_out:
                mGraphicaView.zoomOut();
                break;
            case R.id.zoom_1:
                mGraphicaView.zoomReset();

                break;
            case R.id.switch_XY:
                mGraphicalPanel.removeView(mGraphicaView);
                AbstractChart newChart = switchXYData(mChart);
                 mGraphicaView = new GraphicalView(this, newChart);
                mChart = newChart;
                mGraphicalPanel.addView(mGraphicaView);
                break;
            default:
                break;
        }
    }

    public XYChart switchXYData(AbstractChart currentChart) {
        ScatterChart scatterChart = (ScatterChart) currentChart;
        XYMultipleSeriesDataset dataset = scatterChart.getDataset();
        XYMultipleSeriesDataset newDataset = new XYMultipleSeriesDataset();
        String titles[] = new String[dataset.getSeries().length];
        ArrayList<double[]> valuesX = new ArrayList<>();
        ArrayList<double[]> valuesY = new ArrayList<>();
        for (int i = 0; i < dataset.getSeries().length; i++) {
            titles[i] = dataset.getSeriesAt(i).getTitle();
            double[] xValues = new double[1];
            double[] yValues = new double[1];
            xValues[0] = dataset.getSeriesAt(i).getXYMap().getYByIndex(0);
            yValues[0] = dataset.getSeriesAt(i).getXYMap().getXByIndex(0);
            valuesX.add(xValues);
            valuesY.add(yValues);
        }
        addXYSeries(newDataset, titles, valuesX, valuesY, 0);
        XYChart chart = new ScatterChart(newDataset, scatterChart.getRenderer());
        return chart;
    }

    public XYMultipleSeriesDataset addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues,
                                               List<double[]> yValues, int scale) {
        int length = titles.length;
        for (int i = 0; i < length; i++) {
            XYSeries series = new XYSeries(titles[i], scale);
            double[] xV = xValues.get(i);
            double[] yV = yValues.get(i);
            int seriesLength = xV.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(xV[k], yV[k]);
                series.addAnnotation("No." + (i + 1), xV[k], yV[k]);
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    private Handler mHandlerUpdateData = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_RESULT:
                    mFiremanPositionArrayList = MatrixUtil2.calculatePointsPosition(mDistanceMap, mLastFiremanPositionArrayList);
                    mLastFiremanPositionArrayList = MatrixUtil2.saveFiremanPositionHistory(mFiremanPositionArrayList);
                    mGraphicalPanel.removeView(mGraphicaView);
                    AbstractChart newChart = repeatGraphicaView();
                    mGraphicaView = new GraphicalView(GraphicalActivity.this, newChart);
                    mChart = newChart;
                    mGraphicalPanel.addView(mGraphicaView);
                    break;
                default:
                    break;
            }
        }
    };
    //service给到的广播
    private class UpdateViewReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            List<Report> mReportList = (ArrayList<Report>) intent.getSerializableExtra(UPDATE_DATA);
            if (mReportList != null) {
                Message msg = new Message();
                msg.what = SEND_RESULT;
                msg.obj = MatrixUtil.parseList(mReportList, mDistanceMap);
                mHandlerUpdateData.sendMessage(msg);
            }
        }
    }
    //定时重绘坐标
    private XYChart repeatGraphicaView(){
        String[] titles = new String[mFiremanPositionArrayList.size()];
        for (int i = 0; i < mFiremanPositionArrayList.size(); i++) {
            titles[i] = "Fireman " + (i + 1);
        }
        ArrayList<double[]> valuesX = new ArrayList<>();
        ArrayList<double[]> valuesY = new ArrayList<>();
        for (int i = 0; i < mFiremanPositionArrayList.size(); i++) {
            double[] xValues = new double[1];
            double[] yValues = new double[1];
            xValues[0] = mFiremanPositionArrayList.get(i).getX();
            yValues[0] = mFiremanPositionArrayList.get(i).getY();
            valuesX.add(xValues);
            valuesY.add(yValues);
        }
        int[] colors = new int[mFiremanPositionArrayList.size()];
        for (int i = 0; i < mFiremanPositionArrayList.size(); i++) {
            colors[i] = mColorsArray[i % mColorsArray.length];
        }
        PointStyle[] styles = new PointStyle[mFiremanPositionArrayList.size()];
        for (int i = 0; i < mFiremanPositionArrayList.size(); i++) {
            styles[i] = mPointStyles[i % mPointStyles.length];
        }
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
      //  XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
        setChartSettings(renderer, "", "X", "Y", -2, 8, -2, 8, Color.WHITE,
                Color.WHITE);
        renderer.setPointSize(15);
        renderer.setLabelsTextSize(25);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setAxisTitleTextSize(50);
        renderer.setAxesColor(Color.BLACK);
        renderer.setShowLegend(false);
        renderer.setYLabelsPadding(10);
        renderer.setLegendTextSize(15);
        renderer.setMargins(new int[]{20, 30, 15, 20});
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            renderer.addSeriesRenderer(r);
        }
        for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        addXYSeries(dataset, titles, valuesX, valuesY, 0);
        return new ScatterChart(dataset, renderer);

    }
    private void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
                                    String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
                                    int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
    }
}