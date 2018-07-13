package com.edu.zucc.rjc31501412.mycurrencies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RecordAdapter extends ArrayAdapter<BeanRecord>{
    private int resourceId;
    public RecordAdapter(Context context, int id, List<BeanRecord> objects) {
        super(context,id,objects);
        resourceId = id;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BeanRecord beanRecord = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId,null);
        }
        else {
            view = convertView;
        }

        TextView forCode = (TextView) view.findViewById(R.id.list_for);
        TextView forAmount = (TextView) view.findViewById(R.id.list_for_amount);
        TextView homCode = (TextView) view.findViewById(R.id.list_hom);
        TextView homAmount = (TextView) view.findViewById(R.id.list_hom_amount);
        TextView listTime = (TextView) view.findViewById(R.id.list_time);

        forCode.setText(beanRecord.getForCode());
        forAmount.setText(beanRecord.getForAmount());
        homCode.setText(beanRecord.getHomCode());
        homAmount.setText(beanRecord.getHomAmount());
        listTime.setText(beanRecord.getTime());


        return view;
    }
}
