package com.example.cryptocurrencytracker;

import android.graphics.Bitmap;

public class CoinModel {
    // variable for currency name,
    // currency symbol and price.
    private String name;
    private String symbol;
    private double price;
    private double change_percentage24h,volume24h;
    private int int_id;
    private String id;
    private String image_url;
    private Bitmap image;


    private boolean isFavorite;
    public CoinModel(String name, String symbol, double price, String id,int int_id , String img,double percent,double volume24h) {
        //set up vars
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.id = id;
        this.int_id=int_id;
        this.image_url = img;
        this.change_percentage24h=percent;
        this.volume24h=volume24h;
        //
        isFavorite=false;

    }
    public CoinModel(String name, String symbol, double price, String id,int int_id , Bitmap img,double percent,double volume24h) {
        //set up vars
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.id = id;
        this.int_id=int_id;
        this.image = img;
        this.change_percentage24h=percent;
        this.volume24h=volume24h;
        //
        isFavorite=false;

    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getInt_id() {
        return int_id;
    }

    public void setInt_id(int int_id) {
        this.int_id = int_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage_url() {
        return image_url;
    }

    public double getChange_percentage24h() {
        return change_percentage24h;
    }

    public void setChange_percentage24h(double change_percentage24h) {
        this.change_percentage24h = change_percentage24h;
    }

    public double getVolume24h() {
        return volume24h;
    }

    public void setVolume24h(double volume24h) {
        this.volume24h = volume24h;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    /*
    public String toString() {

        return ("This coin named"+name+" with symbol:"+symbol+" // a price:"+String.valueOf(price)+" and id :"+ String.valueOf(id));
    }

     */
}