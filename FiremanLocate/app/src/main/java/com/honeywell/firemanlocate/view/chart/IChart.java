package com.honeywell.firemanlocate.view.chart;

import android.content.Context;
import android.content.Intent;

/**
 * Created by lynn.liu on 6/16/15.
 */
public interface IChart {
    /**
     * A constant for the name field in a list activity.
     */
    String NAME = "name";
    /**
     * A constant for the description field in a list activity.
     */
    String DESC = "desc";

    /**
     * Returns the chart name.
     *
     * @return the chart name
     */
    String getName();

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    String getDesc();

    /**
     * Executes the chart demo.
     *
     * @param context the context
     * @return the built intent
     */
    Intent execute(Context context);
}
