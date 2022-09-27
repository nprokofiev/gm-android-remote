package in.prokofiev.gmandroidremote.commands;

public interface DeviceConnector {
    void connect();
    void stop();
    int getState();
    void write(byte[] data);
}
