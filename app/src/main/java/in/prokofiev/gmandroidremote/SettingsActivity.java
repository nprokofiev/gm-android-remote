package in.prokofiev.gmandroidremote;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity  implements ActivityCompat.OnRequestPermissionsResultCallback  {

    public static final String BLUETOOTH_DEVICE = "bluetooth_device";
    public static final String ENABLE_SMS_CONTROL = "enable_sms_remote";
    public static final String SMS_NUMBERS = "sms_numbers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }






    public static class SettingsFragment extends PreferenceFragmentCompat {
        public static final int SMS_PERMISSION_REQUEST = 0;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            ListPreference pref = findPreference(BLUETOOTH_DEVICE);
            populateDeviceList(pref);
            SwitchPreferenceCompat switchPreference = findPreference(ENABLE_SMS_CONTROL);
            switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(ActivityCompat.checkSelfPermission(SettingsFragment.this.getActivity(),
                            Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.RECEIVE_SMS},
                                SMS_PERMISSION_REQUEST);
                        return false;
                    }

                    return true;
                }
            });
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults){

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(this.getView(), R.string.read_sms_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {

                Snackbar.make(this.getView(), R.string.read_sms_permission_required,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }

        private void populateDeviceList(ListPreference pref){
            BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBtAdapter!=null && mBtAdapter.isEnabled()) {

                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

                if (pairedDevices != null && !pairedDevices.isEmpty()) {
                    String[] keys = new String[pairedDevices.size()];
                    String[] values = new String[pairedDevices.size()];
                    int i = 0;
                    for (BluetoothDevice device : pairedDevices) {
                        final String address = device.getAddress();
                        keys[i] = device.getName() + " | " + address;
                        values[i] = address;
                        i++;
                    }
                    pref.setEntries(keys);
                    pref.setEntryValues(values);
                    return;
                }
            }

                pref.setEnabled(false);

        }
    }
}