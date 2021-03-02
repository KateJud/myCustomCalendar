package com.example.ttt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {

    public static final String CREATE_EVENTS_TABLE = "create table " + DBStructure.EVENT_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBStructure.EVENT + " TEXT, " + DBStructure.TIME + " TEXT, " + DBStructure.DAY + " TEXT, " + DBStructure.MONTH + " TEXT, " + DBStructure.YEAR + " TEXT, " + DBStructure.NOTIFY + " TEXT)";

    public static final String DROP_EVENTS_TABLE = "DROP TABLE IF EXISTS " + DBStructure.EVENT_TABLE_NAME;

    public DBOpenHelper(@Nullable Context context) {
        super(context, DBStructure.DB_NAME, null, DBStructure.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_EVENTS_TABLE);
        onCreate(db);
    }


    public void SaveEvent(String event, String time, String day, String month, String year,String notify, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBStructure.EVENT, event);
        contentValues.put(DBStructure.TIME, time);
        contentValues.put(DBStructure.DAY, day);
        contentValues.put(DBStructure.MONTH, month);
        contentValues.put(DBStructure.YEAR, year);
        contentValues.put(DBStructure.NOTIFY, notify);

        db.insert(DBStructure.EVENT_TABLE_NAME, null, contentValues);
    }

    public Cursor ReadEvents(String data, SQLiteDatabase db) {
        String[] Projections = {DBStructure.EVENT, DBStructure.TIME, DBStructure.DAY, DBStructure.MONTH, DBStructure.YEAR};
        String Selection = DBStructure.DAY + "=?";
        String[] SelectionArgs = {data};
        return db.query(DBStructure.EVENT_TABLE_NAME, Projections, Selection, SelectionArgs, null, null, null);
    }

    public Cursor ReadIDEvents(String data, String event, String time, SQLiteDatabase db) {
        String[] Projections = {DBStructure.ID, DBStructure.NOTIFY};
        String Selection = DBStructure.DAY + "=? and " + DBStructure.EVENT + "=? and " + DBStructure.TIME + "=?";
        String[] SelectionArgs = {data, event, time};
        return db.query(DBStructure.EVENT_TABLE_NAME, Projections, Selection, SelectionArgs, null, null, null);
    }

    public Cursor ReadEventsMonth(String month, String year, SQLiteDatabase db) {
        String[] Projections = {DBStructure.EVENT, DBStructure.TIME, DBStructure.DAY, DBStructure.MONTH, DBStructure.YEAR};
        String Selection = DBStructure.MONTH + "=? and " + DBStructure.YEAR + "=?";
        String[] SelectionArgs = {month, year};
        return db.query(DBStructure.EVENT_TABLE_NAME, Projections, Selection, SelectionArgs, null, null, null);
    }

    public void deleteEvent(String event, String date, String time, SQLiteDatabase db) {
        String selection = DBStructure.EVENT + "=? and " + DBStructure.DAY + "=? and " + DBStructure.TIME + "=?";
        String[] selectionArg = {event, date, time};
        db.delete(DBStructure.EVENT_TABLE_NAME, selection, selectionArg);

    }

    public void updateEvent(String data, String event, String time, String notify, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBStructure.NOTIFY, notify);
        String Selection = DBStructure.DAY + "=? and " + DBStructure.EVENT + "=? and " + DBStructure.TIME + "=?";
        String[] SelectionArgs = {data, event, time};
        db.update(DBStructure.EVENT_TABLE_NAME, contentValues, Selection, SelectionArgs);


    }
}
