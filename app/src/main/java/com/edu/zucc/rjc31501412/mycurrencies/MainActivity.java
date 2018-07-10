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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button mClacButton;
    private Button mTrend;
    private TextView mConvertedTextView;
    private EditText mAmountEditText;
    private Spinner mForSpinner, mHomSpinner;

    private String[] mCurrencies;

    public static final String FOR = "FOR_CURRENCY";
    public static final String HOM = "HOM_CURRENCY";

//    开发者key
    private String getKey(String keyName){
        AssetManager assetManager = this.getResources().getAssets();
        Properties properties = new Properties(); try {
            InputStream inputStream = assetManager.open("keys.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return	properties.getProperty(keyName);
    }

    private String mKey;

    public static final String RATES = "rates";
    public static final String URL_BASE =
            "https://openexchangerates.org/api/latest.json?app_id=";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConvertedTextView = (TextView) findViewById(R.id.txt_converted);
        mAmountEditText = (EditText) findViewById(R.id.edt_amount);
        mClacButton = (Button) findViewById(R.id.btn_calc);
        mForSpinner = (Spinner) findViewById(R.id.spn_for);
        mHomSpinner = (Spinner) findViewById(R.id.spn_hom);
        mTrend = (Button) findViewById(R.id.trendActivity);

        @SuppressWarnings("unchecked")
        ArrayList<String> arrayList = (ArrayList<String>)
                getIntent().getSerializableExtra(SplashActivity.KEY_ARRAYLIST);
        Collections.sort(arrayList);
        mCurrencies = arrayList.toArray(new String[arrayList.size()]);


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
                new CurrencyConverterTask().execute(URL_BASE+mKey);
            }
        });
        mTrend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,Trend.class);
                startActivity(intent);
            }
        });
        mKey = getKey("open_key");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
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


    //judge online
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private void launchBrowser(String strUri) {
        if (isOnline()) {
            Uri uri = Uri.parse(strUri);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void invertCurrencies() {
        int nFor = mForSpinner.getSelectedItemPosition();
        int nHom = mHomSpinner.getSelectedItemPosition();
        mForSpinner.setSelection(nHom);
        mHomSpinner.setSelection(nFor);
        mConvertedTextView.setText("");
        PrefsMgr.setString(this, FOR, extractCodeFromCurrency((String) mForSpinner.getSelectedItem()));
        PrefsMgr.setString(this, HOM, extractCodeFromCurrency((String) mHomSpinner.getSelectedItem()));
    }

    //menuItem
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.mnu_invert:
                invertCurrencies();
                break;
            case R.id.mnu_codes:
                launchBrowser(SplashActivity.URL_CODES);
                break;
            case R.id.mnu_exit:
                finish();
                break;
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


//    通过json格式获取货币代码，创建私有内部类实现数据的提取
    private class CurrencyConverterTask extends AsyncTask<String,Void,JSONObject> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
//            等待对话框
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Calculating Result...");
            progressDialog.setMessage("One moment please..."); progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//                取消
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    CurrencyConverterTask.this.cancel(true);
                    progressDialog.dismiss();
                }
            });
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            double dCalculated = 0.0;
            String strForCode = extractCodeFromCurrency(mCurrencies[mForSpinner.getSelectedItemPosition()]);
            String strHomCode = extractCodeFromCurrency(mCurrencies[mHomSpinner. getSelectedItemPosition()]);
            String strAmount = mAmountEditText.getText().toString();
            try {
                if (jsonObject == null){
                    throw new JSONException("no data available.");
                }
                JSONObject jsonRates = jsonObject.getJSONObject(RATES);
                if (strHomCode.equalsIgnoreCase("USD")){
                    dCalculated = Double.parseDouble(strAmount) / jsonRates.getDouble(strForCode);
                } else if (strForCode.equalsIgnoreCase("USD")) {
                    dCalculated = Double.parseDouble(strAmount) *
                        jsonRates.getDouble(strHomCode) ;
                }
                else {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode)
                            / jsonRates.getDouble(strForCode) ;
                }
            } catch (JSONException e) {
                Toast.makeText(
                        MainActivity.this,
                        "There's been a JSON exception: " + e.getMessage(), Toast.LENGTH_LONG
                ).show();
                mConvertedTextView.setText("");
                e.printStackTrace();
            }
            mConvertedTextView.setText(String.format("%s%s", DECIMAL_FORMAT.format(dCalculated), strHomCode));
                    progressDialog.dismiss();

        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            return new JSONParser().getJSONFromUrl(strings[0]);
        }
    }

}
