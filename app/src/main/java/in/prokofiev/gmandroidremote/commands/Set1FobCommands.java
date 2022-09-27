package in.prokofiev.gmandroidremote.commands;

public class Set1FobCommands implements FobCommands {
    private final String BASE = ""+
            "ATR0\n" +
            "ATAL\n" +
            "STP61\n" +
            "STCSWM2\n" +
            "ATSH100\n" +
            "0000000000000000\n" +
            "ATSH638\n" +
            "0114000023400503\n" +
            "STCSWM3\n" +
            "STP62\n";

    private final String CENTRAL_LOCK_COMMAND_BASE = BASE +
            "ATCP08\n" +
            "ATSH0080B0\n";

    private final String START_ENGINE = "" +
            "ATR0\n" +
            "ATAL\n" +
            "STP61\n" +
            "STCSWM2\n" +
            "ATSH100\n" +
            "0000000000000000\n" +
            "ATSH638\n" +
            "0114000023400503\n" +
            "STCSWM3\n" +
            "STP62\n" +
            "ATCP10\n" +
            "ATSH044097\n" +
            "00FF0A";

    @Override
    public String openDriver() {
        return CENTRAL_LOCK_COMMAND_BASE + "0202\n";
    }

    @Override
    public String open() {
        return CENTRAL_LOCK_COMMAND_BASE + "0203\n";
    }

    @Override
    public String close() {
        return CENTRAL_LOCK_COMMAND_BASE + "0201\n";
    }

    @Override
    public String stop() {
        return CENTRAL_LOCK_COMMAND_BASE + "020C\n";
    }

    @Override
    public String start() {
        return START_ENGINE;
    }
}
