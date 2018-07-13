package com.edu.zucc.rjc31501412.mycurrencies;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.text.format.Time;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.edu.zucc.rjc31501412.mycurrencies.MainActivity.FOR;
import static com.edu.zucc.rjc31501412.mycurrencies.MainActivity.HOM;


public class Trend extends AppCompatActivity {

    private String mKey;
    private String[] mCurrencies;
    private AppCompatSpinner mForSpinner, mHomSpinner;

    public static String DATE ="2018-7-10";
    public static final String URL_LEFT = "https://openexchangerates.org/api/historical/";
    public static final String URL_Right = ".json?app_id=";

    private CurrencyTaskCallback mCurrencyTaskCallback;

    public interface CurrencyTaskCallback {
        void executionDone();
    }
    public void setCurrencyTaskCallback(CurrencyTaskCallback currencyTaskCallback) {
        this.mCurrencyTaskCallback = currencyTaskCallback;
    }

    private String GetDateFromTime(){
        Time t = new Time();
        t.setToNow();
        return t.year+"-"+t.month+"-"+t.monthDay;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LineChart mLineChart = (LineChart) findViewById(R.id.lineChart);
        Button chartEx = (Button) findViewById(R.id.chart_ex);
        mForSpinner = (AppCompatSpinner) findViewById(R.id.trend_for_spinner);
        mHomSpinner = (AppCompatSpinner) findViewById(R.id.trend_for_spinner);

        //spinner适配器
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_closed,
                mCurrencies
        );
        //spinner下拉
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mForSpinner.setAdapter(arrayAdapter);
        mHomSpinner.setAdapter(arrayAdapter);

        mForSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //显示边界
        mLineChart.setDrawBorders(true);
        //设置数据
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(new Entry(i, (float) (Math.random()) * 10));
        }

        //一个LineDataSet就是一条线
        LineDataSet lineDataSet = new LineDataSet(entries, "汇率");
        final LineData data = new LineData(lineDataSet);
        mLineChart.setData(data);

        chartEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CurrencyConverterTask().execute(URL_LEFT + DATE + URL_Right+mKey);
            }
        });

        mKey = getKey("open_key");
    }


    private class CurrencyConverterTask extends AsyncTask<String, Void, JSONObject> {
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Trend.this);
            progressDialog.setTitle("Getting data...");
            progressDialog.setMessage("One moment please...");
            progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    CurrencyConverterTask.this.cancel(true);
                    progressDialog.dismiss();
                }
            });
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(params[0]);
        }
    }
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
