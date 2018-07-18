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

public class ChinaRateAdapter extends ArrayAdapter<BeanCNY> {
    private int resourceId;

    public ChinaRateAdapter(Context context, int Id, List<BeanCNY> objects) {
        super(context,Id,objects);
        resourceId = Id;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {
        BeanCNY beanCNY = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId,null);
        } else {
            view = convertView;
        }
        TextView forCurrency = (TextView) view.findViewById(R.id.CNY_ListView_For);
        TextView bid = (TextView) view.findViewById(R.id.CNY_ListView_Bid);
        TextView ask = (TextView) view.findViewById(R.id.CNY_ListView_Ask);

        forCurrency.setText(beanCNY.getForCurrency());
        bid.setText(beanCNY.getBid());
        ask.setText(beanCNY.getAsk());

        return view;
    }
}
