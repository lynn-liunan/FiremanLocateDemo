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

/**
 * Created by lynn.liu on 6/16/15.
 */
public class ScatterChart2 extends BaseChart {

    private ArrayList<FiremanPosition> mFiremanPositionArrayList;
    private int[] mColorsArray = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color
            .MAGENTA, Color.DKGRAY, Color.GRAY, Color.LTGRAY};
    private PointStyle[] mPointStyles = {PointStyle.X, PointStyle.DIAMOND, PointStyle.TRIANGLE,
            PointStyle.SQUARE, PointStyle.CIRCLE};

    public ScatterChart2(ArrayList<FiremanPosition> firemanPositionArrayList) {
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

    /**
     * Executes the chart demo.
     *
     * @param context the context
     * @return the built intent
     */
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
        setChartSettings(renderer, "", "X", "Y", -2, 8, -2, 8, Color.GRAY,
                Color.LTGRAY);
//        renderer.setXLabels(10);
//        renderer.setYLabels(10);
        renderer.setZoomEnabled(false);
        renderer.setPointSize(15);
        renderer.setLabelsTextSize(25);
        renderer.setAxisTitleTextSize(50);
        renderer.setLegendTextSize(50);
        renderer.setYLabelsPadding(10);
        for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }
        return ChartFactory.getScatterChartIntent(context, buildDataset(titles, valuesX, valuesY), renderer);
    }
}
