package com.restaurant.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            Properties props = new Properties();
            if (in == null) {
                throw new RuntimeException("db.properties not found on classpath");
            }
            props.load(in);
            url      = props.getProperty("db.url");
            user     = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private Database() { }
}
