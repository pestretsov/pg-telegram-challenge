package org.pg.telegramchallenge;

import android.content.Intent;
import android.os.Message;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Created by artemypestretsov on 10/20/15.
 */
public interface FragmentHandler {
    void replaceFragment(Class fragmentClass);
    void replaceFragmentFromTLObject(TdApi.TLObject tlObject);
    void addNewFragmentToBackstack(Class fragmentClass);
}
