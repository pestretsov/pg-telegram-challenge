package org.pg.telegramchallenge;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
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

    private ActionBar actionBar = null;

    private Fragment getCurrentFragment() {
        return fragmentManager.findFragmentById(R.id.base_fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
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

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
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

    private void replaceCurrentFragment(Class<EmptyFragment> fragmentClass) {
    }

    @Override
    public void proceed(TdApi.AuthState obj) {

        switch (obj.getConstructor()) {
            case TdApi.AuthStateWaitPhoneNumber.CONSTRUCTOR:
                actionBar.setTitle(R.string.phone_number_title);

                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.base_fragment, AuthPhoneNumberFragment.newInstance());
//                fragmentTransaction.commit();

                try {
                    fragmentTransaction.replace(R.id.base_fragment, AuthPhoneNumberFragment.class.newInstance());
                    fragmentTransaction.commit();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;

            case TdApi.AuthStateWaitCode.CONSTRUCTOR:

                actionBar.setTitle(R.string.activation_code_title);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.base_fragment, AuthCodeFragment.newInstance())
                        .addToBackStack(AuthCodeFragment.class.getName());
                fragmentTransaction.commit();

                break;

            case TdApi.AuthStateOk.CONSTRUCTOR:
                fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.base_fragment, EmptyFragment.newInstance());
                fragmentTransaction.commit();

                break;

            case TdApi.AuthStateWaitName.CONSTRUCTOR:
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.base_fragment, AuthNameFragment.newInstance())
                        .addToBackStack(AuthNameFragment.class.getName());
                fragmentTransaction.commit();

            default:
                Toast.makeText(MainActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void proceed(TdApi.Error err) {
        Toast.makeText(MainActivity.this, err.text, Toast.LENGTH_SHORT).show();
    }
}
