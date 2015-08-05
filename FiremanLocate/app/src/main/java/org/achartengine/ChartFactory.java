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
package org.achartengine;

import android.content.Context;
import android.content.Intent;

import com.honeywell.firemanlocate.activity.GraphicalActivity;
import com.honeywell.firemanlocate.model.FiremanPosition;

import org.achartengine.chart.ScatterChart;
import org.achartengine.chart.XYChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility methods for creating chart views or intents.
 */
public class ChartFactory {
    /**
     * The key for the chart data.
     */
    public static final String CHART = "chart";

    /**
     * The key for the lastFiremanPosition data
     */
    public static final String LASTFIREMANPOSITON = "lastFiremanPosition";

    public static final String DISTANCEMAP = "distancemap";

    /**
     * The key for the chart graphical activity title.
     */
    public static final String TITLE = "title";

    private ChartFactory() {
        // empty
    }

    /**
     * Creates a scatter chart view.
     *
     * @param context  the context
     * @param dataset  the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @return a scatter chart graphical view
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     *                                  if the dataset and the renderer don't include the same number of
     *                                  series
     */
    public static final GraphicalView getScatterChartView(Context context,
                                                          XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        checkParameters(dataset, renderer);
        XYChart chart = new ScatterChart(dataset, renderer);
        return new GraphicalView(context, chart);
    }

    /**
     * Creates a scatter chart intent that can be used to start the graphical view
     * activity.
     *
     * @param context  the context
     * @param dataset  the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @return a scatter chart intent
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     *                                  if the dataset and the renderer don't include the same number of
     *                                  series
     */
    public static final Intent getScatterChartIntent(Context context,
                                                     XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, ArrayList<FiremanPosition> lastFiremanPosition,TreeMap distanceMap) {
        return getScatterChartIntent(context, dataset, renderer, "", lastFiremanPosition,distanceMap);
    }

    public static final Intent getScatterChartIntent(Context context,
                                                     XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        return getScatterChartIntent(context, dataset, renderer, "");
    }

    /**
     * Creates a scatter chart intent that can be used to start the graphical view
     * activity.
     *
     * @param context       the context
     * @param dataset       the multiple series dataset (cannot be null)
     * @param renderer      the multiple series renderer (cannot be null)
     * @param activityTitle the graphical chart activity title
     * @return a scatter chart intent
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     *                                  if the dataset and the renderer don't include the same number of
     *                                  series
     */
    public static final Intent getScatterChartIntent(Context context,
                                                     XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, String activityTitle,
                                                     ArrayList<FiremanPosition> lastFiremanPosition, TreeMap distanceMap) {
        checkParameters(dataset, renderer);
        Intent intent = new Intent(context, GraphicalActivity.class);
        XYChart chart = new ScatterChart(dataset, renderer);
        intent.putExtra(CHART, chart);
        intent.putExtra(LASTFIREMANPOSITON, lastFiremanPosition);
        intent.putExtra(DISTANCEMAP, distanceMap);
        intent.putExtra(TITLE, activityTitle);
        return intent;
    }

    public static final Intent getScatterChartIntent(Context context,
                                                     XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, String activityTitle) {
        checkParameters(dataset, renderer);
        Intent intent = new Intent(context, GraphicalActivity.class);
        XYChart chart = new ScatterChart(dataset, renderer);
        intent.putExtra(CHART, chart);
        intent.putExtra(TITLE, activityTitle);
        return intent;
    }

    /**
     * Checks the validity of the dataset and renderer parameters.
     *
     * @param dataset  the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     *                                  if the dataset and the renderer don't include the same number of
     *                                  series
     */
    private static void checkParameters(XYMultipleSeriesDataset dataset,
                                        XYMultipleSeriesRenderer renderer) {
        if (dataset == null || renderer == null
                || dataset.getSeriesCount() != renderer.getSeriesRendererCount()) {
            throw new IllegalArgumentException(
                    "Dataset and renderer should be not null and should have the same number of series");
        }
    }

    /**
     * Checks the validity of the dataset and renderer parameters.
     *
     * @param dataset  the category series dataset (cannot be null)
     * @param renderer the series renderer (cannot be null)
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     *                                  if the dataset number of items is different than the number of
     *                                  series renderers
     */
    private static void checkParameters(CategorySeries dataset, DefaultRenderer renderer) {
        if (dataset == null || renderer == null
                || dataset.getItemCount() != renderer.getSeriesRendererCount()) {
            throw new IllegalArgumentException(
                    "Dataset and renderer should be not null and the dataset number of items should be equal to the number of series renderers");
        }
    }

}
