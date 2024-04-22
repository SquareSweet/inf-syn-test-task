import controller.RoutingController;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Server {
    private final int port;
    private final RoutingController controller;

    public Server(int port, RoutingController controller) {
        this.port = port;
        this.controller = controller;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port, 50)) {

            log.info("Server started on port {}", port);
            ExecutorService executorService = Executors.newCachedThreadPool();

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                if (clientSocket != null) {
                    log.debug("Client connected");
                    executorService.submit(() -> controller.handleRequest(clientSocket));
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
