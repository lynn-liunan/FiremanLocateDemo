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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.honeywell.firemanlocate.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.ScatterChart;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class GraphicalActivity extends Activity implements OnClickListener {
    /**
     * The encapsulated graphical view.
     */
    private GraphicalView mGraphicaView;
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
}