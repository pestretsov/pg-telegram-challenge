package org.pg.telegramchallenge;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

public class PassCodeFragment extends Fragment implements iConfirmable{

    private TextView passCode;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pass_code, container, false);
        passCode = (TextView)view.findViewById(R.id.passCode);

        Activity activity = getActivity();

        if (!(activity instanceof iHostMessenger)) {
            throw new InstantiationException("Attached to wrong activity", null);
        }

        if (activity instanceof ActionBarActivity) {
            activity.getActionBar().setTitle(R.string.enterPassCodeTitle);
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        } else if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity)activity).getSupportActionBar().setTitle(R.string.enterPassCodeTitle);
            ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        return view;
    }

    @Override
    public void confirm() {

        Message codeMsg = Message.obtain();
        codeMsg.obj = new TdApi.SetAuthCode(passCode.getText().toString());

        ((iHostMessenger)getActivity()).sendMessage(codeMsg);
    }

    @Override
    public void unconfirmed(Object o) {

    }
}
