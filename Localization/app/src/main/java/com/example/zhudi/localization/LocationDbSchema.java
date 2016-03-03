package com.example.zhudi.localization;

/**
 * Created by zhudi on 15/10/11.
 */
public class LocationDbSchema {
    public static final class LocationTable {
        public static final String NAME="locations";

        public static final class Cols {
            //public static final String ID = "id";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String TIME = "time";
        }
    }
}
