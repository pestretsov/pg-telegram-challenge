package org.pg.telegramchallenge;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.TdApi;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ServiceConnection conn;

    private Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService = null;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Object o = msg.obj;
            if (msg.obj instanceof TdApi.TLObject) {
                Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Message msg = new Message();
    }

    @Override
    protected void onResume() {
        super.onResume();
        conn = new ServiceConnection() {

            private ComponentName name;
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                this.name = name;
                mService = new Messenger(service);

                Message msg = Message.obtain();
                msg.obj = new TdApi.GetAuthState();
                msg.replyTo = mMessenger;

                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                name = null;
                mMessenger = null;
            }
        };

        Intent intent = new Intent(this, HandlerService.class);
        boolean result = bindService(intent, conn, BIND_AUTO_CREATE);

        Log.d(TAG, Boolean.toString(result));
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
    }
}
