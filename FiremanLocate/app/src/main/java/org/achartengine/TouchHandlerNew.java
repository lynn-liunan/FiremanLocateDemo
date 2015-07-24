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

import android.graphics.Point;
import android.graphics.RectF;
import android.view.MotionEvent;

import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.RoundChart;
import org.achartengine.chart.XYChart;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.tools.MyDegreeAdapter;
import org.achartengine.tools.Pan;
import org.achartengine.tools.PanListener;
import org.achartengine.tools.Zoom;
import org.achartengine.tools.ZoomListener;

/**
 * The main handler of the touch events.
 */
public class TouchHandlerNew implements ITouchHandler {
    /**
     * The chart renderer.
     */
    private DefaultRenderer mRenderer;
    /**
     * The old x coordinate.
     */
    private float oldX;
    /**
     * The old y coordinate.
     */
    private float oldY;
    /**
     * The old x2 coordinate.
     */
    private float oldX2;
    /**
     * The old y2 coordinate.
     */
    private float oldY2;
    /**
     * The zoom buttons rectangle.
     */
    private RectF zoomR = new RectF();
    /**
     * The pan tool.
     */
    private Pan mPan;
    /**
     * The zoom for the pinch gesture.
     */
    private Zoom mPinchZoom;
    /**
     * The graphical view.
     */
    private GraphicalView graphicalView;

    /**
     * Creates a new graphical view.
     *
     * @param view  the graphical view
     * @param chart the chart to be drawn
     */
    public TouchHandlerNew(GraphicalView view, AbstractChart chart) {
        graphicalView = view;
        zoomR = graphicalView.getZoomRectangle();
        if (chart instanceof XYChart) {
            mRenderer = ((XYChart) chart).getRenderer();
        } else {
            mRenderer = ((RoundChart) chart).getRenderer();
        }
        if (mRenderer.isPanEnabled()) {
            mPan = new Pan(chart);
        }
        if (mRenderer.isZoomEnabled()) {
            mPinchZoom = new Zoom(chart, true, 1);
        }
    }

    /**
     * Handles the touch event.
     *
     * @param event the touch event
     */
    public boolean handleTouch(MotionEvent event) {
        int action = event.getAction();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                calcDegree((int) event.getX(), (int) event.getY(), false);
                break;
            case MotionEvent.ACTION_MOVE:
                calcDegree((int) event.getX(), (int) event.getY(), false);
                break;
            case MotionEvent.ACTION_UP:
                calcDegree((int) event.getX(), (int) event.getY(), true);
                break;
        }
        return !mRenderer.isClickEnabled();
    }


    /**
     * @param x
     * @param y
     * @param flag			是否校正指针角度（ACTION_UP 时要校正）
     *
     */
    public void calcDegree(int x, int y, boolean flag){
        int rx = x - (graphicalView.getLeft() + graphicalView.getWidth() / 2);
        int ry = - (y - (graphicalView.getTop() + graphicalView.getHeight() / 2));

        Point point = new Point(rx, ry);

        int rotateDegree = MyDegreeAdapter.GetRadianByPos(point);
        graphicalView.setRotateDegree(rotateDegree);
    }

    private void applyZoom(float zoomRate, int axis) {
        zoomRate = Math.max(zoomRate, 0.9f);
        zoomRate = Math.min(zoomRate, 1.1f);
        if (mPinchZoom != null && zoomRate > 0.9 && zoomRate < 1.1) {
            mPinchZoom.setZoomRate(zoomRate);
            mPinchZoom.apply(axis);
        }
    }

    /**
     * Adds a new zoom listener.
     *
     * @param listener zoom listener
     */
    public void addZoomListener(ZoomListener listener) {
        if (mPinchZoom != null) {
            mPinchZoom.addZoomListener(listener);
        }
    }

    /**
     * Removes a zoom listener.
     *
     * @param listener zoom listener
     */
    public void removeZoomListener(ZoomListener listener) {
        if (mPinchZoom != null) {
            mPinchZoom.removeZoomListener(listener);
        }
    }

    /**
     * Adds a new pan listener.
     *
     * @param listener pan listener
     */
    public void addPanListener(PanListener listener) {
        if (mPan != null) {
            mPan.addPanListener(listener);
        }
    }

    /**
     * Removes a pan listener.
     *
     * @param listener pan listener
     */
    public void removePanListener(PanListener listener) {
        if (mPan != null) {
            mPan.removePanListener(listener);
        }
    }
}