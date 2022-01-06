package com.example.cryptocurrencytracker.ethereum;

import android.os.AsyncTask;
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
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.cryptocurrencytracker.MainActivity;
import com.example.cryptocurrencytracker.R;
import com.example.cryptocurrencytracker.ui.MainFragment;
import com.example.cryptocurrencytracker.ui.ViewModel;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.security.Provider;
import java.security.Security;
import java.util.Random;

public class BlockchainFragment2_Transactions extends Fragment {
    Web3j web3j;
    // --Commented out by Inspection (06/01/22, 20:18):File file;
    // --Commented out by Inspection (06/01/22, 20:18):String wallet_title;
    ProgressBar pb;
    Info_shareVM_util info;
    EditText edit_amount_ether,addressReceiver;
    TextView tv_connectWalletAddyDisplay;
    ViewModel vm;

    public static BlockchainFragment2_Transactions newInstance() {
        return new BlockchainFragment2_Transactions();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.blockchain_frag, container, false);
        vm= new ViewModelProvider(requireActivity()).get(ViewModel.class);

        setHasOptionsMenu(true);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setupBouncyCastle();
        pb = (ProgressBar)v.findViewById(R.id.trx_progressBar);
        pb.setVisibility(View.GONE);
        //
        info=vm.getInfo().getValue();
        tv_connectWalletAddyDisplay= v.findViewById(R.id.textView_connected_to);
        tv_connectWalletAddyDisplay.setText("Connect to: "+info.getAddress());
        Button button_retrieveBalance = (Button)v.findViewById(R.id.btn_getBalanceAcc);
        button_retrieveBalance.setOnClickListener(view -> getAccountBalance());
        //MAKE TRX
        edit_amount_ether= v.findViewById(R.id.edt_eth_to_send);
        addressReceiver= v.findViewById(R.id.edt_receiverAddy);
        Button button_makeTrx = (Button)v.findViewById(R.id.btn_connectWalletFromJson);
        button_makeTrx.setOnClickListener(view -> {
            try {
                double eth_amount = Double.parseDouble(edit_amount_ether.getText().toString());
                if(addressReceiver.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Please enter text in the", Toast.LENGTH_LONG).show();

                }else{
                    makeTransaction(eth_amount,addressReceiver.getText().toString());

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/6e76c3f2798547aab60d11de145d70fd"));
        web3j= info.getWeb3j();
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion()
                    .sendAsync().get();
            if (!clientVersion.hasError()) {
                Toast.makeText(getActivity(), "Connected", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), clientVersion.getError().getMessage(),Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return v;

    }

    /**
     * Overrides menu options
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
     * get balance from own account
     */
    public void getAccountBalance ()  {
        //get wallet's balance

        try {
            //"0x5040d9a580cb943414c24d03dd46e9dc85582d73"
            //System.out.println(" ADD : "+vm.getInfo().getValue().getAddress());
            EthGetBalance balanceWei = web3j.ethGetBalance(vm.getInfo().getValue().getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            //BigDecimal bigdec = new BigDecimal(balanceWei.getBalance());
            //System.out.println("balance:"+bigdec.movePointLeft(18));
            TextView balance_tv=getView().findViewById(R.id.tv_showAccountBalance);
            //System.out.println(Convert.fromWei(balanceWei.getBalance().toString(), Convert.Unit.ETHER));
            balance_tv.setText("ETH Balance: "+Convert.fromWei(balanceWei.getBalance().toString(), Convert.Unit.ETHER));
        }
        catch (Exception e){
            Toast.makeText(getActivity(), "Balance request failed!", Toast.LENGTH_LONG).show();

        }

    }


    public void makeTransaction(double ethToSend, String addyToSend) throws Exception {
        //credentials=loadWalletFromFile();


        try{
            new TaskSendTransactionOnEthereum().execute(addyToSend,String.valueOf(ethToSend));
            Toast.makeText(getActivity(), "Transaction sent! ", Toast.LENGTH_LONG).show();
        }
        catch(Exception e){
            Toast.makeText(getActivity(), "Low balance", Toast.LENGTH_LONG).show();

        }
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

 class TaskSendTransactionOnEthereum extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String addy_to_send = params[0];
            double ethamount = Double.parseDouble(params[1]);
            TransactionReceipt res = null;

            try {
                res=Transfer.sendFunds(web3j, info.getCred(),"0x229eC7EcF6E3e3219B01686F9dB7cbfdb6D8A03E", BigDecimal.valueOf(ethamount), Convert.Unit.ETHER).send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(res != null){
                return res.getBlockHash();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pb.setVisibility(View.GONE);
            String CHANNEL_ID = "1";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal)
                    .setContentText("Transaction successfully background HashCode: "+ result)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Random notification_id = new Random();
            ((MainActivity)getActivity()).getNotificationManager().notify(notification_id.nextInt(100), builder.build());

        }
    }
}