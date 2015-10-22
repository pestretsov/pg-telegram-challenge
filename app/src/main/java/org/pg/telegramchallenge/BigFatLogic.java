package org.pg.telegramchallenge;

import android.app.Application;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artemypestretsov on 10/18/15.
 */
public class BigFatLogic extends Application implements Client.ResultHandler {

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (Exception e) {
            Log.v("TLibrary", e.getMessage());
        };
    }

    private static final String TAG = BigFatLogic.class.getSimpleName();
//    public static Messenger mMessenger = new Messenger(new BigFatHandler());

//
//    static class BigFatHandler extends Handler implements Client.ResultHandler {
//
//        private Messenger fromMessanger;
//
//        @Override
//        public void handleMessage(Message msg) {
//            fromMessanger = msg.replyTo;
//
//            if (msg.obj instanceof  TdApi.TLFunction) {
//                TdApi.TLFunction func = (TdApi.TLFunction) msg.obj;
//                Log.e(TAG, "handleMessage");
//                TG.getClientInstance().send(func, this);
//            }
//        }
//
//        @Override
//        public void onResult(TdApi.TLObject object) {
//
//
//
//
//            Message message = new Message();
//            message.obj = object;
//            Log.e(TAG, object.toString());
//            try {
//                Log.e(TAG, "BigFatHandler.onResult");
//                fromMessanger.send(message);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    static private List<OnAuthObserver> onAuthObservers = new ArrayList<>();
    public interface OnAuthObserver {
    }

    public interface Observer {
        void result(Object obj);
    }

    static private List<OnNewMessage> onNewMessageObservers = new ArrayList<>();
    public interface OnNewMessage {
    }

    public static void registerObservers(OnAuthObserver observer) {
        onAuthObservers.add(observer);
        Log.e(TAG, "registerOnAuthObserver");

    }

    public static void registerObservers(OnNewMessage observer) {
        onNewMessageObservers.add(observer);
        Log.e(TAG, "registerOnNewMessage");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate");

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TDB";
        TG.setDir(dir);
        TG.setUpdatesHandler(this);
    }

    @Override
    public void onResult(TdApi.TLObject object) {
    }

    public static void send(TdApi.TLFunction func) {
        Log.e(TAG, "handleMessage");
        TG.getClientInstance().send(func, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                for (OnAuthObserver auth:
                     onAuthObservers) {
                    ((Observer)auth).result(object);
                }
            }
        });
    }
}
