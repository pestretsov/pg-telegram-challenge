package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private EditText number;
    private EditText countryCode;

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
        getApplication().addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_auth_phone_number, container, false);

        countryCode = (EditText) view.findViewById(R.id.country_code);
        number = (EditText) view.findViewById(R.id.phone_number);

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
}
