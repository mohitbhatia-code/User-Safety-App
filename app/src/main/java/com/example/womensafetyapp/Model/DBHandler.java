package com.example.womensafetyapp.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="receiversMobileNumbersDatabase";
    private static final int DATABASE_VERSION=1;
    private static final String TABLE_NAME="receiverMobile";
    private static final String MOBILE_KEY="mobile";

    public DBHandler(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createDBSQL="create table "+TABLE_NAME +"( "+MOBILE_KEY +" text primary key"+" )";
        db.execSQL(createDBSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+TABLE_NAME);
        onCreate(db);
    }

    public void addMobile(String mobile){
        SQLiteDatabase database=getWritableDatabase();
        String addMobileCommand="insert into "+TABLE_NAME+
                " values('"+ mobile+"')";
        database.execSQL(addMobileCommand);
        database.close();
    }

    public void deleteMobile(String mobile){
        SQLiteDatabase database=getWritableDatabase();
        String deleteMobileCommand="delete from "+TABLE_NAME+
                " where "+MOBILE_KEY +" = "+mobile;
        database.execSQL(deleteMobileCommand);
        database.close();
    }

    public ArrayList<String> returnAllMobileNumbers(){
        SQLiteDatabase database=getWritableDatabase();
        String sqlQuery= "select * from "+TABLE_NAME;
        Cursor cursor=database.rawQuery(sqlQuery,null);

        ArrayList<String> mobileNumbers=new ArrayList<>();
        while(cursor.moveToNext()){
            mobileNumbers.add(cursor.getString(0));
        }
        return mobileNumbers;
    }

}
