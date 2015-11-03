package com.honeywell.firemanlocate.view.chart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.honeywell.firemanlocate.model.FiremanPosition;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by lynn.liu on 6/16/15.
 */
public class ScatterChart extends BaseChart {

    private ArrayList<FiremanPosition> mFiremanPositionArrayList;
    private ArrayList<FiremanPosition> mLastFiremanPositionArrayList;
    private TreeMap mDistanceMap;
    private int[] mColorsArray = {Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color
            .RED, Color.RED, Color.RED, Color.RED, Color.RED};
    private PointStyle[] mPointStyles = {PointStyle.CIRCLE, PointStyle.CIRCLE, PointStyle.CIRCLE,
            PointStyle.CIRCLE, PointStyle.CIRCLE, PointStyle.CIRCLE};

    public ScatterChart(ArrayList<FiremanPosition> firemanPositionArrayList, ArrayList<FiremanPosition> lastFiremanPositionArrayList,TreeMap distanceMap) {

        mFiremanPositionArrayList = firemanPositionArrayList;
        mLastFiremanPositionArrayList = lastFiremanPositionArrayList;
        mDistanceMap = distanceMap;
    }

    public ScatterChart(ArrayList<FiremanPosition> firemanPositionArrayList) {

        mFiremanPositionArrayList = firemanPositionArrayList;
    }

    /**
     * Returns the chart name.
     *
     * @return the chart name
     */
    @Override
    public String getName() {
//        return "Scatter chart";
        return "";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    @Override
    public String getDesc() {
        return null;
    }

    @Override
    public Intent execute(Context context) {
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
        XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
        setChartSettings(renderer, "", "X", "Y", -200, 200, -200, 800, Color.WHITE,
                Color.WHITE);
        renderer.setPointSize(15);
        renderer.setLabelsTextSize(25);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setAxisTitleTextSize(50);
        renderer.setAxesColor(Color.BLACK);
        renderer.setShowLegend(false);
        renderer.setYLabelsPadding(10);
        for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }
        return ChartFactory.getScatterChartIntent(context, buildDataset(titles, valuesX, valuesY), renderer, mLastFiremanPositionArrayList,mDistanceMap);
    }
}