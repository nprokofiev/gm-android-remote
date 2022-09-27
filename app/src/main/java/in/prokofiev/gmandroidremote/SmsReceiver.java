package in.prokofiev.gmandroidremote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import in.prokofiev.gmandroidremote.commands.CommandExecutor;

public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    private static final int WAIT_LOCK = 15000; //15 sec
    private static final long SMS_TIMEOUT = 120000; //2 mins
    private static final String TAG = "SmsReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED)) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "GmAndroidRemote::WakelockTag");
            wakeLock.acquire(WAIT_LOCK);
            CommandExecutor commandExecutor = GmAndroidRemoteApplication.getCommandExecutor(context);
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // get sms objects
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                // large message might be broken into many
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                }
                String sender = messages[0].getOriginatingAddress();
                long smsSentTime = messages[0].getTimestampMillis();
                if(checkMessageStillValid(smsSentTime)){
                    Log.i(TAG, "sms too old "+sender);
                    return;
                }
                Set<String> numbers = getAllowedNumbers(context);
                if(!numbers.contains(sender)){
                    Log.i(TAG, "number not allowed "+sender);
                    return;
                }
                String message = sb.toString();
                commandExecutor.runCommandByName(message);
            }
        }
    }

    private Set<String> getAllowedNumbers(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String numbers = sp.getString(SettingsActivity.SMS_NUMBERS, "");
        String[] numbersArray = numbers.split(",");
        return new HashSet<>(Arrays.asList(numbersArray));

    }

    private boolean checkMessageStillValid(long timeSmsSent){
        return System.currentTimeMillis() - timeSmsSent > SMS_TIMEOUT;
    }
}
