package com.example.cryptocurrencytracker;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;


public class SQLHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "database.crypto";
    public static final String TABLE_NAME = "cryptoBD";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "coin_name";
    public static final String COLUMN_SYMBOL = "coin_symbol";
    public static final String COLUMN_PRICE = "coin_price";
    public static final String COLUMN_PERCENT = "coin_percent";
    public static final String COLUMN_IMG = "img_url";
    public static final String COLUMN_VOLUME24H = "coin_volume24h";
    private final Context context;



    public SQLHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null  , 4);
        this.context = context;

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.setVersion(oldVersion);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //onDowngrade(db, 5, 4);
        Log.i("QSLDatabase Created","Table Created");
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " TEXT PRIMARY KEY , " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_SYMBOL + " TEXT, " +
                COLUMN_IMG + " TEXT, " +
                COLUMN_PERCENT + " DOUBLE, " +
                COLUMN_VOLUME24H + " DOUBLE, " +
                COLUMN_PRICE + " DOUBLE);"
                ;
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("QSLDatabase Upgraded","Table Upgraded");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public ArrayList<CoinModel> getcoins(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME,null);
        cursor.moveToFirst();
        ArrayList<CoinModel> notes = new ArrayList<CoinModel>();
        if(!cursor.moveToFirst() || cursor.getCount() == 0){
            return notes;
        }
        do {
            @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            @SuppressLint("Range") String symbol = cursor.getString(cursor.getColumnIndex(COLUMN_SYMBOL));
            @SuppressLint("Range") String img_url = cursor.getString(cursor.getColumnIndex(COLUMN_IMG));
            @SuppressLint("Range") double percent_change = cursor.getDouble(cursor.getColumnIndex(COLUMN_PERCENT));
            @SuppressLint("Range") double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
            @SuppressLint("Range") double volume24h = cursor.getDouble(cursor.getColumnIndex(COLUMN_VOLUME24H));
            notes.add(new CoinModel(title,symbol,price, id,0, img_url,percent_change,volume24h));
        } while (cursor.moveToNext());

        cursor.close();
        return notes;
    }

    public void addCoins(ArrayList<CoinModel> coins){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        ArrayList<CoinModel> currentCoinsOnDB= getcoins();
        for (CoinModel coin: coins) {
            //if (currentCoinsOnDB!=null && !currentCoinsOnDB.contains(coin)) {
            cv.put(COLUMN_ID, coin.getId());
            cv.put(COLUMN_NAME, coin.getName());
            cv.put(COLUMN_SYMBOL, coin.getSymbol());
            cv.put(COLUMN_IMG, coin.getImage_url());
            cv.put(COLUMN_PERCENT, coin.getChange_percentage24h());
            cv.put(COLUMN_PRICE, coin.getPrice());
            cv.put(COLUMN_VOLUME24H,coin.getVolume24h());



            int idx = (int) db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            if (idx == -1) {
                db.update(TABLE_NAME, cv,  COLUMN_ID + "=?",  new String[]{String.valueOf(coin.getId())});  // number 1 is the _id here, update to variable for your code
            }



            //int res=db.update(TABLE_NAME, cv, COLUMN_ID + "=?", new String[]{String.valueOf(coin.getId())});

        }

    }


}

