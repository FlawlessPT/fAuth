package pt.flawless.fAuth.database;

import pt.flawless.fAuth.database.bcrypt.BCrypt;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class AuthDatabase {
    private final File dbFile;
    private Connection connection;

    public AuthDatabase(File pluginDataFolder) {
        this.dbFile = new File(pluginDataFolder, "auth.db");
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) return;

        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);

        createTableIfNotExists();
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "uuid TEXT PRIMARY KEY," +
                "username TEXT NOT NULL," +
                "password TEXT NOT NULL);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public boolean isRegistered(UUID uuid) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void registerUser(UUID uuid, String username, String plainPassword) throws SQLException {
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

        String sql = "INSERT INTO users(uuid, username, password) VALUES (?, ?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, username);
            ps.setString(3, hashedPassword);
            ps.executeUpdate();
        }
    }

    public boolean verifyPassword(UUID uuid, String plainPassword) throws SQLException {
        String sql = "SELECT password FROM users WHERE uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashed = rs.getString("password");
                    return BCrypt.checkpw(plainPassword, hashed);
                }
            }
        }
        return false;
    }
}
