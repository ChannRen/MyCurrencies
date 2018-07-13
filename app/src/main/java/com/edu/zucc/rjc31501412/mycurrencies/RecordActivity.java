package com.edu.zucc.rjc31501412.mycurrencies;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {
    private MyDatabaseManager myDatabaseManager;
    private ListView listView;
    private List<BeanRecord> beanRecords = new ArrayList<>();
    EditText editSearch;
    ImageView imageViewDelete;
    private RecordAdapter recordAdapter;

    ArrayList<String> forCode = new ArrayList<>();
    ArrayList<String> forAmount = new ArrayList<>();
    ArrayList<String> homCode = new ArrayList<>();
    ArrayList<String> homAmount = new ArrayList<>();
    ArrayList<String> time = new ArrayList<>();
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDatabaseManager  = new MyDatabaseManager(getBaseContext());
        setContentView(R.layout.activity_record);
        initBeanRecords();
        setEditSearchTextChanged();
        setDeleteOnClick();

        recordAdapter= new RecordAdapter(this,R.layout.record_list,beanRecords);
        recordAdapter.notifyDataSetChanged();

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(recordAdapter);
        listView.setTextFilterEnabled(true);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("Record");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }
    private void setEditSearchTextChanged() {
        editSearch = (EditText) findViewById(R.id.edit_query);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //editText change

                //if edit==null delete=GONE
                if (editable.length() == 0){
                    imageViewDelete.setVisibility(View.GONE);
                } else {
                    imageViewDelete.setVisibility(View.VISIBLE);
                }
                //runnable update ui
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String data = editSearch.getText().toString();
                        beanRecords.clear();
                        getDataSub(beanRecords,data);
                        recordAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     * 筛选数据，放入list
     */
    private void getDataSub(List<BeanRecord> beanRecords,String data) {
        int length  = homCode.size();
        for (int i = 0;i < length;++i) {
            if (time.get(i).contains(data)||
                    forCode.get(i).contains(data)||
                    forAmount.get(i).contains(data)||
                    homCode.get(i).contains(data)||
                    homAmount.get(i).contains(data)){
                BeanRecord beanRecord = new BeanRecord();

                beanRecord.setTime(time.get(i));
                beanRecord.setForCode(forCode.get(i));
                beanRecord.setForAmount(forAmount.get(i));
                beanRecord.setHomCode(homCode.get(i));
                beanRecord.setHomAmount(homAmount.get(i));
                beanRecords.add(beanRecord);
            }
        }
    }

    /**
     * DELETE
     */
    private void setDeleteOnClick(){
        imageViewDelete = (ImageView) findViewById(R.id.delete);
        imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editSearch.setText("");
            }
        });
    }

    private void initBeanRecords(){
        beanRecords = myDatabaseManager.queryAllContent();
        for (BeanRecord  beanRecord:beanRecords) {
            if (beanRecord != null) {
                time.add(beanRecord.getTime());
                forCode.add(beanRecord.getForCode());
                forAmount.add(beanRecord.getForAmount());
                homCode.add(beanRecord.getHomCode());
                homAmount.add(beanRecord.getHomAmount());
            }
        }
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
