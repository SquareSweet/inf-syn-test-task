package data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class ConnectionPool {
    private final HikariDataSource dataSource;

    public ConnectionPool() {
        final Properties dbProperties = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/db.properties")) {
            dbProperties.load(in);
        } catch (IOException e) {
            log.error("Error occurred while reading database properties file: " + e.getMessage());
        }

        final HikariConfig config = new HikariConfig();
        config.setDriverClassName(dbProperties.getProperty("datasource.driver"));
        config.setJdbcUrl(dbProperties.getProperty("datasource.url"));
        config.setUsername(dbProperties.getProperty("datasource.username"));
        config.setPassword(dbProperties.getProperty("datasource.password"));
        config.setMaximumPoolSize(50);
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("Error occurred while issuing connection from pool: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
