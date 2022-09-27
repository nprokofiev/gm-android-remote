package in.prokofiev.gmandroidremote.commands;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.ArrayDeque;

import in.prokofiev.gmandroidremote.MainActivity;
import in.prokofiev.gmandroidremote.SettingsActivity;

public class CommandExecutor {


    private ArrayDeque<Command> commandsToRun = new ArrayDeque<>();
    private final static String TAG = "CommandExecutor";
    private final Context context;
    private DeviceConnector connector;
    private Handler uiHandler;
    private boolean disconnectOnFinish;
    private final Handler bluetoothHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            CommandExecutor.this.handleMessage(msg);
        }
    };
    private CountDownTimer countDownTimer;


    public CommandExecutor(Context context) {
        this.context = context;
    }


    public void runCommands(String cmds, boolean disconnect_on_finish) {
        this.disconnectOnFinish = disconnect_on_finish;
        commandsToRun.clear();
        String[] commands = cmds.split("\n");
        for (String command : commands) {
            String line = command.replace("\n", "") + "\r";
            Command cmd = new Command(line, 100);
            commandsToRun.push(cmd);
        }
        if (!isConnected()) {
            setupConnector();
            return;
        }
        runNextCommand();
    }

    public void runCommandByName(String commandCode){
        CommandRepository commandRepository = new CommandRepository(context);
        String commands = commandRepository.getCommandByName(commandCode);
        if(commands!=null)
            runCommands(commands, true);
        else{
            Log.i(TAG, "no such command: " + commandCode);
        }
    }
    public void connect() {
        setupConnector();
    }
    public void disconnect(){
        if(connector!=null) {
            connector.stop();
        }
    }

    public boolean isConnected(){
        return !(connector == null || connector.getState() != DeviceConnectorImpl.STATE_CONNECTED);
    }

    private void runNextCommand() {
        Command cmd = commandsToRun.pollLast();
        if(cmd==null) {
            Log.i(TAG, "commands finished");
            if(disconnectOnFinish) {
                connector.stop();
            }
            return;
        }
        connector.write(cmd.getCommand().getBytes());
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CommandTimer(cmd.getDelay(), cmd.getDelay());
        countDownTimer.start();
    }

    private class CommandTimer extends CountDownTimer {
        public CommandTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            //noop
        }

        @Override
        public void onFinish() {
            runNextCommand();
        }
    }

    private void setupConnector() {
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            Log.e(TAG, "no bluetooth adapter");
            return;
        }


        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String deviceAddress = sp.getString(SettingsActivity.BLUETOOTH_DEVICE, "");

            if (deviceAddress == null || deviceAddress.isEmpty()) {
                Log.e(TAG, "no bluetooth device");
                return;
            }
            connector = new DeviceConnectorImpl(deviceAddress, bluetoothHandler);
            connector.connect();
            Log.i(TAG, "connecting to "+deviceAddress);
        } catch (IllegalArgumentException e) {
            Log.e(MainActivity.class.getCanonicalName(),
                    "setupConnector failed: " + e.getMessage());
        }
    }

    private void handleMessage(@NonNull Message msg) {
        sendMsgToUi(msg.what);
        switch (msg.what) {
            case DeviceConnectorImpl.STATE_CONNECTED:
                Log.d(TAG, "connected successfully");
                runNextCommand();
                break;
            case DeviceConnectorImpl.STATE_CONNECTING:
                Log.d(TAG, "trying to connect..");
                //    no op
                break;
            case DeviceConnectorImpl.STATE_NONE:
                Log.d(TAG, "disconnected");
                break;
        }
    }

    public void setUiHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    private void sendMsgToUi(int msg){
        if(uiHandler!=null)
            uiHandler.obtainMessage(msg).sendToTarget();
    }


}
