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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptocurrencytracker.CoinModel;
import com.example.cryptocurrencytracker.R;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

// on below line we are creating our adapter class
// in this class we are passing our array list
// and our View Holder class which we have created.
public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.CurrencyViewholder> {
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static ClickListener clickListener;
    private ArrayList<CoinModel> currencyModals;
    private Context context;


    public CoinAdapter(ArrayList<CoinModel> currencyModals, Context context) {
        this.currencyModals = currencyModals;
        this.context = context;
    }

    // below is the method to filter our list.
    public void filterList(ArrayList<CoinModel> filterllist) {
        // adding filtered list to our
        // array list and notifying data set changed
        currencyModals = filterllist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoinAdapter.CurrencyViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // this method is use to inflate the layout file
        // which we have created for our recycler view.
        // on below line we are inflating our layout file.
        View view = LayoutInflater.from(context).inflate(R.layout.currency_rv_item, parent, false);
        return new CoinAdapter.CurrencyViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinAdapter.CurrencyViewholder holder, int position) {
        // on below line we are setting data to our item of
        // recycler view and all its views.
        CoinModel modal = currencyModals.get(position);
        holder.nameTV.setText(modal.getName());
        holder.rateTV.setText("$ " + df2.format(modal.getPrice()));
        holder.symbolTV.setText(modal.getSymbol());
        System.out.println(" HERE zzzz");
        double value_percentage = modal.getChange_percentage24h();
        String str_value_percentage="";
        if (value_percentage >= 0){
            str_value_percentage="+"+df2.format(value_percentage)+"%";
            holder.percentchangeTV.setTextColor(Color.GREEN);
        }
        else{

            str_value_percentage=df2.format(value_percentage)+"%";
            holder.percentchangeTV.setTextColor(Color.RED);

        }

        holder.percentchangeTV.setText(str_value_percentage);
        // show The Image in a ImageView
        //if (holder.logoIV.getImage())
        //DownloadImageTask dl = new DownloadImageTask(itemView.findViewById(R.id.id_imageView_logo));

        String logoImageTag = (String) holder.logoIV.getTag();
        //if(logoImageTag.compareTo("initialImg")==0){
        System.out.println("HOLDER VIEWER ->" + holder.logoIV.getDrawable());
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
        System.out.println("\n--------> POS:"+position+" the bol:"+modal.isFavorite());

        //in some cases, it will prevent unwanted situations
        holder.cb.setOnCheckedChangeListener(null);

        //if true, your checkbox will be selected, else unselected
        holder.cb.setChecked(modal.isFavorite());

        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                modal.setFavorite(isChecked);
            }
        });
        //holder.logoIV.buildDrawingCache();

    }

    @Override
    public int getItemCount() {
        // on below line we are returning
        // the size of our array list.
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
    // on below line we are creating our view holder class
    // which will be used to initialize each view of our layout file.
    public class CurrencyViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView symbolTV, rateTV, nameTV,percentchangeTV;
        private ImageView logoIV;
        private CheckBox cb;
        public CurrencyViewholder(@NonNull View itemView) {
            super(itemView);
            // on below line we are initializing all
            // our text views along with  its ids.
            symbolTV = itemView.findViewById(R.id.idtvsymbol);
            rateTV = itemView.findViewById(R.id.idtvrate);
            nameTV = itemView.findViewById(R.id.idtvname);
            logoIV = itemView.findViewById(R.id.id_imageView_logo);
            percentchangeTV= itemView.findViewById(R.id.idtvrate2);
            itemView.setOnClickListener(this);
            cb = (CheckBox) itemView.findViewById(R.id.checkBoxFavorites);
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.iconImageViewOnClick(v, getAdapterPosition());
                }
            });



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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        CoinModel model;

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
