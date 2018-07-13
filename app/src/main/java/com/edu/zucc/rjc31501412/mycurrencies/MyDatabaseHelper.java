package com.edu.zucc.rjc31501412.mycurrencies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "currencies.db";
    private static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    //create table
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS record" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "forCode TEXT, " +
                "forAmount TEXT," +
                "homCode TEXT," +
                "homAmount TEXT," +
                "time TEXT)");
    }

    //if version is different,upgrade
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,int oldVersion,int newVersion) {
        sqLiteDatabase.execSQL("drop table if exists record");
    }
}