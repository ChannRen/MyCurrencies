package com.edu.zucc.rjc31501412.mycurrencies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import static com.edu.zucc.rjc31501412.mycurrencies.SplashActivity.KEY_ARRAYLIST;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    private Button mClacButton;
    private TextView mConvertedTextView;
    private EditText mAmountEditText;
    private AppCompatSpinner mForSpinner, mHomSpinner;

    public String[] mCurrencies;
    public static String[] mCurrenciesForMain;
    public static final String stringx = "key_arraylist";
    public static final String FOR = "FOR_CURRENCY";
    public static final String HOM = "HOM_CURRENCY";

    private String mKey;

    public static final String RATES = "rates";
    public static final String URL_BASE = "https://openexchangerates.org/api/latest.json?app_id=";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00000");
    private CurrencyTaskCallback mCurrencyTaskCallback;

    public interface CurrencyTaskCallback {
        void executionDone();
    }

    public void setCurrencyTaskCallback(CurrencyTaskCallback currencyTaskCallback) {
        this.mCurrencyTaskCallback = currencyTaskCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        @SuppressWarnings("unchecked")
        ArrayList<String> arrayList = (ArrayList<String>) getIntent().getSerializableExtra(KEY_ARRAYLIST);
        Collections.sort(arrayList);
        mCurrencies = arrayList.toArray(new String[arrayList.size()]);
        mCurrenciesForMain = mCurrencies;
        mConvertedTextView = (TextView) findViewById(R.id.txt_converted);
        mAmountEditText = (EditText) findViewById(R.id.edt_amount);
        mClacButton = (Button) findViewById(R.id.btn_calc);
        mForSpinner = (AppCompatSpinner) findViewById(R.id.spn_for);
        mHomSpinner = (AppCompatSpinner) findViewById(R.id.spn_hom);

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


        mClacButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNumeric(String.valueOf(mAmountEditText.getText()))) {
                    new CurrencyConverterTask().execute(URL_BASE + mKey);
                } else {
                    Toast.makeText(MainActivity.this, "Not a numeric value, try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
        mKey = getKey("open_key");
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    //在浏览器中查看
    private void launchBrowser(String strUri) {
        if (isOnline()) {
            Uri uri = Uri.parse(strUri);
            //call an implicit intent
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    //交换上下
    private void invertCurrencies() {
        int nFor = mForSpinner.getSelectedItemPosition();
        int nHom = mHomSpinner.getSelectedItemPosition();
        mForSpinner.setSelection(nHom);
        mHomSpinner.setSelection(nFor);
        mConvertedTextView.setText("");

        PrefsMgr.setString(this, FOR, extractCodeFromCurrency((String) mForSpinner.getSelectedItem()));
        PrefsMgr.setString(this, HOM, extractCodeFromCurrency((String) mHomSpinner.getSelectedItem()));
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

    public static boolean isNumeric(String str) {
        try {
            double dub = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
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
        mConvertedTextView.setText("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class CurrencyConverterTask extends AsyncTask<String, Void, JSONObject> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Calculating Result...");
            progressDialog.setMessage("One moment please...");
            progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CurrencyConverterTask.this.cancel(true);
                            progressDialog.dismiss();
                        }
                    });
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            double dCalculated = 0.0;

            String strForCode = extractCodeFromCurrency(mCurrencies[mForSpinner.getSelectedItemPosition()]);
            String strHomCode = extractCodeFromCurrency(mCurrencies[mHomSpinner.getSelectedItemPosition()]);
            String strAmount = mAmountEditText.getText().toString();

            try {
                if (jsonObject == null) {
                    throw new JSONException("no data available.");
                }
                JSONObject jsonRates = jsonObject.getJSONObject(RATES);
                if (strHomCode.equalsIgnoreCase("USD")) {
                    dCalculated = Double.parseDouble(strAmount) / jsonRates.getDouble(strForCode);
                } else if (strForCode.equalsIgnoreCase("USD")) {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode);
                } else {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode) / jsonRates.getDouble(strForCode);
                }
            } catch (JSONException e) {
                Toast.makeText(
                        MainActivity.this,
                        "There's been a JSON exception: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                mConvertedTextView.setText("");
                e.printStackTrace();
            }
            mConvertedTextView.setText(DECIMAL_FORMAT.format(dCalculated) + "" + strHomCode);

            //存入数据库
            BeanRecord beanRecord = new BeanRecord();
            beanRecord.setForCode(strForCode);
            beanRecord.setForAmount(strAmount);
            beanRecord.setHomCode(strHomCode);
            beanRecord.setHomAmount(new DecimalFormat("0.00").format(dCalculated));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
            Date date = new Date(System.currentTimeMillis());
            beanRecord.setTime(simpleDateFormat.format(date));
            MyDatabaseManager myDatabaseManager = new MyDatabaseManager(getBaseContext());
            myDatabaseManager.addData(beanRecord);

            progressDialog.dismiss();

            //for testing
            if (mCurrencyTaskCallback != null) {
                mCurrencyTaskCallback.executionDone();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_exchange:
                invertCurrencies();
                break;
            case R.id.nav_search:
                launchBrowser(SplashActivity.URL_CODES);
                break;
            case R.id.nav_record:
                Intent intent = new Intent(this, RecordActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_Trend:
                Intent intent1 = new Intent(this, Trend.class);
                intent1.putExtra(stringx,mCurrencies);
                startActivity(intent1);
                break;
            case R.id.nav_CNY:
                Intent intent2 = new Intent(MainActivity.this,ChinaRateActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_share:

                break;
            case R.id.nav_exit:
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
