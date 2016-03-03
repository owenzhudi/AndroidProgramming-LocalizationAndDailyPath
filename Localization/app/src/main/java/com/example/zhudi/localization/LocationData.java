package com.example.zhudi.localization;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhudi on 15/10/11.
 */
public class LocationData {
    private static LocationData mLocationData;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public LocationData(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new LocationBaseHelper(mContext).getReadableDatabase();
    }

    private LocationCursorWrapper queryLocations(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                LocationDbSchema.LocationTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new LocationCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(LocationRecord location) {
        ContentValues values = new ContentValues();
        values.put(LocationDbSchema.LocationTable.Cols.LATITUDE, location.getLatitude());
        values.put(LocationDbSchema.LocationTable.Cols.LONGITUDE, location.getLongitude());
        values.put(LocationDbSchema.LocationTable.Cols.TIME, location.getUpdateTime());

        return values;
    }

    public void addLocation(LocationRecord location) {
        ContentValues values = getContentValues(location);
        mDatabase.insert(LocationDbSchema.LocationTable.NAME, null, values);
    }



    public ArrayList<LocationRecord> getLocations() {
        ArrayList<LocationRecord> locations = new ArrayList<>();

        LocationCursorWrapper cursor = queryLocations(null, null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                locations.add(cursor.getLocation());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return locations;
    }




}
