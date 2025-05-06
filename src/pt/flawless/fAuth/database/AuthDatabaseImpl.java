package pt.flawless.fAuth.database;

import org.bukkit.Bukkit;
import pt.flawless.fAuth.Main;

import java.io.File;
import java.sql.SQLException;

public class AuthDatabaseImpl {
    public static AuthDatabase database;

    public static void init() {
        File dataFolder = Main.getMainPlugin().getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            database = new AuthDatabase(dataFolder);
            database.connect();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("SQL Exception - Error creating database!");
        } catch (ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage("Class Not Found- Error creating database!");
        }
    }
}
