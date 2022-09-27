package in.prokofiev.gmandroidremote.commands;

import android.os.Handler;
import android.util.Log;

public class DummyDeviceConnector implements DeviceConnector{
    private Handler handler;
    private int state;

    public DummyDeviceConnector(String address, Handler handler) {
        this.handler = handler;
    }

    @Override
    public void connect() {
        state = DeviceConnectorImpl.STATE_CONNECTED;
        handler.obtainMessage(DeviceConnectorImpl.STATE_CONNECTED).sendToTarget();
    }

    @Override
    public void stop() {
        state = DeviceConnectorImpl.STATE_NONE;
        handler.obtainMessage(DeviceConnectorImpl.STATE_NONE).sendToTarget();
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void write(byte[] data) {
        Log.i("DummyDeviceConnector", new String(data));
    }
}
