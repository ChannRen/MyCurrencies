package com.edu.zucc.rjc31501412.mycurrencies;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ChinaRateActivity extends AppCompatActivity {
    private String mKey;

    private ListView listView;
    private List<BeanCNY> beanCNYs = new ArrayList<>();
    private ChinaRateAdapter chinaRateAdapter;
    public static final String URL_BASE =
            "http://openexchangerates.org/api/latest.json?app_id=";
    //public static final String URL_BID_ASK = "&show_bid_ask=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_china_rate);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("CNY Bid&Ask");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mKey = getKey("open_key");
        new FetchCodeTask().execute(URL_BASE+mKey);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.CNY_Refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchCodeTask().execute(URL_BASE+mKey);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     *AsyncTask
     */
    private class FetchCodeTask extends AsyncTask<String, Void, JSONObject>{
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ChinaRateActivity.this);
            progressDialog.setTitle("Getting Result...");
            progressDialog.setMessage("One moment please...");
            progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ChinaRateActivity.FetchCodeTask.this.cancel(true);
                            progressDialog.dismiss();
                        }
                    });
            progressDialog.show();
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject == null) {
                    throw new JSONException("no data available.");
                }

                JSONObject rates = jsonObject.getJSONObject("rates");
                Iterator iterator = rates.keys();
                String key = "";
                beanCNYs = new ArrayList<BeanCNY>();
                BeanCNY beanCNY = null;

                while (iterator.hasNext()) {
                    double money;
                    key = (String) iterator.next();
                    String forC = key;

                    money = rates.getDouble("CNY")/rates.getDouble(key)*100;
                    beanCNY = new BeanCNY(forC, money+"", money+"");
                    beanCNYs.add(beanCNY);
                }
                chinaRateAdapter = new ChinaRateAdapter(ChinaRateActivity.this,R.layout.chinarate_list,beanCNYs);
                listView = (ListView) findViewById(R.id.CNY_ListView);
                listView.setAdapter(chinaRateAdapter);
                listView.setTextFilterEnabled(true);
                progressDialog.dismiss();
            } catch (JSONException e){
                Toast.makeText(
                        ChinaRateActivity.this,
                        "There's been a JSON exception: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                e.printStackTrace();
            }

        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            return new JSONParser().getJSONFromUrl(strings[0]);
        }
    }
    /**
     *get key from assets/keys.properties
     */
    private String getKey(String keyName) {
        AssetManager assetManager = this.getResources().getAssets();
        Properties properties = new Properties();
        try {
            InputStream inputStream = assetManager.open("keys.properties");
            properties.load(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(keyName);
    }

    /**
     *back button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
