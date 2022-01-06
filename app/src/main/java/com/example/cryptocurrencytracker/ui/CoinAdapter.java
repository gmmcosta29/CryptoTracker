package com.example.cryptocurrencytracker.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptocurrencytracker.CoinModel;
import com.example.cryptocurrencytracker.R;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.CurrencyViewholder> {
    private static final DecimalFormat df2 = new DecimalFormat("#.##");
    private static ClickListener clickListener;
    private ArrayList<CoinModel> currencyModals;
    private final Context context;


    public CoinAdapter(ArrayList<CoinModel> currencyModals, Context context) {
        this.currencyModals = currencyModals;
        this.context = context;
    }

    public void filterList(ArrayList<CoinModel> filterllist) {
        currencyModals = filterllist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoinAdapter.CurrencyViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.currency_rv_item, parent, false);
        return new CurrencyViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinAdapter.CurrencyViewholder holder, int position) {
        CoinModel modal = currencyModals.get(position);
        holder.nameTV.setText(modal.getName());
        holder.rateTV.setText("$ " + df2.format(modal.getPrice()));
        holder.symbolTV.setText(modal.getSymbol());
        double value_percentage = modal.getChange_percentage24h();
        String str_value_percentage;
        if (value_percentage >= 0){
            str_value_percentage="+"+df2.format(value_percentage)+"%";
            holder.percentchangeTV.setTextColor(Color.GREEN);
        }
        else{

            str_value_percentage=df2.format(value_percentage)+"%";
            holder.percentchangeTV.setTextColor(Color.RED);

        }

        holder.percentchangeTV.setText(str_value_percentage);

        String logoImageTag = (String) holder.logoIV.getTag();
        //if(logoImageTag.compareTo("initialImg")==0){
        if(modal.getImage() != null){
            holder.logoIV.setImageBitmap(modal.getImage());
        }else{
            new DownloadImageTask((ImageView) holder.logoIV, modal).execute(modal.getImage_url());

        }



        // BitmapDrawable drawable = (BitmapDrawable) holder.logoIV.getDrawable();
       // BitmapDrawable drawable = (BitmapDrawable) this.bmImage.getDrawable();
       // Bitmap bitmap = drawable.getBitmap();
        //    holder.logoIV.setTag("logoSet");
        //}
        //holder.cb.

        holder.cb.setOnCheckedChangeListener(null);

        holder.cb.setChecked(modal.isFavorite());

        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            modal.setFavorite(isChecked);
        });

    }

    @Override
    public int getItemCount() {
        return currencyModals.size();
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }

        return hasImage;
    }
    public static class CurrencyViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView symbolTV;
        private final TextView rateTV;
        private final TextView nameTV;
        private final TextView percentchangeTV;
        private final ImageView logoIV;
        private final CheckBox cb;
        public CurrencyViewholder(@NonNull View itemView) {
            super(itemView);
            symbolTV = itemView.findViewById(R.id.idtvsymbol);
            rateTV = itemView.findViewById(R.id.idtvrate);
            nameTV = itemView.findViewById(R.id.idtvname);
            logoIV = itemView.findViewById(R.id.id_imageView_logo);
            percentchangeTV= itemView.findViewById(R.id.idtvrate2);
            itemView.setOnClickListener(this);
            cb = (CheckBox) itemView.findViewById(R.id.checkBoxFavorites);
            cb.setOnClickListener(v -> clickListener.iconImageViewOnClick(v, getAdapterPosition()));



        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }

    }

    public void setOnItemClickListener(ClickListener clickListener) {
        CoinAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
        void iconImageViewOnClick(View v, int position);
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        final ImageView bmImage;
        final CoinModel model;

        public DownloadImageTask(ImageView bmImage, CoinModel modal) {
            this.bmImage = bmImage;
            this.model = modal;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result)
        {
            bmImage.setImageBitmap(result);
            model.setImage(result);

        }
        public ImageView getImgView(){
            return bmImage;
        }
    }
}
