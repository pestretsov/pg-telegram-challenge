package org.pg.telegramchallenge.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.pg.telegramchallenge.CountrySelectFragment.Country;
import org.pg.telegramchallenge.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by artemypestretsov on 10/24/15.
 */
public class CountryListAdapter extends BaseAdapter {
    private HashMap<String, Country> mDataHelper;
    private ArrayList<String> mData;

    public CountryListAdapter(Map<String, Country> data) {
        mDataHelper = (HashMap<String, Country>) data;
        mData = new ArrayList<>(data.size());
        mData.addAll(mDataHelper.keySet());
        Collections.sort(mData);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Country getItem(int position) {
        return mDataHelper.get(mData.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.country_cell, parent, false);
        }

        TextView tv1 = (TextView)view.findViewById(R.id.country_full_name);
        tv1.setText(mData.get(position));
        TextView tv2 = (TextView)view.findViewById(R.id.country_code);
        String text = "+".concat(getItem(position).getCode());
        tv2.setText(text);

        return view;
    }
}
