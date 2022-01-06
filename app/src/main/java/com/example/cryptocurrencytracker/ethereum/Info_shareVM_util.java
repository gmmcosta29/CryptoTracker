package com.example.cryptocurrencytracker.ethereum;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

public class Info_shareVM_util {

    private String address;
    private Credentials cred;
    private Web3j web3j;

    public Info_shareVM_util(String ad , Credentials c,Web3j w){
        address = ad;
        cred=c;
        web3j=w;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Credentials getCred() {
        return cred;
    }

    public void setCred(Credentials cred) {
        this.cred = cred;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }
}
