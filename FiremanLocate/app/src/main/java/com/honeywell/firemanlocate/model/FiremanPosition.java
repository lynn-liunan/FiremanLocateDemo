package com.honeywell.firemanlocate.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lynn.liu on 6/18/15.
 */
public class FiremanPosition {

    private double mX;
    private double mY;

    public FiremanPosition() {
    }

    public FiremanPosition(double x, double y) {
        mX = x;
        mY = y;
    }

    public void setX(double mX) {
        this.mX = mX;
    }

    public void setY(double mY) {
        this.mY = mY;
    }

    public double getX() {
        return mX;
    }

    public double getY() {
        return mY;
    }
}
