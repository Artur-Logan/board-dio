package com.arturlogan.board_dio;

import com.arturlogan.board_dio.persistence.migration.MigrationStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

import static com.arturlogan.board_dio.persistence.config.ConnectionConfig.getConnection;

@SpringBootApplication
public class BoardDioApplication {

	public static void main(String[] args) throws SQLException {
		SpringApplication.run(BoardDioApplication.class, args);

		try (var connection = getConnection()){
			new MigrationStrategy(connection).executeMigrations();
		}
	}

}
