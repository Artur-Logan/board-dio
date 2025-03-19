package com.arturlogan.board_dio.persistence.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionConfig {
    public static Connection getConnection() throws SQLException {

        var conncetion = DriverManager.getConnection("jdbc:postgresql://localhost:15432/mydb", "postgres", "admin");
        conncetion.setAutoCommit(false);
        return conncetion;
    }
}
