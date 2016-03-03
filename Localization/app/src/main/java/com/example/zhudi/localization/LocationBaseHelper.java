package com.example.zhudi.localization;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhudi on 15/10/11.
 */
public class LocationBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "locationBase.db";

    public LocationBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + LocationDbSchema.LocationTable.NAME + "(" +
            "_id integer primary key autoincrement, " +
                LocationDbSchema.LocationTable.Cols.LATITUDE + " DOUBLE NOT NULL, " +
                LocationDbSchema.LocationTable.Cols.LONGITUDE + " DOUBLE NOT NULL, " +
                LocationDbSchema.LocationTable.Cols.TIME  + " TEXT NOT NULL " +
                 ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF IT EXIST CONTACTS");
        this.onCreate(db);
    }
}
