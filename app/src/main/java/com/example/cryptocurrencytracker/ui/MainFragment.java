package com.example.cryptocurrencytracker.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.cryptocurrencytracker.ChatFragment;
import com.example.cryptocurrencytracker.CoinModel;
import com.example.cryptocurrencytracker.R;
import com.example.cryptocurrencytracker.SQLHelper;
import com.example.cryptocurrencytracker.ethereum.BlockchainFragment_WalletManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment {
    private final String apiKey = "a968fb35-1b88-48bf-aaf9-26a57006a30e";
    private ArrayList<CoinModel> currencyModalArrayList;
    private CoinAdapter currencyRVAdapter;
    private SQLHelper sqlHelper;
    private boolean checkFavorite;
    private final String CHANNEL_ID = "1";


    ViewModel viewmodal;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static MainFragment newInstance() {
        return new MainFragment();
    }


    private void filter(String filter,boolean checked) {
        ArrayList<CoinModel> filteredlist = new ArrayList<>();
        for (CoinModel item : currencyModalArrayList) {
            if(checked){
                if (item.isFavorite() && item.getName().toLowerCase().contains(filter.toLowerCase())) {
                    filteredlist.add(item);
                }
            }else{
                if (item.getName().toLowerCase().contains(filter.toLowerCase())) {
                    filteredlist.add(item);
                }
            }

        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(getActivity(), "No currency found..", Toast.LENGTH_SHORT).show();

        }
        currencyRVAdapter.filterList(filteredlist);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.chat_open_btn,menu);
        menuInflater.inflate(R.menu.wallet_btn,menu);

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.chat_open){

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, ChatFragment.newInstance());
            ft.commit();

            return true;
        }
        else {
            if ( item.getItemId()==R.id.wallet_btns){
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, BlockchainFragment_WalletManager.newInstance());
                ft.commit();
            }
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);


        viewmodal=new ViewModelProvider(requireActivity()).get(ViewModel.class);
        checkFavorite = false;
        EditText searchEdt = (EditText) v.findViewById(R.id.idedtcurrency);

        sqlHelper = new SQLHelper(getActivity());
        ProgressBar loadingPB = (ProgressBar) v.findViewById(R.id.idpbloading);
        RecyclerView currencyRV = (RecyclerView) v.findViewById(R.id.idrvcurrency);
        CheckBox cb_Favorite_Filter = (CheckBox) v.findViewById(R.id.checkBox_activateFav_filter);

        currencyModalArrayList = new ArrayList<>();

        if (viewmodal.getCoinsVM().getValue() !=null && !viewmodal.getCoinsVM().getValue().isEmpty()){
            currencyModalArrayList= (ArrayList<CoinModel>) viewmodal.getCoinsVM().getValue();
        }

        currencyRVAdapter = new CoinAdapter(currencyModalArrayList, this.getContext());

        currencyRV.setLayoutManager(new LinearLayoutManager(this.getContext()));

        currencyRV.setAdapter(currencyRVAdapter);

        final Handler handler = new Handler();
        final int delay = 12000; // 1000 milliseconds == 1 second
        getData();

        handler.postDelayed(new Runnable() {
            public void run() {
                if (isVisible()){
                    getData();
                    handler.postDelayed(this, delay);

                }

            }
        }, delay);


        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                filter(s.toString(),checkFavorite);
            }
        });

        currencyRVAdapter.setOnItemClickListener(new CoinAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //Toast.makeText(getContext(), "button working", Toast.LENGTH_SHORT).show();
                viewmodal.setChoice(currencyModalArrayList.get(position));
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, new FragmentCoinDetails());
                ft.commit();
            }

            @Override
            public void iconImageViewOnClick(View v, int position) {
            }

        });



        cb_Favorite_Filter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //filter_by_Favorite();
            checkFavorite = isChecked;
            filter("",checkFavorite);
        });
        return v;
    }

        private void getData(){
            //String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
            String url ="https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=15&page=1&sparkline=false&price_change_percentage=24h";
                    //"https://api.coingecko.com/api/v3/coins/bitcoin/market_chart?vs_currency=usd&days=4&interval=daily" ;

            RequestQueue queue = Volley.newRequestQueue(getActivity());

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
                //loadingPB.setVisibility(View.GONE);
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject dataObj = response.getJSONObject(i);
                        String symbol = dataObj.getString("symbol").toUpperCase();
                        String name = dataObj.getString("name");
                        String id = dataObj.getString("id");
                        String img_url = dataObj.getString("image");
                        double volume24h = dataObj.getDouble("total_volume");
                        double price = dataObj.getDouble("current_price");//USD.getDouble("price");
                        double percent_change24h = dataObj.getDouble("price_change_percentage_24h");

                        if (currencyModalArrayList.size() < response.length()) {
                            //Toast.makeText(getActivity(), "creating new coin on the list", Toast.LENGTH_SHORT).show();
                            currencyModalArrayList.add(new CoinModel(name, symbol, price, id, 0, img_url, percent_change24h, volume24h));
                        }else{
                            //Toast.makeText(getActivity(), "updating coing", Toast.LENGTH_SHORT).show();
                            for (int j=0;j<currencyModalArrayList.size(); j++){

                                if (currencyModalArrayList.get(j).getId().compareTo(id)==0){
                                    currencyModalArrayList.get(j).setPrice(price);
                                    currencyModalArrayList.get(j).setChange_percentage24h(percent_change24h);
                                    currencyModalArrayList.get(j).setVolume24h(volume24h);

                                }
                            }
                        }



                    }
                    execWrite(currencyModalArrayList);
                    currencyRVAdapter.notifyDataSetChanged();
                    viewmodal.setStateData(currencyModalArrayList);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Something went wrong parsing the data from the API. Please try again later", Toast.LENGTH_SHORT).show();

                }
            }, error -> {
                // displaying error response when received any error.
                execRead();

                Toast.makeText(getActivity(), "Something went wrong, so it will load offline Database", Toast.LENGTH_SHORT).show();

            });

            queue.add(jsonArrayRequest);
        }

    public void execRead(){
        executor.execute(()-> {
            ArrayList<CoinModel> myData;

            myData = sqlHelper.getcoins();

            ArrayList<CoinModel> finalMyData = myData;
            handler.post(() -> onComplete(finalMyData));
        });
    }

    public void onComplete(ArrayList<CoinModel> coins) {
        currencyModalArrayList.addAll(coins);
        currencyRVAdapter.notifyDataSetChanged();
        viewmodal.setStateData(currencyModalArrayList);
    }

    public void execWrite(ArrayList<CoinModel> coins){
        executor.execute(()-> {
            sqlHelper.addCoins(coins);
            handler.post(this::onWriteComplete);
        });

    }

    public void onWriteComplete() {
      //  Toast.makeText(getActivity(), "Write into database completed", Toast.LENGTH_SHORT).show();

    }
}
