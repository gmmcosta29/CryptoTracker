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

import java.io.File;
import java.math.BigDecimal;
import java.security.Provider;
import java.security.Security;
import java.util.Random;

public class BlockchainFragment2_Transactions extends Fragment {
    Web3j web3j;
    File file;
    String wallet_title;
    ProgressBar pb;
    Info_shareVM_util info;
    EditText edit_amount_ether,addressReceiver;
    TextView tv_connectWalletAddyDisplay;
    ViewModel vm;
    private final String CHANNEL_ID = "1";

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
        //Infura key Api

        setupBouncyCastle();
        pb = (ProgressBar)v.findViewById(R.id.trx_progressBar);
        pb.setVisibility(View.GONE);
        //
        info=vm.getInfo().getValue();
        tv_connectWalletAddyDisplay= v.findViewById(R.id.textView_connected_to);
        tv_connectWalletAddyDisplay.setText("Connect to: "+info.getAddress());
        System.out.println("\n THE ADDRESS Is:"+info.getAddress());
        Button button_retrieveBalance = (Button)v.findViewById(R.id.btn_getBalanceAcc);
        button_retrieveBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAccountBalance();
            }
        });
        //MAKE TRX
        edit_amount_ether= v.findViewById(R.id.edt_eth_to_send);
        addressReceiver= v.findViewById(R.id.edt_receiverAddy);
        Button button_makeTrx = (Button)v.findViewById(R.id.btn_connectWalletFromJson);
        button_makeTrx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(" HERE trxe");
                try {
                    double eth_amount =Double.parseDouble(edit_amount_ether.getText().toString());
                    makeTransaction(eth_amount,addressReceiver.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            BigDecimal bigdec = new BigDecimal(balanceWei.getBalance());
            //System.out.println("balance:"+bigdec.movePointLeft(18));
            TextView balance_tv=getView().findViewById(R.id.tv_showAccountBalance);
            //System.out.println(Convert.fromWei(balanceWei.getBalance().toString(), Convert.Unit.ETHER));
            balance_tv.setText("ETH Balance: "+Convert.fromWei(balanceWei.getBalance().toString(), Convert.Unit.ETHER));
        }
        catch (Exception e){
            Toast.makeText(getActivity(), "Balance request failed!", Toast.LENGTH_LONG).show();

        }

    }


    //make a trx to another ETH wallet; send ETH
    public void makeTransaction(double ethToSend, String addyToSend) throws Exception {
        // get the amout of eth value the user wants to send
        //credentials=loadWalletFromFile();


        try{
            new TaskSendTransactionOnEthereum().execute(addyToSend,String.valueOf(ethToSend));
            //TransactionReceipt receipt =
            //Toast.makeText(getContext(), "Transaction successful: " +receipt.getTransactionHash(), Toast.LENGTH_LONG).show();
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
            // Web3j will set up a provider  when it's used for the first time.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            return;
        }
        //There is a possibility  the bouncy castle registered by android may not have all ciphers
        //so we  substitute with the one bundled in the app.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    /*
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void checkStateOfSmartContract(){

        RemoteCall<BigInteger> customerBalance;
        Function function = new Function(
                "balanceOf",
                Arrays.asList(new Address(<address_of_the_account>,
                        new ArrayList<>());
        String encodedFunction = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
        Transaction.createEthCallTransaction(<not_sure_what_the _FROM_address_is>, <not_sure_what_the_TO_address_is>, encodedFunction),
        DefaultBlockParameterName.LATEST)
            .sendAsync().get();

        List<Type> result = FunctionReturnDecoder.decode(
                response.getValue(), function.getOutputParameters());
    }

     */
    /**
     * ASYNC CLASS TO SEND TRANSACTION ON BACKGROUND... IT TAKES SOMETIME
     * ref:https://stackoverflow.com/questions/25647881/android-asynctask-example-and-explanation/25647882#25647882
     */
    private class TaskSendTransactionOnEthereum extends AsyncTask<String, Integer, String> {
        //ProgressBar pb;
        //TransactionReceipt res;

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pb.setVisibility(View.VISIBLE);
            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            // get the string from params, which is an array
            String addy_to_send = params[0];
            System.out.println(addy_to_send + " PARAMS: + "+params);
            double ethamount = Double.parseDouble(params[1]);
            TransactionReceipt res = null;

            // Do something that takes a long time, for example:
            try {
                res=Transfer.sendFunds(web3j, info.getCred(),"0x229eC7EcF6E3e3219B01686F9dB7cbfdb6D8A03E", BigDecimal.valueOf(ethamount), Convert.Unit.ETHER).send();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return res.getBlockHash();
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pb.setVisibility(View.GONE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal)
                    .setContentText("Transaction successfully background HashCode: "+ result)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Random notification_id = new Random();
            ((MainActivity)getActivity()).getNotificationManager().notify(notification_id.nextInt(100), builder.build());
            // Do things like hide the progress bar or change a TextView

        }
    }
}