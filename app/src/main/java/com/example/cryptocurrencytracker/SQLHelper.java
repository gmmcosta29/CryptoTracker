package com.example.cryptocurrencytracker;


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
    private final Context context;



    public SQLHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null  , 4);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("QSLDatabase Created","Table Created");
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY , " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_SYMBOL + " TEXT, " +
                COLUMN_PRICE + " DOUBLE);";
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
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            String symbol = cursor.getString(cursor.getColumnIndex(COLUMN_SYMBOL));
            double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
            notes.add(new CoinModel(title,symbol,price, id));
        } while (cursor.moveToNext());
        cursor.close();
        return notes;
    }

    public void addCoins(ArrayList<CoinModel> coins){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (CoinModel coin: coins) {
            cv.put(COLUMN_ID,coin.getId());
            cv.put(COLUMN_NAME,coin.getName());
            cv.put(COLUMN_SYMBOL,coin.getSymbol());
            cv.put(COLUMN_PRICE,coin.getPrice());
            db.insert(TABLE_NAME,null,cv);
            db.update(TABLE_NAME, cv, COLUMN_ID + "=?", new String[]{String.valueOf(coin.getId())});
        }

    }

}
