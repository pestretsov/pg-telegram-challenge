package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class AuthCodeFragment extends Fragment implements Acceptable, ObserverApplication.OnErrorObserver {

    private EditText passCode;

    private Toolbar toolbar;
    private View acceptActionBarIcon;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_auth_code, container, false);


        toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle(R.string.auth_phone_number_title);
            toolbar.inflateMenu(R.menu.menu_main);

            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setPadding(0, Utils.getStatusBarHeight(getContext()), 0, 0);
        }

        acceptActionBarIcon = getActivity().findViewById(R.id.action_accept);

        passCode = (EditText)view.findViewById(R.id.passCode);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (toolbar != null) {
            toolbar.setTitle(R.string.auth_activation_code_title);
        }

        if (acceptActionBarIcon!=null) {
            acceptActionBarIcon.setVisibility(View.VISIBLE);
        }

        getApplication().addObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getApplication().removeObserver(this);
    }

    @Override
    public void accept() {
        getApplication().sendRequest(new TdApi.CheckAuthCode(passCode.getText().toString()));
    }

    @Override
    public void proceed(TdApi.Error err) {
        // TODO show that passCode is wrong
    }
}
