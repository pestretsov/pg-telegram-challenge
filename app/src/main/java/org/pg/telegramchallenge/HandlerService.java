package org.pg.telegramchallenge;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;

/**
 * Created by roman on 16.10.15.
 */
public class HandlerService extends Service implements Client.ResultHandler{
    private static final String TAG = HandlerService.class.getName();

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (Exception e) {
            Log.v("TLibrary", e.getMessage());
        }
    }

    private class SingleRequestHandler implements Client.ResultHandler {
        // messages are inconsistable, you cannot transmit them between threads
        private final Messenger replyTo;

        SingleRequestHandler(Messenger messenger) {
            replyTo = messenger;
        }
        @Override
        public void onResult(TdApi.TLObject object) {
            Message reply = Message.obtain();
            reply.replyTo = mMessenger;
            reply.obj = object;

            try {
                replyTo.send(reply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Message reply = Message.obtain();

            reply.replyTo = mMessenger;

            if (msg.obj instanceof TdApi.TLFunction) {
                TG.getClientInstance().send((TdApi.TLFunction)(msg.obj), new SingleRequestHandler(msg.replyTo));
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "in onCreate!");
        Toast.makeText(this, "on Create!", Toast.LENGTH_LONG).show();


        File telegramFolder = new File(Environment.getExternalStorageDirectory() + "/TGDir");

        if (!telegramFolder.exists()) {
            telegramFolder.mkdir();
        }

        String dir = telegramFolder.getAbsolutePath();
        TG.setDir(dir);
        TG.setUpdatesHandler(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "Someone is trying to bind", Toast.LENGTH_LONG).show();
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "Unbinding", Toast.LENGTH_LONG).show();
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "is started by onStartCommand!");
        Toast.makeText(this, "on Start Command", Toast.LENGTH_LONG).show();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "is destroyed!");
    }

    @Override
    public void onResult(TdApi.TLObject object) {
        // here would be handled only updates!
        // all requests with desired answers should go to
        // SimpleRequestHandler
    }
}
