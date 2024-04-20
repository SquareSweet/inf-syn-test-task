import data.LiquibaseMigration;

public class InfiniteSynergyTestTask {
    public static void main(String[] args) {
        LiquibaseMigration migration = new LiquibaseMigration();
        migration.runMigration();
    }
}