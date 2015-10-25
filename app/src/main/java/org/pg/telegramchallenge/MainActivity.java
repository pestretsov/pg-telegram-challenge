package org.pg.telegramchallenge;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.service.HandlerService;

import static org.pg.telegramchallenge.ObserverApplication.OnAuthObserver;

public class MainActivity extends AppCompatActivity implements OnAuthObserver, ObserverApplication.OnErrorObserver{

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FragmentTransaction fragmentTransaction = null;

    private Fragment getCurrentFragment() {
        return fragmentManager.findFragmentById(R.id.base_fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatemen
        if (id == R.id.action_accept){
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof Acceptable) {
                ((Acceptable) currentFragment).accept();
            } else {
                ((ObserverApplication)getApplication()).sendRequest(new TdApi.ResetAuth(false));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ObserverApplication application = (ObserverApplication) getApplication();

        application.addObserver(this);

        application.sendRequest(new TdApi.GetAuthState());
    }

    @Override
    protected void onPause() {
        super.onPause();

        ObserverApplication application = (ObserverApplication) getApplication();
        application.removeObserver(this);
    }

    @Override
    public void proceed(TdApi.AuthState obj) {

        switch (obj.getConstructor()) {
            case TdApi.AuthStateWaitPhoneNumber.CONSTRUCTOR:
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.base_fragment, AuthPhoneNumberFragment.newInstance());
                fragmentTransaction.commit();
                break;

            case TdApi.AuthStateWaitCode.CONSTRUCTOR:

                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.base_fragment, AuthCodeFragment.newInstance()).addToBackStack(null);
                fragmentTransaction.commit();

                break;

            case TdApi.AuthStateOk.CONSTRUCTOR:
                fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                if (getCurrentFragment() instanceof EmptyFragment)
                    break;

                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.base_fragment, EmptyFragment.newInstance());
                fragmentTransaction.commit();

                break;
        }
    }

    @Override
    public void proceed(TdApi.Error err) {
        Toast.makeText(MainActivity.this, err.text, Toast.LENGTH_SHORT).show();
    }
}
