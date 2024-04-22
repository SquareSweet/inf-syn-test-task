import Service.AccountService;
import Service.AccountServiceImpl;
import Service.AuthenticationService;
import Service.AuthenticationServiceImpl;
import controller.AccountController;
import controller.AuthenticationController;
import controller.RoutingController;
import dao.AccountDao;
import dao.AccountDaoImpl;
import dao.UserDao;
import dao.UserDaoImpl;
import data.ConnectionPool;
import data.LiquibaseMigration;

public class InfiniteSynergyTestTask {
    public static void main(String[] args) {
        LiquibaseMigration migration = new LiquibaseMigration();
        migration.runMigration();

        ConnectionPool connectionPool = new ConnectionPool();

        UserDao userDao = new UserDaoImpl(connectionPool);
        AuthenticationService authenticationService = new AuthenticationServiceImpl(userDao);
        AuthenticationController authenticationController = new AuthenticationController(authenticationService);

        AccountDao accountDao = new AccountDaoImpl(connectionPool);
        AccountService accountService = new AccountServiceImpl(accountDao);
        AccountController accountController = new AccountController(accountService);

        RoutingController controller = new RoutingController(authenticationController, accountController);
        Server server = new Server(8080, controller);
        server.start();
    }
}