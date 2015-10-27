package org.pg.telegramchallenge;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.service.HandlerService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by roman on 22.10.15.
 */
public class ObserverApplication extends Application implements Client.ResultHandler {

    public static volatile Context appContext;
    public static final String TAG = ObserverApplication.class.getSimpleName();

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (Exception e) {
            Log.v("TLibrary", e.getMessage());
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(ObserverApplication.this, "Application is created!", Toast.LENGTH_SHORT).show();

        appContext = getApplicationContext();

        handler = new Handler();
        startService(new Intent(this, HandlerService.class));
    }

//    private static final List<Class> interfaces;
//    static {
//        interfaces = new LinkedList<>();
//        interfaces.add(OnAuthObserver.class);
//        interfaces.add(OnErrorObserver.class);
//    }

    private volatile List<OnErrorObserver> onErrorObservers = new LinkedList<>();

    public interface OnErrorObserver {
        void proceed(TdApi.Error err);
    }

    private volatile List<OnAuthObserver> onAuthObservers = new LinkedList<>();

    public interface OnAuthObserver {
        void proceed(TdApi.AuthState obj);
    }

    /** for future
     * there can be multimap of classes of tdlib
     * assosiated with observers which have to get
     * objects of this type*/

    private Map<Class, List> observersLists = new HashMap<>();
    {
        observersLists.put(OnAuthObserver.class, onAuthObservers);
        observersLists.put(OnErrorObserver.class, onErrorObservers);
    }

    /** i think that one method for all observers is better
     * than one for each type of them*/
    public void addObserver(Object obs) {
        if (obs instanceof OnAuthObserver)
            onAuthObservers.add((OnAuthObserver) obs);

        if (obs instanceof OnErrorObserver)
            onErrorObservers.add((OnErrorObserver) obs);
    }

    public void removeObserver(Object obs) {
        if (obs instanceof OnAuthObserver)
            onAuthObservers.remove(obs);

        if (obs instanceof OnErrorObserver)
            onErrorObservers.remove(obs);
    }


    private List <TdApi.TLFunction> requestPull = new LinkedList<>();

    public void sendRequest(TdApi.TLFunction request) {
        if (TG.getClientInstance() == null) {
            requestPull.add(request);
        } else {
            TG.getClientInstance().send(request, this);
        }
    }

    public boolean invokeRequestPull() {
        if (TG.getClientInstance() == null)
            return false;

        for (TdApi.TLFunction function: requestPull) {
            TG.getClientInstance().send(function, this);
        }

        requestPull.clear();

        return true;
    }

    Handler handler = new Handler();

    @Override
    public void onResult(TdApi.TLObject object) {
        handler.post(new HandlerRunnable(object));
        Log.i(TAG, object.toString());
    }

    private class HandlerRunnable implements Runnable{

        final TdApi.TLObject object;
        public HandlerRunnable(TdApi.TLObject object){
            this.object = object;
        }

        @Override
        public void run() {

            if (object instanceof TdApi.AuthState) {
                for (OnAuthObserver observer : onAuthObservers) {
                    observer.proceed((TdApi.AuthState) object);
                }
            }

            if (object instanceof TdApi.Error) {
                for (OnErrorObserver observer : onErrorObservers) {
                    observer.proceed((TdApi.Error) object);
                }
            }
        }
    }
}
