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
import android.widget.CompoundButton;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.cryptocurrencytracker.ChatFragment;
import com.example.cryptocurrencytracker.CoinModel;
import com.example.cryptocurrencytracker.R;
import com.example.cryptocurrencytracker.SQLHelper;
import com.example.cryptocurrencytracker.ethereum.BlockchainFragment_WalletManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment {
    private String apiKey = "a968fb35-1b88-48bf-aaf9-26a57006a30e";
    private RecyclerView currencyRV;
    private EditText searchEdt;
    private ArrayList<CoinModel> currencyModalArrayList;
    private CoinAdapter currencyRVAdapter;
    private ProgressBar loadingPB;
    private SQLHelper sqlHelper;
    private CheckBox cb_Favorite_Filter;
    private boolean checkFavorite;
    private final String CHANNEL_ID = "1";


    ViewModel viewmodal;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static MainFragment newInstance() {
        return new MainFragment();
    }


    private void filter(String filter,boolean checked) {
        // on below line we are creating a new array list
        // for storing our filtered data.
        ArrayList<CoinModel> filteredlist = new ArrayList<>();
        // running a for loop to search the data from our array list.
        for (CoinModel item : currencyModalArrayList) {
            // on below line we are getting the item which are
            // filtered and adding it to filtered list.
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
        // on below line we are checking
        // weather the list is empty or not.
        if (filteredlist.isEmpty()) {
            // if list is empty we are displaying a toast message.
            Toast.makeText(getActivity(), "No currency found..", Toast.LENGTH_SHORT).show();
        } else {
            // on below line we are calling a filter
            // list method to filter our list.
            currencyRVAdapter.filterList(filteredlist);
        }
    }
    private void filter_by_Favorite(String s,boolean checked) {
        // on below line we are creating a new array list
        // for storing our filtered data.
        ArrayList<CoinModel> filteredlist = new ArrayList<>();
        // running a for loop to search the data from our array list.
        for (CoinModel item : currencyModalArrayList) {
            // on below line we are getting the item which are
            // filtered and adding it to filtered list.

        }
        // on below line we are checking
        // weather the list is empty or not.
        if (filteredlist.isEmpty()) {
            // if list is empty we are displaying a toast message.
            Toast.makeText(getActivity(), "No currency found..", Toast.LENGTH_SHORT).show();
        } else {
            // on below line we are calling a filter
            // list method to filter our list.
            currencyRVAdapter.filterList(filteredlist);
        }
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.chat_open_btn,menu);
        menuInflater.inflate(R.menu.wallet_btn,menu);
        //MenuItem searchMenuItem = menu.findItem(R.id.search);

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


        //getActivity().setContentView(R.layout.activity_main);
        viewmodal=new ViewModelProvider(requireActivity()).get(ViewModel.class);
        checkFavorite = false;
        searchEdt = (EditText) v.findViewById(R.id.idedtcurrency);
        sqlHelper = new SQLHelper(getActivity());
        // initializing all our variables and array list.
        loadingPB = (ProgressBar)v.findViewById(R.id.idpbloading);
        currencyRV = (RecyclerView)v.findViewById(R.id.idrvcurrency);
        cb_Favorite_Filter = (CheckBox)v.findViewById(R.id.checkBox_activateFav_filter);

        currencyModalArrayList = new ArrayList<>();

        if (viewmodal.getCoinsVM().getValue() !=null && !viewmodal.getCoinsVM().getValue().isEmpty()){
            currencyModalArrayList= (ArrayList<CoinModel>) viewmodal.getCoinsVM().getValue();
            System.out.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz------------ >>zzz :"+ currencyModalArrayList);
            Toast.makeText(getContext(), "enter here", Toast.LENGTH_SHORT).show();
        }

        // initializing our adapter class.
        currencyRVAdapter = new CoinAdapter(currencyModalArrayList, this.getContext());

        // setting layout manager to recycler view.
        currencyRV.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // setting adapter to recycler view.
        currencyRV.setAdapter(currencyRVAdapter);

        // calling get data method to get data from API.
        final Handler handler = new Handler();
        final int delay = 12000; // 1000 milliseconds == 1 second
        getData();

        handler.postDelayed(new Runnable() {
            public void run() {
                //System.out.println("myHandler: here!"); // Do your work here
                if (isVisible()){
                    getData();
                    handler.postDelayed(this, delay);

                }

            }
        }, delay);


        // on below line we are adding text watcher for our
        // edit text to check the data entered in edittext.
        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // on below line calling a
                // method to filter our array list
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
                System.out.println(" Item clicked favrorite:"+position);
                //currencyModalArrayList.get(position).setFavorite(true);
                //currencyRVAdapter.notifyDataSetChanged();
                System.out.println("FAVORITE COIN?:"+currencyModalArrayList.get(position).isFavorite());
            }

        });



        cb_Favorite_Filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                System.out.println(" CHECKED ");
                //filter_by_Favorite();
                checkFavorite = isChecked;
                filter("",checkFavorite);
            }
        });
        return v;
    }

        private void getData(){
            // creating a variable for storing our string.
            //String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
            String url ="https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=15&page=1&sparkline=false&price_change_percentage=24h";
                    //"https://api.coingecko.com/api/v3/coins/bitcoin/market_chart?vs_currency=usd&days=4&interval=daily" ;
            // creating a variable for request queue.

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            // making a json object request to fetch data from API.

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    //loadingPB.setVisibility(View.GONE);
                    try {
                        System.out.println(" -----\n \n \n -------------------------------------------------------- REPSONDE"+response);
                        // extracting data from json.
                        System.out.println(" -----\n \n \n -----------LENGTH"+currencyModalArrayList.size());
                        JSONArray dataArray = response ;//.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObj = dataArray.getJSONObject(i);
                            String symbol = dataObj.getString("symbol").toUpperCase();
                            String name = dataObj.getString("name");
                            String id = dataObj.getString("id");
                            int int_id = i;//dataObj.getInt("id");
                            String img_url = dataObj.getString("image");
                            double volume24h = dataObj.getDouble("total_volume");
                            //JSONObject quote = dataObj.getJSONObject("quote");
                            //JSONObject USD = quote.getJSONObject("USD");
                            double price = dataObj.getDouble("current_price");//USD.getDouble("price");
                            double percent_change24h = dataObj.getDouble("price_change_percentage_24h");

                            if (currencyModalArrayList.size() < dataArray.length()) {
                                // adding all data to our array li st.
                                //Toast.makeText(getActivity(), "creating new coin on the list", Toast.LENGTH_SHORT).show();
                                currencyModalArrayList.add(new CoinModel(name, symbol, price, id, 0, img_url, percent_change24h, volume24h));
                            }else{
                                //Toast.makeText(getActivity(), "updating coing", Toast.LENGTH_SHORT).show();
                                for (int j=0;j<currencyModalArrayList.size(); j++){

                                    if (currencyModalArrayList.get(j).getId().compareTo(id)==0){
                                        currencyModalArrayList.get(j).setPrice(price);
                                        currencyModalArrayList.get(j).setChange_percentage24h(percent_change24h);
                                        currencyModalArrayList.get(j).setVolume24h(volume24h);
                                        /*
                                        if(percent_change24h > 0){

                                        }*/

                                    }
                                }
                            }



                        }
                        System.out.println(" -----\n \n \n -----------LENGTHBIS "+currencyModalArrayList.size());
                        // notifying adapter on data change.
                        execWrite(currencyModalArrayList);
                        currencyRVAdapter.notifyDataSetChanged();
                        viewmodal.setStateData(currencyModalArrayList);

                    } catch (Exception e) {
                        // handling json exception.
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Something went wrong parsing the data from the API. Please try again later", Toast.LENGTH_SHORT).show();

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // displaying error response when received any error.
                    execRead();

                    Toast.makeText(getActivity(), "Something went wrong, so it will load offline Database", Toast.LENGTH_SHORT).show();

                }
            });
                /*
                @Override
                public Map<String, String> getHeaders() {
                    // in this method passing headers as
                    // key along with value as API keys.
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-CMC_PRO_API_KEY", apiKey); // API KEY
                    // at last returning headers
                    return headers;
                }
                */

            // calling a method to add our
            // json object request to our queue.
            queue.add(jsonArrayRequest);
        }

    public void execRead(){
        executor.execute(()-> {
            ArrayList<CoinModel> myData = new ArrayList<>();

            myData = sqlHelper.getcoins();

            ArrayList<CoinModel> finalMyData = myData;
            handler.post(() -> {
                onComplete(finalMyData);
            });
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
            handler.post(() -> {
                onWriteComplete();
            });
        });

    }

    public void onWriteComplete() {
      //  Toast.makeText(getActivity(), "Write into database completed", Toast.LENGTH_SHORT).show();

    }
}
