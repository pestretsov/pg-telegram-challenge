package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.TdApi;


/**
 * A simple {@link Fragment} subclass.
 */
public class AuthPhoneNumberFragment extends Fragment implements Acceptable, ObserverApplication.OnErrorObserver{

    private EditText countryName;
    private EditText number;
    private EditText countryCode;

    private ActionBar actionBar;
    View acceptActionBarIcon;

    private CountrySelectFragment.Country country;

    public ObserverApplication getApplication(){
        return (ObserverApplication) getActivity().getApplication();
    }

    public AuthPhoneNumberFragment() {
        // Required empty public constructor
    }

    public static AuthPhoneNumberFragment newInstance() {
        
        Bundle args = new Bundle();
        
        AuthPhoneNumberFragment fragment = new AuthPhoneNumberFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.auth_phone_number_title);
        }

        if (country!=null) {
            countryName.setText(country.getFullName());
            countryCode.setText(String.format("+%s", country.getCode()));
            country = null;
        }

        if (acceptActionBarIcon!=null) {
            acceptActionBarIcon.setVisibility(View.VISIBLE);
        }

        getApplication().addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_auth_phone_number, container, false);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        acceptActionBarIcon = getActivity().findViewById(R.id.action_accept);

        countryName = (EditText) view.findViewById(R.id.auth_country_name);
        countryCode = (EditText) view.findViewById(R.id.country_code);
        number = (EditText) view.findViewById(R.id.phone_number);

        Selection.setSelection(countryCode.getText(), countryCode.getText().length());
        countryCode.setText("+");
        countryCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().startsWith("+")) {
                    countryCode.setText("+");
                    Selection.setSelection(countryCode.getText(), countryCode.getText().length());
                }

                if (s.length() < 2) {
                    countryName.setText(null);
                    return;
                }

                String code = s.toString().substring(1);

                CountrySelectFragment.Country country = null;
                if ((country = CountrySelectFragment.getCountryByCode(code)) != null) {
                    countryName.setText(country.getFullName());
                } else {
                    countryName.setText(null);
                }
            }
        });

        countryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).replaceFragment(CountrySelectFragment.newInstance(), true);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        getApplication().removeObserver(this);
    }

    @Override
    public void proceed(TdApi.Error err) {
        Toast.makeText(getActivity().getBaseContext(), "ERROR IN PHONE NUMBER!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void accept() {

        final String phoneNumber = countryCode.getText().toString() + number.getText().toString();
        getApplication().sendRequest(new TdApi.SetAuthPhoneNumber(phoneNumber));
    }

    public void setCountry(CountrySelectFragment.Country country) {
        this.country = country;
    }
}
