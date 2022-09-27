package in.prokofiev.gmandroidremote.commands;

public class Command {
    private final String command;
    private final int delay;

    public Command(String command, int delay) {
        if(command==null)
            throw new RuntimeException("command may not by null");
        this.command = command;
        this.delay = delay;
    }

    public String getCommand() {
        return command;
    }

    public int getDelay() {
        return delay;
    }
}
