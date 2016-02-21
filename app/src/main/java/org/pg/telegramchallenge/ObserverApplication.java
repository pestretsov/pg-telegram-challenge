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

import java.io.File;
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

    public static final int PRIVATE_CHAT_INFO = 0, GROUP_CHAT_INFO = 1, SECRET_CHAT_INFO = 2, CHANNEL_CHAT_INFO = 3;
    private static final int LOG_ERROR = 1, LOG_VERBOSE = 5, LOG_ASSERT = 0, LOG_WARNING = 2, LOG_INFO = 3, LOG_DEBUG = 4;

    public static volatile TdApi.User userMe;

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


        final String dir = getExternalFilesDir(null).getAbsolutePath()
                + File.separator
                + getString(R.string.db_folder_name);

        File dirFile = new File(dir);
        if (!dirFile.exists()){
            dirFile.mkdirs();
        }

        TG.setDir(dir);
        TG.setUpdatesHandler(this);
        TG.setFileLogEnabled(false);
        TG.setLogVerbosity(LOG_ERROR);

        Log.e(TAG, "onCreate");
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

    private volatile List<OnUpdateChatOrderObserver> onUpdateChatOrderObservers = new LinkedList<>();

    public interface OnUpdateChatOrderObserver {
        void proceed(TdApi.UpdateChatOrder obj);
    }

    private volatile List<ChatsObserver> chatsObservers = new LinkedList<>();

    public interface ChatsObserver {
        void proceed(TdApi.Chats obj);
    }

    private volatile List<ChatObserver> chatObservers = new LinkedList<>();

    public interface ChatObserver {
        void proceed(TdApi.Chat obj);
    }

    private volatile List<ConcreteChatObserver> concreteChatObservers = new LinkedList<>();

    public interface ConcreteChatObserver {
        long getChatId();
        void proceedConcrete(TdApi.UpdateNewMessage obj);
    }

    private volatile List<OnUpdateFileObserver> onUpdateFileObservers = new LinkedList<>();
    public interface OnUpdateFileObserver {
        void proceed(TdApi.UpdateFile obj);
    }

    private volatile List<OnUpdateChatPhotoObserver> onUpdateChatPhotoObservers = new LinkedList<>();
    public interface OnUpdateChatPhotoObserver {
        void proceed(TdApi.UpdateChatPhoto obj);
    }

    private volatile List<OnGetChatHistoryObserver> onGetChatHistoryObservers = new LinkedList<>();
    public interface OnGetChatHistoryObserver {
        void proceed(TdApi.Messages obj);
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

        if (obs instanceof OnUpdateChatPhotoObserver) {
            onUpdateChatPhotoObservers.add((OnUpdateChatPhotoObserver) obs);
        }

        if (obs instanceof OnUpdateChatOrderObserver) {
            onUpdateChatOrderObservers.add((OnUpdateChatOrderObserver) obs);
        }

        if (obs instanceof ChatsObserver) {
            chatsObservers.add((ChatsObserver) obs);
        }

        if (obs instanceof ChatObserver) {
            chatObservers.add((ChatObserver) obs);
        }

        if (obs instanceof OnGetChatHistoryObserver) {
            onGetChatHistoryObservers.add((OnGetChatHistoryObserver) obs);
        }

        if (obs instanceof ConcreteChatObserver) {
            concreteChatObservers.add((ConcreteChatObserver) obs);
        }
    }

    public boolean isSubscribed(Object obj) {
        if (obj instanceof ChatObserver && chatObservers.contains(obj)) {
            return true;
        }

        if (obj instanceof OnUpdateNewMessageObserver && onUpdateNewMessageObservers.contains(obj)) {
            return true;
        }

        return false;
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

        if (obs instanceof OnUpdateChatPhotoObserver) {
            onUpdateChatPhotoObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatOrderObserver) {
            onUpdateChatOrderObservers.remove(obs);
        }

        if (obs instanceof ChatsObserver) {
            chatsObservers.remove(obs);
        }

        if (obs instanceof ChatObserver) {
            chatObservers.remove(obs);
        }

        if (obs instanceof OnGetChatHistoryObserver) {
            onGetChatHistoryObservers.remove(obs);
        }

        if (obs instanceof ConcreteChatObserver) {
            concreteChatObservers.remove(obs);
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

    public void sendRequest(TdApi.TLFunction request, Client.ResultHandler handler) {
        TG.getClientInstance().send(request, handler);
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

                    for (ConcreteChatObserver observer : concreteChatObservers) {
                        if (observer.getChatId() == ((TdApi.UpdateNewMessage) object).message.chatId) {
                            observer.proceedConcrete((TdApi.UpdateNewMessage) object);
                        }
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

                if (object instanceof TdApi.UpdateChatPhoto) {
                    for (OnUpdateChatPhotoObserver observer : onUpdateChatPhotoObservers) {
                        observer.proceed((TdApi.UpdateChatPhoto) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateChatOrder) {
                    for (OnUpdateChatOrderObserver observer : onUpdateChatOrderObservers) {
                        observer.proceed((TdApi.UpdateChatOrder) object);
                    }
                    return;
                }



                // TODO: REVISE
//                if (object instanceof TdApi.UpdateUser) {
//                    if (userMe == null) {
//                        userMe = ((TdApi.UpdateUser) object).user;
//                    }
//                    return;
//                }

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

            if (object instanceof TdApi.Messages) {
                for (OnGetChatHistoryObserver observer : onGetChatHistoryObservers) {
                    observer.proceed((TdApi.Messages) object);
                }
                return;
            }
        }
    }
}
