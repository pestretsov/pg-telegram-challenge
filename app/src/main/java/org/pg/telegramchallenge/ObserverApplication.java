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

    public static volatile TdApi.User userMe = null;

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

    //Fragment should respond:
//
//    UpdateNewMessage - очевидно
//    UpdateUserAction - "$username$ сейчас набирает"
//    UpdateChatReadOutbox - флажки и цифры
//    UpdateChatReadInbox - флажки и цифры
//    UpdateChatTitle - очевидно

    private volatile List<OnUpdateNewMessageObserver> onUpdateNewMessageObservers = new LinkedList<>();

    public interface OnUpdateNewMessageObserver {
        void proceed(TdApi.UpdateNewMessage obj);
    }

    private volatile List<OnUpdateUserActionObserver> onUpdateUserActionObservers = new LinkedList<>();

    public interface OnUpdateUserActionObserver {
        void proceed(TdApi.UpdateUserAction obj);
    }

    private volatile List<OnUpdateChatReadOutboxObserver> onUpdateChatReadOutboxObservers = new LinkedList<>();

    public interface OnUpdateChatReadOutboxObserver {
        void proceed(TdApi.UpdateChatReadOutbox obj);
    }

    private volatile List<OnUpdateChatReadInboxObserver> onUpdateChatReadInboxObservers = new LinkedList<>();

    public interface OnUpdateChatReadInboxObserver {
        void proceed(TdApi.UpdateChatReadInbox obj);
    }

    private volatile List<OnUpdateChatTitleObserver> onUpdateChatTitleObservers = new LinkedList<>();

    public interface OnUpdateChatTitleObserver {
        void proceed(TdApi.UpdateChatTitle obj);
    }

    private volatile List<ChatsObserver> chatsObservers = new LinkedList<>();

    public interface ChatsObserver {
        void proceed(TdApi.Chats obj);
    }

    private volatile List<ChatObserver> chatObservers = new LinkedList<>();

    public interface ChatObserver {
        void proceed(TdApi.Chat obj);
    }

    private volatile List<OnUpdateFileObserver> onUpdateFileObservers = new LinkedList<>();
    public interface OnUpdateFileObserver {
        void proceed(TdApi.UpdateFile obj);
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
        if (obs instanceof OnUpdateFileObserver)
            onUpdateFileObservers.add((OnUpdateFileObserver) obs);

        if (obs instanceof OnAuthObserver)
            onAuthObservers.add((OnAuthObserver) obs);

        if (obs instanceof OnErrorObserver)
            onErrorObservers.add((OnErrorObserver) obs);

        if (obs instanceof OnUpdateNewMessageObserver) {
            onUpdateNewMessageObservers.add((OnUpdateNewMessageObserver) obs);
        }

        if (obs instanceof OnUpdateUserActionObserver) {
            onUpdateUserActionObservers.add((OnUpdateUserActionObserver) obs);
        }

        if (obs instanceof OnUpdateChatReadInboxObserver) {
            onUpdateChatReadInboxObservers.add((OnUpdateChatReadInboxObserver) obs);
        }

        if (obs instanceof OnUpdateChatReadOutboxObserver) {
            onUpdateChatReadOutboxObservers.add((OnUpdateChatReadOutboxObserver) obs);
        }

        if (obs instanceof OnUpdateChatTitleObserver) {
            onUpdateChatTitleObservers.add((OnUpdateChatTitleObserver) obs);
        }

        if (obs instanceof ChatsObserver) {
            chatsObservers.add((ChatsObserver) obs);
        }

        if (obs instanceof ChatObserver) {
            chatObservers.add((ChatObserver) obs);
        }
    }

    public void removeObserver(Object obs) {
        if (obs instanceof OnAuthObserver) {
            onAuthObservers.remove(obs);
        }

        if (obs instanceof OnErrorObserver) {
            onErrorObservers.remove(obs);
        }

        if (obs instanceof OnUpdateNewMessageObserver) {
            onUpdateNewMessageObservers.remove(obs);
        }

        if (obs instanceof OnUpdateUserActionObserver) {
            onUpdateUserActionObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatReadInboxObserver) {
            onUpdateChatReadInboxObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatReadOutboxObserver) {
            onUpdateChatReadOutboxObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatTitleObserver) {
            onUpdateChatTitleObservers.remove(obs);
        }

        if (obs instanceof ChatsObserver) {
            chatsObservers.remove(obs);
        }

        if (obs instanceof ChatObserver) {
            chatObservers.remove(obs);
        }
    }


    private List <TdApi.TLFunction> requestPool = new LinkedList<>();

    public void sendRequest(TdApi.TLFunction request) {
        Log.e(TAG, request.toString());
        if (TG.getClientInstance() == null) {
            requestPool.add(request);
        } else {
            TG.getClientInstance().send(request, this);
        }
    }

    public boolean invokeRequestPool() {
        if (TG.getClientInstance() == null)
            return false;

        for (TdApi.TLFunction function: requestPool) {
            TG.getClientInstance().send(function, this);
        }

        requestPool.clear();

        return true;
    }

    Handler handler = new Handler();

    @Override
    public void onResult(TdApi.TLObject object) {
        handler.post(new HandlerRunnable(object));
        Log.i(TAG, object.toString());
    }

    private class HandlerRunnable implements Runnable {

        final TdApi.TLObject object;
        public HandlerRunnable(TdApi.TLObject object){
            this.object = object;
        }

        @Override
        public void run() {

            if (object instanceof TdApi.Update) {
                if (object instanceof TdApi.UpdateFile) {
                    for (OnUpdateFileObserver observer: onUpdateFileObservers) {
                        observer.proceed((TdApi.UpdateFile) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateNewMessage) {
                    for (OnUpdateNewMessageObserver observer : onUpdateNewMessageObservers) {
                        observer.proceed((TdApi.UpdateNewMessage) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateChatReadInbox) {
                    for (OnUpdateChatReadInboxObserver observer : onUpdateChatReadInboxObservers) {
                        observer.proceed((TdApi.UpdateChatReadInbox) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateChatReadOutbox) {
                    for (OnUpdateChatReadOutboxObserver observer : onUpdateChatReadOutboxObservers) {
                        observer.proceed((TdApi.UpdateChatReadOutbox) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateChatTitle) {
                    for (OnUpdateChatTitleObserver observer : onUpdateChatTitleObservers) {
                        observer.proceed((TdApi.UpdateChatTitle) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateUserAction) {
                    for (OnUpdateUserActionObserver observer : onUpdateUserActionObservers) {
                        observer.proceed((TdApi.UpdateUserAction) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateUser) {
                    if (userMe == null) {
                        userMe = ((TdApi.UpdateUser) object).user;
                    }
                    return;
                }

                return;
            }

            if (object instanceof TdApi.AuthState) {
                for (OnAuthObserver observer : onAuthObservers) {
                    observer.proceed((TdApi.AuthState) object);
                }
                return;
            }

            if (object instanceof TdApi.Error) {
                for (OnErrorObserver observer : onErrorObservers) {
                    observer.proceed((TdApi.Error) object);
                }
                return;
            }

            if (object instanceof TdApi.Chats) {
                for (ChatsObserver observer : chatsObservers) {
                    observer.proceed((TdApi.Chats) object);
                }
                return;
            }

            if (object instanceof TdApi.Chat) {
                for (ChatObserver observer : chatObservers) {
                    observer.proceed((TdApi.Chat) object);
                }
                return;
            }
        }
    }
}
