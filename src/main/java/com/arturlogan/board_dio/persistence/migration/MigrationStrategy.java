package com.arturlogan.board_dio.persistence.migration;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

import static com.arturlogan.board_dio.persistence.config.ConnectionConfig.getConnection;

public class MigrationStrategy {

    private final Connection connection;

    public MigrationStrategy(Connection connection) {
        this.connection = connection;
    }

    public void executeMigrations() {

        var originalOut = System.out;
        var originalErr = System.out;

        try (var fos = new FileOutputStream("liquibase.log")) {
            System.setErr(new PrintStream(fos));
            System.setOut(new PrintStream(fos));

            try (var connection = getConnection(); var jdbcConnection = new JdbcConnection(connection);) {

                var liquibase = new Liquibase("src/main/resources/db/changelog/db.changelog-master.yml",
                        new ClassLoaderResourceAccessor(),
                        jdbcConnection);
                liquibase.update();
            } catch (SQLException | LiquibaseException e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.out.println(originalOut);
            System.out.println(originalErr);
        }
    }
}
