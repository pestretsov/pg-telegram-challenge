package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
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
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class CountrySelectFragment extends Fragment {
    private static String TAG = CountrySelectFragment.class.getCanonicalName();

    private RecyclerView countriesListView;
    private static Map<String ,Country> fullnameCountryMap = new HashMap<>();
    private static Map<String, Country> codeCountryMap = new HashMap<>();
    private ActionBar actionBar;

    public CountrySelectFragment() {
        // Required empty public constructor
    }

    public static CountrySelectFragment newInstance() {

        Bundle args = new Bundle();

        CountrySelectFragment fragment = new CountrySelectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_country_select, container, false);

        if (codeCountryMap.isEmpty())
            parseCountries();

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        countriesListView = (RecyclerView)view.findViewById(R.id.countries_recycler_view);
        countriesListView.setLayoutManager(new LayoutManager(getContext()));
        countriesListView.setAdapter(new CountryAdapter(fullnameCountryMap, new CountryAdapter.Clicker() {
            @Override
            public void click(Country country) {
                Log.e(TAG, country.getFullName());

                FragmentManager fm = getFragmentManager();
                List<Fragment> fragments = fm.getFragments();

                // not really good solution
                AuthPhoneNumberFragment phoneNumberFragment = (AuthPhoneNumberFragment) fragments.get(0);

                phoneNumberFragment.setCountry(country);
                fm.popBackStack();
            }
        }));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        actionBar.setTitle(R.string.auth_country);
        View viewById = getActivity().findViewById(R.id.action_accept);
        if (viewById!=null) {
            viewById.setVisibility(View.GONE);
        }
    }

    public static Country getCountryByCode(String code){
        if (codeCountryMap.isEmpty()){
            parseCountries();
        }

        if (codeCountryMap.isEmpty()) {
            return null;
        }

        return codeCountryMap.get(code);
    }

    static public void parseCountries() {
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

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public class Country {
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
