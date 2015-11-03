package com.honeywell.firemanlocate.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by lynn.liu on 6/18/15.
 */
public class FiremanPosition implements Cloneable,Serializable {

    protected double mX;
    protected double mY;
    protected String mIndex;
    protected double mZ;

    public FiremanPosition() {
    }

    public FiremanPosition(double x, double y) {
        mX = x;
        mY = y;
    }

    public FiremanPosition(double mX, double mY, String mIndex) {
        this.mIndex = mIndex;
        this.mY = mY;
        this.mX = mX;
    }
    public FiremanPosition(double mX, double mY, double mZ) {
        this.mZ = mZ;
        this.mY = mY;
        this.mX = mX;
    }
    public FiremanPosition(double mX, double mY, double mZ,String mIndex) {
        this.mZ = mZ;
        this.mY = mY;
        this.mX = mX;
        this.mIndex = mIndex;
    }
    @Override
    public FiremanPosition clone() {
        FiremanPosition clone = null;
        try {
            clone = (FiremanPosition) super.clone();

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // won't happen
        }

        return clone;
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

    public String getmIndex() {
        return mIndex;
    }

    public void setmIndex(String mIndex) {
        this.mIndex = mIndex;
    }

    public double getZ() {
        return mZ;
    }

    public void setZ(double mZ) {
        this.mZ = mZ;
    }
}
