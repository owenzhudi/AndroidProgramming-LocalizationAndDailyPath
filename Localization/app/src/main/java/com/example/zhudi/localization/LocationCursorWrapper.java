package com.example.zhudi.localization;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.location.Location;

/**
 * Created by zhudi on 15/10/14.
 */
public class LocationCursorWrapper extends CursorWrapper {
    public LocationCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public LocationRecord getLocation() {
        double latitude = getDouble(getColumnIndex(LocationDbSchema.LocationTable.Cols.LATITUDE));
        double longitude = getDouble(getColumnIndex(LocationDbSchema.LocationTable.Cols.LONGITUDE));
        String updateTime = getString(getColumnIndex(LocationDbSchema.LocationTable.Cols.TIME));

        LocationRecord locationRecord = new LocationRecord(latitude, longitude, updateTime);
        return locationRecord;
    }
}
