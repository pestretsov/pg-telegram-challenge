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

public class EnterCodeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_code, container, false);

        final EditText codeField = (EditText)view.findViewById(R.id.codeField);
        codeField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!event.isShiftPressed()) {
                        String code = codeField.getText().toString();
                        ((FragmentHandler)getActivity()).replaceFragmentFromTLObject(new TdApi.SetAuthCode(code));

                        return true;
                    }
                }
                return false;
            }
        });


        return view;
    }
}
