package com.swift.io.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database manager, creates the database and and required tables
 * @author Rob Powell
 * @version 1.0
 */
public class DbManager extends SQLiteOpenHelper {

    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_ACCOUNT_NAME = "account_name";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_MOB_NUMBER = "mobile_number";
    public static final String COLUMN_NETWORK = "network";

    private static final String DATABASE_NAME = "icc.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_ACCOUNTS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_MOB_NUMBER
            + " text not null, " + COLUMN_ACCOUNT_NAME + " text not null, "+ COLUMN_PASSWORD
            + " text not null, "+ COLUMN_NETWORK + " text not null, "+COLUMN_TIMESTAMP+" long);";

    public DbManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
