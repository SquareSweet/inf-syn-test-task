package data;

import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.exception.CommandExecutionException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class LiquibaseMigration {
    private final Properties dbProperties;

    public LiquibaseMigration() {
        dbProperties = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/db.properties")) {
            dbProperties.load(in);
        } catch (IOException e) {
            log.error("Error occurred while reading database properties file: " + e.getMessage());
        }
    }

    public void runMigration() {
        if (!Boolean.parseBoolean(dbProperties.getProperty("liquibase.migration"))) {
            log.info("Migration skipped due to configuration");
            return;
        }
        try {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME);

            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_ARG, dbProperties.getProperty("datasource.driver"));
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, dbProperties.getProperty("datasource.url"));
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, dbProperties.getProperty("datasource.username"));
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, dbProperties.getProperty("datasource.password"));
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, dbProperties.getProperty("liquibase.changelog"));

            commandScope.execute();
        } catch (CommandExecutionException e) {
            log.error("Error occurred while running database migration: " + e.getMessage());
        }
    }
}
