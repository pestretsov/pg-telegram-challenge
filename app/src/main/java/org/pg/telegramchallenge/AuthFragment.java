package org.pg.telegramchallenge;


import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

public class AuthFragment extends Fragment {

    public AuthFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_auth, container, false);

        final EditText phoneNumber = (EditText)view.findViewById(R.id.phoneNumber);
        phoneNumber.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!event.isShiftPressed()) {
                        String number = phoneNumber.getText().toString();
                        ((FragmentHandler)getActivity()).replaceFragmentFromTLObject(new TdApi.SetAuthPhoneNumber(number));

                        return true;
                    }
                }
                return false;
            }
        });


        return view;
    }
}
