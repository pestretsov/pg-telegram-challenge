package org.pg.telegramchallenge;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

public class PhoneNumberFragment extends Fragment implements iConfirmable {
    private TextView countryCode;
    private TextView phoneNumber;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone_number, container, false);

        countryCode = (TextView)view.findViewById(R.id.countryCode);
        phoneNumber = (TextView)view.findViewById(R.id.phoneNumber);

        Activity activity = getActivity();

        if (!(activity instanceof iHostMessenger)) {
            throw new InstantiationException("Attached to wrong activity", null);
        }

        if (activity instanceof ActionBarActivity) {
            activity.getActionBar().setTitle(R.string.enterNumberTitle);
            activity.getActionBar().setDisplayHomeAsUpEnabled(false);
        } else if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity)activity).getSupportActionBar().setTitle(R.string.enterNumberTitle);
            ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }


        return view;
    }

    @Override
    public void unconfirmed(Object o) {
        //TODO should show that number is wrong
    }

    @Override
    public void confirm() {
        String number = countryCode.getText().toString() + phoneNumber.getText().toString();

        Message msg = Message.obtain();
        msg.obj = new TdApi.SetAuthPhoneNumber(number);

        ((iHostMessenger)getActivity()).sendMessage(msg);
    }
}
