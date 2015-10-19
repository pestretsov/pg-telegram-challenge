package org.pg.telegramchallenge;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

public class RegistrationActivity extends AppCompatActivity implements BigFatLogic.OnAuth {

    private static final String TAG = RegistrationActivity.class.getSimpleName();

    private Handler mHandler = new ActivityHandler();
    private Messenger mMessenger = new Messenger(mHandler);

    private class ActivityHandler extends Handler {
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();

        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage");
            if (msg.obj instanceof TdApi.AuthStateWaitPhoneNumber) {
                mFragmentTransaction.add(R.id.fragment_holder, new AuthFragment());
                mFragmentTransaction.commit();
            } else if (msg.obj instanceof TdApi.AuthStateWaitCode) {
                mFragmentTransaction.add(R.id.fragment_holder, new EnterCodeFragment());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate");

        BigFatLogic.registerObservers(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Message message = new Message();
        message.replyTo = mMessenger;
        message.obj = new TdApi.GetAuthState();

        try {
            BigFatLogic.mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "onStart");
    }
}
