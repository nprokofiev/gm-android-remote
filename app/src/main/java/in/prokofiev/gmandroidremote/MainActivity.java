package in.prokofiev.gmandroidremote;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.preference.PreferenceManager;

import in.prokofiev.gmandroidremote.commands.CommandExecutor;
import in.prokofiev.gmandroidremote.commands.CommandRepository;
import in.prokofiev.gmandroidremote.commands.DeviceConnectorImpl;
import in.prokofiev.gmandroidremote.commands.FobCommands;
import in.prokofiev.gmandroidremote.commands.Set1FobCommands;
import in.prokofiev.gmandroidremote.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    // Message types sent from the DeviceConnector Handler



    private ActivityMainBinding binding;


    private static final FobCommands fobCommands = new Set1FobCommands();
    private static CommandExecutor commandExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        LinearLayout ll = findViewById(R.id.buttons_layout);
        ll.addView(new TextView(this));
        CommandRepository commandRepository = new CommandRepository(this);
        try(Cursor cmdsCursor = commandRepository.getCommands()) {
            while (cmdsCursor.moveToNext()) {
                String commandName = cmdsCursor.getString(0);
                String commands = cmdsCursor.getString(1);
                Button btn = new Button(this);
                btn.setText(commandName);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        runCommand(commands);
                    }
                });
                ll.addView(btn);
            }
        }




        GmAndroidRemoteApplication application  = (GmAndroidRemoteApplication) getApplication();
        commandExecutor = application.getCommandExecutor();
        ResponseHandler mHandler = new ResponseHandler(this);
        commandExecutor.setUiHandler(mHandler);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(commandExecutor.isConnected())
                    commandExecutor.disconnect();
                else
                    commandExecutor.connect();
            }
        });



    }

    private void runCommand(String cmds){
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            infoBar("Please enable bluetooth");
            return;
        }

        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String deviceAddress = sp.getString(SettingsActivity.BLUETOOTH_DEVICE, "");

            if (deviceAddress == null || deviceAddress.isEmpty()) {
                infoBar("Please select a paired OBDLink MX device in settings");
                return;
            }

        } catch (IllegalArgumentException e) {
            Log.e(MainActivity.class.getCanonicalName(),
                    "setupConnector failed: " + e.getMessage());
        }

        commandExecutor.runCommands(cmds, false);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void infoBar(String info) {
        Snackbar.make(binding.fab, info,
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void onConnected() {
        if(binding!=null) {
            binding.fab.setBackgroundColor(Color.parseColor("#3758BA"));
        }
    }

    public void onDisconnected() {
        if(binding!=null) {
            binding.fab.setBackgroundColor(Color.parseColor("#FFAFACAC"));
        }
    }



    // ==========================================================================

    /**
     * Обработчик приёма данных от bluetooth-потока
     */
    private static class ResponseHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        public ResponseHandler(MainActivity activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<MainActivity>(activity);
        }

        public void setTarget(MainActivity target) {
            mActivity.clear();
            mActivity = new WeakReference<MainActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {

                Log.i("MESSAGE_STATE_CHANGE", "state=" + msg.what);
                switch (msg.what) {
                        case DeviceConnectorImpl.STATE_CONNECTED:
                            activity.infoBar("Connected");
                            activity.onConnected();
                            break;
                        case DeviceConnectorImpl.STATE_CONNECTING:
                            activity.infoBar("Connecting..");
                            break;
                        case DeviceConnectorImpl.STATE_NONE:
                            activity.infoBar("Disconnected");
                            activity.onDisconnected();
                            break;
                }



            }
        }
    }
}
// ==========================================================================
