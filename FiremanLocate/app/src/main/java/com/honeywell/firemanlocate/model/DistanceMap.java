package com.honeywell.firemanlocate.model;

import java.util.TreeMap;

/**
 * Created by Vincent on 5/8/15.
 */
public class DistanceMap {
    private static TreeMap distanceMap;

    public static TreeMap getDistanceMap() {
        return distanceMap;
    }

    public static void setDistanceMap(TreeMap distanceMap) {
        DistanceMap.distanceMap = distanceMap;
    }

}
