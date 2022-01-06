package com.example.cryptocurrencytracker.ethereum;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.cryptocurrencytracker.R;
import com.example.cryptocurrencytracker.ui.MainFragment;
import com.example.cryptocurrencytracker.ui.ViewModel;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.security.Provider;
import java.security.Security;

public class BlockchainFragment_WalletManager extends Fragment {
    Web3j web3j;
    File file;
    String wallet_title;
    Credentials credentials;
    TextView tv_address; //display user address when created
    ViewModel vm;
    String curr_add;
    ProgressBar mProgressBar;
    final String infura_API_key="https://rinkeby.infura.io/v3/6e76c3f2798547aab60d11de145d70fd";
    Info_shareVM_util infoToShare;
    public static BlockchainFragment_WalletManager newInstance() {
        return new BlockchainFragment_WalletManager();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.wallet_management_frag, container, false);

        setHasOptionsMenu(true);
        vm= new ViewModelProvider(requireActivity()).get(ViewModel.class);
        tv_address = (TextView)v.findViewById(R.id.tv_showAccountBalance);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        mProgressBar=(ProgressBar)v.findViewById(R.id.loadsimpleProgressBar);

        mProgressBar.setVisibility(View.GONE);

        setupBouncyCastle();
        Button createWalletBtn = (Button)v.findViewById(R.id.btn_getBalanceAcc);
        createWalletBtn.setOnClickListener(view -> createWallet(v));
        EditText title2_tmp  =(EditText) v.findViewById(R.id.walletTitle2); // Edit views for input name + password of wallet (load it)
        EditText pass2  = (EditText)v.findViewById(R.id.edt_receiverAddy);
        Button connectWalletBtn = (Button)v.findViewById(R.id.btn_connectWalletFromJson);
        connectWalletBtn.setOnClickListener(view -> {
            credentials=loadWalletFromFile(title2_tmp.getText().toString()  , pass2.getText().toString());
            if (credentials!=null) {
                vm.setInfo(new Info_shareVM_util(credentials.getAddress(), credentials,web3j));
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, new BlockchainFragment2_Transactions());
                ft.commit();
            }else{
                Toast.makeText(getContext(),"Error login", Toast.LENGTH_LONG).show();
            }
        });
        //INFURA API KEY ; to connect to an ETHEREUM node.
        web3j = Web3j.build(new HttpService(infura_API_key));
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                Toast.makeText(getContext(), "Connected", Toast.LENGTH_LONG).show();
            } else {
              //  Toast.makeText(getContext(), clientVersion.getError().getMessage(),Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
          //  Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return v;

    }

    /**
     * Override menu btns; back to mainfrag
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.back_btn,menu);
        //MenuItem searchMenuItem = menu.findItem(R.id.search);

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.back){
            // reset the choice
            vm.setInfo(null);

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, new MainFragment());
            ft.commit();

            return true;
        }
        return false;
    }
    /**
     * creation walle file; json
     * @param v
     */
    public void createWallet(View v)  { //NEW WALLET : 0x5040d9a580cb943414c24d03dd46e9dc85582d73 => path ; /data/user/0/com.example.cryptocurrencytracker/files/Test/wallet1
        EditText edt_title1_tmp  = v.findViewById(R.id.walletTitle1); // Edit views for input name + password of wallet : CREATION
        EditText edt_pass1  = v.findViewById(R.id.password1);
        String pass1= edt_pass1.getText().toString();
        String title1_tmp =edt_title1_tmp.getText().toString();

        //Wallet files will be stored in a folder created; each are named after the title
        file = new File(requireContext().getFilesDir()+"/" +title1_tmp);
        if (!file.exists() ) {
            file.mkdirs();
        }
        else {
            //Toast.makeText(getContext(), "Directory already created",Toast.LENGTH_LONG).show();

        }

        //
        try {
            // generating the etherium wallet
            wallet_title =WalletUtils.generateLightNewWalletFile(pass1,file); //WalletUtils.generateLightNewWalletFile(password, file);
            Toast.makeText(getContext(), "Wallet generated!", Toast.LENGTH_LONG).show();
            credentials = WalletUtils.loadCredentials(pass1, file + "/" + wallet_title);
            tv_address.setText("New Wallet Address: " + credentials.getAddress());

        }
        catch(Exception e){
          //  Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();

        }

    }

    public Credentials loadWalletFromFile(String input_titleWallet_path,String input_passWallet){

        try {
            Credentials res_cred;  //getContext().getFilesDir()
            File folder = new File(requireContext().getFilesDir()+"/"+input_titleWallet_path);
            File[] listOfFiles = folder.listFiles();

            file = listOfFiles[0]; //get first element of folder (only element there ) , this is our wallet file

            String path_wallet_complete=getContext().getFilesDir()+"/"+input_titleWallet_path+"/"+file.getName();
            res_cred = WalletUtils.loadCredentials(input_passWallet, path_wallet_complete );//"/data/user/0/com.example.cryptocurrencytracker/files/Test/wallet1");

            return res_cred;
        }catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    //set up the security provider
    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            return;
        }
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }




}

