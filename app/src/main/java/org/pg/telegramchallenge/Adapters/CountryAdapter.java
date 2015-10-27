package org.pg.telegramchallenge.Adapters;

import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import org.drinkless.td.libcore.telegram.Client;
import org.pg.telegramchallenge.CountrySelectFragment.Country;
import org.pg.telegramchallenge.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by artemypestretsov on 10/24/15.
 */

// TODO: NAMING!!!!
public class CountryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SECTION = 1;
    private static final int VIEW_TYPE_COUNTRY = 0;

    private static HashMap<String, Country> mDataHelper;
    private static ArrayList<CountryItem> mData;

    public interface Clicker {
        void click(Country country);
    }

    private final Clicker clickerListener;

    public CountryAdapter(Map<String, Country> data, Clicker clickerListener) {
        this.clickerListener = clickerListener;
        mDataHelper = (HashMap<String, Country>) data;
        mData = new ArrayList<>();

        // create temp list to parse it into headers and items (and sort it alphabetically)
        ArrayList<String> source = new ArrayList<>(data.keySet().size());
        source.addAll(data.keySet());
        Collections.sort(source);

        String prev = "";
        // this counter tracks adapters position of new section -- used inside SuperSlim
        int posOfHeader = 0;
        for (String countryName : source) {
            String candidate = countryName.substring(0, 1);
            if (!prev.equals(candidate)) {
                prev = candidate;
                posOfHeader = mData.size();
                mData.add(new CountryItem(prev, posOfHeader, true));
            }
            mData.add(new CountryItem(mDataHelper.get(countryName).getFullName(), posOfHeader, false));
        }
    }

    private class CountryViewHolder extends RecyclerView.ViewHolder {
        private TextView countryCodeTextView;
        private TextView countryFullNameTextView;

        public CountryViewHolder(View itemView) {
            super(itemView);

            countryFullNameTextView = (TextView) itemView.findViewById(R.id.country_full_name);
            countryCodeTextView = (TextView) itemView.findViewById(R.id.country_code);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickerListener.click(mDataHelper.get(mData.get(getAdapterPosition()).countryName));
                }
            });
        }
    }

    private class SectionViewHolder extends RecyclerView.ViewHolder {
        // section is only one char
        private TextView countryFirstLetter;

        public SectionViewHolder(View itemView) {
            super(itemView);
            countryFirstLetter = (TextView) itemView.findViewById(R.id.country_letter);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_COUNTRY) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_cell, parent, false);
            return new CountryViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_header_cell, parent, false);
            return new SectionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        CountryItem item = mData.get(position);

        // layot params for lib
        final LayoutManager.LayoutParams params = (LayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        params.setSlm(LinearSLM.ID);
        params.setFirstPosition(item.sectionFirstPos);
        holder.itemView.setLayoutParams(params);

        // this might give some cast exceptions, but its k
        if (item.isHeader) {
            ((SectionViewHolder)holder).countryFirstLetter.setText(mData.get(position).countryName);
        } else {
            String code = "+".concat(mDataHelper.get(mData.get(position).countryName).getCode());
            ((CountryViewHolder)holder).countryCodeTextView.setText(code);
            ((CountryViewHolder)holder).countryFullNameTextView.setText(mDataHelper.get(mData.get(position).countryName).getFullName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).isHeader) {
            return VIEW_TYPE_SECTION;
        } else {
            return VIEW_TYPE_COUNTRY;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    // helper class to represent each item inside recycler view
    private class CountryItem {
        public boolean isHeader;
        public int sectionFirstPos;
        // if header then countryName would consist only one letter
        public String countryName;

        public CountryItem(String countryName,int sectionFirstPos, boolean isHeader) {
            this.countryName = countryName;
            this.isHeader = isHeader;
            this.sectionFirstPos= sectionFirstPos;
        }
    }
}
