package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tonicartos.superslim.LayoutManager;

import org.pg.telegramchallenge.Adapters.CountryAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class CountrySelectFragment extends Fragment {
    private static String TAG = CountrySelectFragment.class.getCanonicalName();

    private RecyclerView countriesListView;
    private static Map<String ,Country> fullnameCountryMap = new HashMap<>();
    private static Map<String, Country> codeCountryMap = new HashMap<>();

    public CountrySelectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_country_select, container, false);

        parseCountries();

        countriesListView = (RecyclerView)view.findViewById(R.id.countries_recycler_view);
        countriesListView.setLayoutManager(new LayoutManager(getContext()));
        countriesListView.setAdapter(new CountryAdapter(fullnameCountryMap, new CountryAdapter.Clicker() {
            @Override
            public void click(Country country) {
                Log.e(TAG, country.getFullName());
                // TODO:
                // write code to pass country name/code to the right field
            }
        }));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void parseCountries() {
        try {
            InputStream stream = ObserverApplication.appContext.getResources().getAssets().open("countries.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            // not that elegant, but fast and bulletproof
            while (true) {
                final String line = reader.readLine();

                if (line == null) {
                    break;
                }

                final String[] lineArr = line.split(";");
                final Country country = new Country();

                country.code = lineArr[0];
                country.shortName = lineArr[1];
                country.fullName = lineArr[2];

                fullnameCountryMap.put(country.fullName, country);
                codeCountryMap.put(country.code, country);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class Country {
        private String shortName;
        private String fullName;
        private String code;

        @Override
        public String toString() {
            return code + " " + shortName + " " + fullName;
        }

        public String getCode() {
            return code;
        }

        public String getFullName() {
            return fullName;
        }

        public String getShortName() {
            return shortName;
        }
    }

}
