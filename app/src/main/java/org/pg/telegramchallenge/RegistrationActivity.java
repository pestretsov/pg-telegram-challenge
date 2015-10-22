package org.pg.telegramchallenge;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.TdApi;

public class RegistrationActivity extends AppCompatActivity implements BigFatLogic.Observer, FragmentHandler, BigFatLogic.OnNewMessage {

    private static final String TAG = RegistrationActivity.class.getSimpleName();

//    private Handler mHandler = new ActivityHandler();
//    private Messenger mMessenger = new Messenger(mHandler);

    FragmentManager mFragmentManager = getFragmentManager();

    @Override
    public void replaceFragment(Class fragmentClass) {
        try {
            Fragment fragment = (Fragment)fragmentClass.newInstance();
            // CLEAR BACKSTACK:
            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mFragmentManager.beginTransaction().replace(R.id.fragment_holder, fragment).addToBackStack(null).commit();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void replaceFragmentFromTLObject(TdApi.TLObject tlObject) {
//        try {
//            Message message = Message.obtain();
//            message.obj = tlObject;
//            message.replyTo = mMessenger;
//            BigFatLogic.mMessenger.send(message);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void addNewFragmentToBackstack(Class fragmentClass) {
        // TODO: MANAGE THIS
    }

    @Override
    public void result(Object obj) {
        if (!(obj instanceof TdApi.TLObject)) {
            return;
        }

        Log.e(TAG, "RESULT");

        switch (((TdApi.TLObject) obj).getConstructor()) {
            case TdApi.AuthStateWaitPhoneNumber.CONSTRUCTOR:
                replaceFragment(AuthFragment.class);
                break;
            case TdApi.AuthStateWaitCode.CONSTRUCTOR:
                replaceFragment(EnterCodeFragment.class);
                break;
            case TdApi.AuthStateOk.CONSTRUCTOR:
                replaceFragment(DialogListFragment.class);
                break;
            case TdApi.Error.CONSTRUCTOR:
                Log.e(TAG, "ERROR");
            default:
                Log.e(TAG, "NOT HANDELED YET");
                break;
        }
    }

//    private class ActivityHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            Log.e(TAG, "handleMessage");
//
//            // something went wrong
//            if (!(msg.obj instanceof TdApi.TLObject)) {
//                return;
//            }
//
//            switch (((TdApi.TLObject) msg.obj).getConstructor()) {
//                case TdApi.AuthStateWaitPhoneNumber.CONSTRUCTOR:
//                    replaceFragment(AuthFragment.class);
//                    break;
//                case TdApi.AuthStateWaitCode.CONSTRUCTOR:
//                    replaceFragment(EnterCodeFragment.class);
//                    break;
//                case TdApi.AuthStateOk.CONSTRUCTOR:
//                    replaceFragment(DialogListFragment.class);
//                    break;
//                case TdApi.Error.CONSTRUCTOR:
//                    Log.e(TAG, "ERROR");
//                default:
//                    Log.e(TAG, "NOT HANDELED YET");
//                    break;
//            }
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate");

        BigFatLogic.registerObservers((BigFatLogic.OnAuthObserver) this);
        BigFatLogic.registerObservers((BigFatLogic.OnNewMessage)this);

    }

    @Override
    protected void onStart() {
        super.onStart();

//        Message message = new Message();
//        message.replyTo = mMessenger;
//        message.obj = new TdApi.GetAuthState();

        BigFatLogic.send(new TdApi.GetAuthState());

//        try {
//            BigFatLogic.mMessenger.send(message);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

        Log.e(TAG, "onStart");
    }
}
