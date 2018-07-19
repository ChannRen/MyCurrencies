package com.edu.zucc.rjc31501412.mycurrencies;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.edu.zucc.rjc31501412.mycurrencies.MainActivity.FOR;
import static com.edu.zucc.rjc31501412.mycurrencies.MainActivity.HOM;
import static com.edu.zucc.rjc31501412.mycurrencies.MainActivity.RATES;


public class Trend extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    List<Entry> entries = new ArrayList<>();

    int i = 1;
    private double rate = 0.0;
    private String mKey;
    private AppCompatSpinner mForSpinner, mHomSpinner;
    public String[] mCurrencies;
    public static final String URL_BASE = "http://openexchangerates.org/api/latest.json?app_id=";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00000");
    private CurrencyTaskCallback mCurrencyTaskCallback;
    Button chartEx;
    public interface CurrencyTaskCallback {
        void executionDone();
    }

    public void setCurrencyTaskCallback(CurrencyTaskCallback currencyTaskCallback) {
        this.mCurrencyTaskCallback = currencyTaskCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
                actionBar.setTitle("Trend Chart");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final Button chartEx = (Button) findViewById(R.id.chart_ex);
        mForSpinner = (AppCompatSpinner) findViewById(R.id.trend_for_spinner);
        mHomSpinner = (AppCompatSpinner) findViewById(R.id.trend_hom_spinner);
        mKey = getKey("open_key");

        chartEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chartEx.setEnabled(false);
                mForSpinner.setEnabled(false);
                mHomSpinner.setEnabled(false);
                runnable.run();
            }
        });

        mCurrencies = MainActivity.mCurrenciesForMain;
        //controller
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_closed,
                mCurrencies
        );
        //view
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mHomSpinner.setAdapter(arrayAdapter);
        mForSpinner.setAdapter(arrayAdapter);

        mHomSpinner.setOnItemSelectedListener(this);
        mForSpinner.setOnItemSelectedListener(this);

        if (savedInstanceState == null &&
                (PrefsMgr.getString(this, FOR) == null &&
                        PrefsMgr.getString(this, HOM) == null)) {
            mForSpinner.setSelection(findPositionGivenCode("USD", mCurrencies));
            mHomSpinner.setSelection(findPositionGivenCode("CNY", mCurrencies));
            PrefsMgr.setString(this, FOR, "USD");
            PrefsMgr.setString(this, HOM, "CNY");
        } else {
            mForSpinner.setSelection(findPositionGivenCode(PrefsMgr.getString(this, FOR), mCurrencies));
            mHomSpinner.setSelection(findPositionGivenCode(PrefsMgr.getString(this, HOM), mCurrencies));
        }



    }
    final Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            new CurrencyConverterTask().execute(URL_BASE+mKey);
            handler.postDelayed(this, 6000);
        }
    };
    private void addEntry(){
        LineChart mLineChart = (LineChart) findViewById(R.id.lineChart);
        //不显示边界

        mLineChart.setDrawBorders(false);
        XAxis xAxis = mLineChart.getXAxis();
        YAxis leftYAxis = mLineChart.getAxisLeft();            //左侧Y轴
        YAxis rightYaxis = mLineChart.getAxisRight();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mLineChart.setDrawGridBackground(false);
        xAxis.setDrawGridLines(false);
        rightYaxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        rightYaxis.setEnabled(false);

        Description description = new Description();
        description.setEnabled(false);
        mLineChart.setDescription(description);

        //设置数据
        //一个LineDataSet就是一条线
        LineDataSet lineDataSet = new LineDataSet(entries, "Rate");
        final LineData data = new LineData(lineDataSet);
        mLineChart.setData(data);
        entries.add(new Entry(i, (float) (rate)));
        i++;
        mLineChart.fitScreen();
    }

    private int findPositionGivenCode(String code, String[] currencies) {

        for (int i = 0; i < currencies.length; i++) {
            if (extractCodeFromCurrency(currencies[i]).equalsIgnoreCase(code)) {
                return i;
            }
        }
        //default
        return 0;
    }

    private String extractCodeFromCurrency(String currency) {
        return (currency).substring(0, 3);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {

            case R.id.spn_for:
                PrefsMgr.setString(this, FOR,
                        extractCodeFromCurrency((String) mForSpinner.getSelectedItem()));
                break;

            case R.id.spn_hom:
                PrefsMgr.setString(this, HOM,
                        extractCodeFromCurrency((String) mHomSpinner.getSelectedItem()));
                break;

            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private class CurrencyConverterTask extends AsyncTask<String, Void, JSONObject> {

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        @Override
        protected void onPreExecute() {
            actionBar.setTitle("Trend Chart(Refreshing)");
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            String strForCode = extractCodeFromCurrency(mCurrencies[mForSpinner.getSelectedItemPosition()]);
            String strHomCode = extractCodeFromCurrency(mCurrencies[mHomSpinner.getSelectedItemPosition()]);

            try {
                if (jsonObject == null) {
                    throw new JSONException("no data available.");
                }
                JSONObject jsonRates = jsonObject.getJSONObject(RATES);
                if (strHomCode.equalsIgnoreCase("USD")) {
                    rate = jsonRates.getDouble(strForCode);
                } else if (strForCode.equalsIgnoreCase("USD")) {
                    rate = jsonRates.getDouble(strHomCode);
                } else {
                    rate = jsonRates.getDouble(strHomCode) / jsonRates.getDouble(strForCode);
                }
                addEntry();
                actionBar.setTitle("Trend Chart");
            } catch (JSONException e) {
                Toast.makeText(
                        Trend.this,
                        "There's been a JSON exception: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                e.printStackTrace();
            }
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
                handler.removeCallbacks(runnable);
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
