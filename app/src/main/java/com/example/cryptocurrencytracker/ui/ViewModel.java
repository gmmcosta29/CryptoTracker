package com.example.cryptocurrencytracker.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cryptocurrencytracker.CoinModel;
import com.example.cryptocurrencytracker.ethereum.Info_shareVM_util;

import java.util.ArrayList;
import java.util.List;

public class ViewModel extends AndroidViewModel {

    private final MutableLiveData<CoinModel> choice = new MutableLiveData<>();


    private final MutableLiveData<Info_shareVM_util>info = new MutableLiveData<>();

    public final MutableLiveData<ArrayList<CoinModel>> stateData  = new MutableLiveData<>();


    public ViewModel(@NonNull Application application) {
        super(application);
    }


    public LiveData<ArrayList<CoinModel>> getCoinsVM() {

        return stateData;
    }
    public void setStateData(List<CoinModel> l){
        //stateData = (LiveData<List<CoinModel>>) l;
        ArrayList<CoinModel> coinTmpList = new ArrayList<>(l);
        stateData.setValue(coinTmpList);
    }

    public void setChoice(CoinModel i){
        choice.setValue(i);
    }


    public LiveData<CoinModel> getChoice() {
        return choice;
    }


    public LiveData<Info_shareVM_util> getInfo() {
        return info;
    }

    public void setInfo(Info_shareVM_util c) {
        this.info.setValue(c);
    }



}
