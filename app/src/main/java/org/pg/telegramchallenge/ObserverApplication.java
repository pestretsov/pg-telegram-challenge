package org.pg.telegramchallenge;

import android.app.Application;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by roman on 22.10.15.
 */
public class ObserverApplication extends Application implements Client.ResultHandler {
    static {
        try {
            System.loadLibrary("tdjni");
        } catch (Exception e) {
            Log.v("TLibrary", e.getMessage());
        };
    }

    public static final String TAG = ObserverApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + getString(R.string.db_folder_name);

        File dirFile = new File(dir);
        if (!dirFile.exists()){
            dirFile.mkdirs();
        }

        TG.setDir(dir);
        TG.setUpdatesHandler(this);
    }

//    private static final List<Class> interfaces;
//    static {
//        interfaces = new LinkedList<>();
//        interfaces.add(OnAuthObserver.class);
//        interfaces.add(OnErrorObserver.class);
//    }

    private List<OnErrorObserver> onErrorObservers = new LinkedList<>();

    public interface OnErrorObserver {
        void proceed(TdApi.Error err);
    }

    private List<OnAuthObserver> onAuthObservers = new LinkedList<>();

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

    public void sendRequest(TdApi.TLFunction request) {
        TG.getClientInstance().send(request, this);
    }

    Handler handler = new Handler();

    @Override
    public void onResult(final TdApi.TLObject object) {
        handler.post(new HandlerRunnable(object));
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
