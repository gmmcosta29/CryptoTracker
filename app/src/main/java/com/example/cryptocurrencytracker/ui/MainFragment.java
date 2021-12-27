package com.example.cryptocurrencytracker.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cryptocurrencytracker.CoinModel;
import com.example.cryptocurrencytracker.R;
import com.example.cryptocurrencytracker.SQLHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment {
    private String apiKey = "4a62b31f-a770-44b2-92c7-282fa03ebca4";
    private RecyclerView currencyRV;
    private EditText searchEdt;
    private ArrayList<CoinModel> currencyModalArrayList;
    private CoinAdapter currencyRVAdapter;
    private ProgressBar loadingPB;
    private SQLHelper sqlHelper;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static MainFragment newInstance() {
        return new MainFragment();
    }



    private void filter(String filter) {
        // on below line we are creating a new array list
        // for storing our filtered data.
        ArrayList<CoinModel> filteredlist = new ArrayList<>();
        // running a for loop to search the data from our array list.
        for (CoinModel item : currencyModalArrayList) {
            // on below line we are getting the item which are
            // filtered and adding it to filtered list.
            if (item.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredlist.add(item);
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





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        //getActivity().setContentView(R.layout.activity_main);

        searchEdt = (EditText) v.findViewById(R.id.idedtcurrency);
        sqlHelper = new SQLHelper(getActivity());
        // initializing all our variables and array list.
        loadingPB = (ProgressBar)v.findViewById(R.id.idpbloading);
        currencyRV = (RecyclerView)v.findViewById(R.id.idrvcurrency);
        currencyModalArrayList = new ArrayList<>();

        // initializing our adapter class.
        currencyRVAdapter = new CoinAdapter(currencyModalArrayList, this.getContext());

        // setting layout manager to recycler view.
        currencyRV.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // setting adapter to recycler view.
        currencyRV.setAdapter(currencyRVAdapter);

        // calling get data method to get data from API.
        getData();

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
                filter(s.toString());
            }
        });
        return v;
    }

        private void getData(){
            // creating a variable for storing our string.
            String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
            // creating a variable for request queue.
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            // making a json object request to fetch data from API.
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loadingPB.setVisibility(View.GONE);
                    try {
                        // extracting data from json.
                        JSONArray dataArray = response.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObj = dataArray.getJSONObject(i);
                            String symbol = dataObj.getString("symbol");
                            String name = dataObj.getString("name");
                            String id = dataObj.getString("id");
                            JSONObject quote = dataObj.getJSONObject("quote");
                            JSONObject USD = quote.getJSONObject("USD");
                            double price = USD.getDouble("price");
                            // adding all data to our array li st.
                            currencyModalArrayList.add(new CoinModel(name, symbol, price, Integer.valueOf(id)));
                        }
                        // notifying adapter on data change.
                        execWrite(currencyModalArrayList);
                        currencyRVAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
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
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    // in this method passing headers as
                    // key along with value as API keys.
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-CMC_PRO_API_KEY", apiKey); // API KEY
                    // at last returning headers
                    return headers;
                }
            };
            // calling a method to add our
            // json object request to our queue.
            queue.add(jsonObjectRequest);
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
        Toast.makeText(getActivity(), "Write into database completed", Toast.LENGTH_SHORT).show();

    }
}
