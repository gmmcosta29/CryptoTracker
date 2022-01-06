package com.example.cryptocurrencytracker.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.cryptocurrencytracker.CoinModel;
import com.example.cryptocurrencytracker.ethereum.Info_shareVM_util;

import org.web3j.crypto.Credentials;

import java.util.ArrayList;
import java.util.List;

public class ViewModel extends AndroidViewModel {

    private MutableLiveData<CoinModel> choice = new MutableLiveData<>();


    //Ethereum shared data between frags:
    private MutableLiveData<Info_shareVM_util>info = new MutableLiveData<>();
    //private NoteRepository repository;

    //
    // live data where all the notes are
    //private LiveData<List<Ent_Note>> allCourses;

    public MutableLiveData<ArrayList<CoinModel>> stateData  = new MutableLiveData<>();


    public ViewModel(@NonNull Application application) {
        super(application);
    }


    public LiveData<ArrayList<CoinModel>> getCoinsVM() {

        return stateData;
    }
    public void setStateData(List<CoinModel> l){
        //stateData = (LiveData<List<CoinModel>>) l;
        ArrayList<CoinModel> coinTmpList = new ArrayList<>();
        for (CoinModel c:l){
            coinTmpList.add(c);
        }
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
