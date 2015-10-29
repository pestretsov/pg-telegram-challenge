package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.TdApi;


/**
 * A simple {@link Fragment} subclass.
 */
public class AuthNameFragment extends Fragment implements Acceptable, ObserverApplication.OnErrorObserver{

    private ActionBar actionBar;

    private EditText firstName, secondName;

    public AuthNameFragment() {
        // Required empty public constructor
    }

    public static AuthNameFragment newInstance() {

        Bundle args = new Bundle();

        AuthNameFragment fragment = new AuthNameFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_auth_name, container, false);

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        firstName = (EditText) view.findViewById(R.id.first_name);
        secondName = (EditText)view.findViewById(R.id.second_name);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        actionBar.setTitle(R.string.auth_name_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void proceed(TdApi.Error err) {
        Toast.makeText(getContext(), err.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void accept() {
        ObserverApplication app = (ObserverApplication) getActivity().getApplication();

        if (firstName.getText().length() == 0) {
            Toast.makeText(getContext(), "Invalid first name", Toast.LENGTH_SHORT).show();
            return;
        }

        app.sendRequest(new TdApi.SetAuthName(firstName.getText().toString(), secondName.getText().toString()));
    }
}
