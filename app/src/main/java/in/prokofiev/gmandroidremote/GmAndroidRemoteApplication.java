package in.prokofiev.gmandroidremote;

import android.app.Application;
import android.content.Context;

import in.prokofiev.gmandroidremote.commands.CommandExecutor;

public class GmAndroidRemoteApplication extends Application {

    private static CommandExecutor commandExecutor;

    public CommandExecutor getCommandExecutor(){
        if(commandExecutor == null)
            commandExecutor = new CommandExecutor(getApplicationContext());
        return commandExecutor;
    }

    public static CommandExecutor getCommandExecutor(Context context){
        if(commandExecutor == null)
            commandExecutor = new CommandExecutor(context);
        return commandExecutor;
    }
}
