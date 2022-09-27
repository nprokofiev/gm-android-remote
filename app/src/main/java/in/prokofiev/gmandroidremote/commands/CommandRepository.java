package in.prokofiev.gmandroidremote.commands;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import androidx.annotation.Nullable;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class CommandRepository extends SQLiteAssetHelper {
    public static final String DB_NAME = "commands.db";
    public static final int DB_VERSION = 1;

    public CommandRepository(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public Cursor getCommands() {

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"name", "command"};
        String sqlTables = "commands";

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, null, null,
                null, null, null);

        return c;

    }

    public String getCommandByName(String commandName){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"command"};
        String sqlTables = "commands";

        qb.setTables(sqlTables);
        qb.appendWhere("name =");
        qb.appendWhereEscapeString(commandName);

        Cursor c = qb.query(db, sqlSelect, null, null,
                null, null, null);
        if(!c.moveToNext())
            return null;
        return c.getString(0);
    }
}
