package com.example.cryptocurrencytracker.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cryptocurrencytracker.R;

import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;


public class FragmentCoinDetails extends Fragment {
    ViewModel vm;

    String allText= "";
    TextView tv_symbol_coin;
    TextView tv_name_coin, tv_price,tv_market_cap, tv_volume24h;
    //line chart
    LineChart mpLineChart;
    LineDataSet lineDataSet;
    LineData lineData;
    ArrayList<Entry> data_entries;
    View myView;
    RadioGroup radiogroup;
    String id_coin_for_resquest;

    public FragmentCoinDetails() {
        setHasOptionsMenu(true);
    }

    public static FragmentCoinDetails newInstance() {
        return new FragmentCoinDetails();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag2, container, false);
        vm= new ViewModelProvider(requireActivity()).get(ViewModel.class);
        myView=v;
        mpLineChart = myView.findViewById(R.id.reportingChart);
        setHasOptionsMenu(true);
        //getData();
        tv_symbol_coin = (TextView) v.findViewById(R.id.tv_symbol);
        tv_name_coin = (TextView) v.findViewById(R.id.tv_fullNameCoin);
        tv_price = (TextView) v.findViewById(R.id.tv_price);
        tv_market_cap = (TextView) v.findViewById(R.id.tv_marketcap);
        tv_volume24h = (TextView) v.findViewById(R.id.tv_volume24h);
        //RadioGroup (with 3 options)
        radiogroup= v.findViewById(R.id.radio_grp_time);

        //Choosing a timeline for the chart ; and choosing nb of days ; using radio group buttons;
        //default values start: hourly
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String res_time_in_days="";
                switch (radiogroup.getCheckedRadioButtonId()) {
                    case R.id.hourly_time:
                        res_time_in_days = "1";
                        configureLineChart("HH:mm");

                        break;
                    case R.id.daily_time:
                        res_time_in_days = "7";
                        System.out.println("kek");
                        configureLineChart("dd-MM");

                        break;

                    case R.id.weekly_time:
                        res_time_in_days = "30";
                        configureLineChart("dd-MM");
                        break;
                    case R.id.monthly_time:
                        res_time_in_days = "360";
                        configureLineChart("MM-yyyy");
                        break;
                    case R.id.max_time:
                        res_time_in_days = "max";
                        configureLineChart("MM-yyyy");
                        break;
                }
                getData_from_geckoAPI(res_time_in_days);
            }
        });
        //Get the choice from view model ; item selected by the user and init the text views
        if (vm.getChoice().getValue() != null){
            System.out.println(vm.getChoice().getValue().toString());
            tv_symbol_coin.setText(vm.getChoice().getValue().getSymbol());
            tv_name_coin.setText(vm.getChoice().getValue().getName());
            id_coin_for_resquest=vm.getChoice().getValue().getId();
            Object tmp = Math.round(vm.getChoice().getValue().getPrice() *100) /100;
            tv_price.setText(String.valueOf(tmp));
            //tv_market_cap.setText(vm.getChoice().getValue().());
            tv_volume24h.setText(String.valueOf(vm.getChoice().getValue().getVolume24h()));
        }


        //making first API CALL to get data for default choice of timeline chart; hourly: ; the rest is updated by user choice buttons (radiogrp)
        configureLineChart("HH:mm");
        getData_from_geckoAPI("1");
        return v;

    }




    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.notifs_btn,menu);
        menuInflater.inflate(R.menu.back_btn,menu);
        //MenuItem searchMenuItem = menu.findItem(R.id.search);

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.back){
            // reset the choice
            vm.setChoice(null);

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, new MainFragment());
            ft.commit();

            return true;
        }
        else{
            if(item.getItemId() == R.id.notifs){
                // reset the choice
                //getData_from_geckoAPI();
                /*
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, new BlockchainFragment());
                ft.commit();
                    */
                return true;
            }
        }
        return false;
    }

    private void getData_from_geckoAPI(String time_choice_in_days){

        // creating a variable for storing our string.
        String url = "https://api.coingecko.com/api/v3/coins/"+id_coin_for_resquest+"/market_chart?vs_currency=usd&days="+time_choice_in_days;
        //String url = "https://api.coingecko.com/api/v3/coins/"+id_coin_for_resquest+"/market_chart?vs_currency=usd&days=4&interval=daily";
        // creating a variable for request queue.
        System.out.println("URL:"+url);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        // making a json object request to fetch data from API.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    System.out.println(" Response\n\n:"+response);
                    //Charting init, and give values
                    mpLineChart.setDragEnabled(true);//setTouchEnabled(true);
                    mpLineChart.setScaleEnabled(true);//setPinchZoom(true);

                    data_entries=new ArrayList<>();
                    //ArrayList<Integer> dataArray =new ArrayList<>();

                    // extracting data from json.
                    JSONArray dataArray = response.getJSONArray("prices");
                    //System.out.println("---------------RESPONSE COINGECKO------------------------------->>"+response+ " JSON:"+dataArray);

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONArray innerArray = dataArray.optJSONArray(i);
                        Float y = ((Double)innerArray.get(1)).floatValue();
                        Double dx = ((Long)innerArray.get(0)).doubleValue();
                        Float x = ((Double)dx).floatValue();
                        data_entries.add(new Entry(x, y));


                    }


                    Collections.sort(data_entries, new EntryXComparator());
                    lineDataSet = new LineDataSet(data_entries,"Dataset_Price");
                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(lineDataSet);
                    lineDataSet.setDrawCircles(false);
                    lineData = new LineData(dataSets);
                    lineData.setDrawValues(false);

                    mpLineChart.setData(lineData);
                    mpLineChart.setDrawBorders(true);
                    mpLineChart.setBackgroundColor(Color.DKGRAY); //set whatever color you prefer
                    mpLineChart.setDrawGridBackground(false);// this is a must

                    mpLineChart.invalidate();





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
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        });
        // calling a method to add our
        // json object request to our queue.
        queue.add(jsonObjectRequest);
    }
    private void configureLineChart(String str_time_format) {
        Description desc = new Description();
        desc.setText("Price History");
        desc.setTextSize(20);
        mpLineChart.setDescription(desc);

        XAxis xAxis = mpLineChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat(str_time_format, Locale.ENGLISH);//"dd MMM"

            @Override
            public String getFormattedValue(float value) {
                long v = (long) value;
                //System.out.println(" TIME !"+mFormat.format(new Date(v)));
                Date the_date = new Date(v);
                return mFormat.format(the_date);
            }
        });
    }
}
