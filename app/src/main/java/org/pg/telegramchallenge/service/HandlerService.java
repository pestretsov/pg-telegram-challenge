package org.pg.telegramchallenge.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.ObserverApplication;
import org.pg.telegramchallenge.R;

import java.io.File;

/**
 * Created by roman on 24.10.15.
 */
public class HandlerService extends Service implements Client.ResultHandler {

    private static final String TAG = HandlerService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        ObserverApplication application = (ObserverApplication)getApplicationContext();

        // when service is created, requests from pull can be invoked
        application.invokeRequestPool();

        Toast.makeText(HandlerService.this, "Service is launched", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onResult(TdApi.TLObject object) {
        // TODO implement notifications
        Log.i(TAG, object.toString());
    }
}
