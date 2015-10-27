package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.drinkless.td.libcore.telegram.TdApi;


/**
 * A simple {@link Fragment} subclass.
 */
public class AuthCodeFragment extends Fragment implements Acceptable, ObserverApplication.OnErrorObserver{


    private EditText passCode;

    public ObserverApplication getApplication(){
        return (ObserverApplication) getActivity().getApplication();
    }

    public AuthCodeFragment() {
        // Required empty public constructor
    }

    public static AuthCodeFragment newInstance() {

        Bundle args = new Bundle();

        AuthCodeFragment fragment = new AuthCodeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getApplication().addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_auth_code, container, false);

        passCode = (EditText)view.findViewById(R.id.passCode);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        getApplication().removeObserver(this);
    }

    @Override
    public void accept() {
        getApplication().sendRequest(new TdApi.SetAuthCode(passCode.getText().toString()));
    }

    @Override
    public void proceed(TdApi.Error err) {
        // TODO show that passCode is wrong
    }
}
