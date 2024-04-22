package controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import rawhttp.core.EagerHttpRequest;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpResponse;
import utils.HttpUtils;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class RoutingController {
    private final AuthenticationController authenticationController;
    private final AccountController accountController;
    private final HttpUtils httpUtils;

    public RoutingController(
            AuthenticationController authenticationController,
            AccountController accountController
    ) {
        this.authenticationController = authenticationController;
        this.accountController = accountController;
        httpUtils = new HttpUtils();
    }

    public void handleRequest(Socket socket) {
        try {
            if (socket.getInputStream().available() != 0) {
                RawHttp http = new RawHttp();
                EagerHttpRequest request = http.parseRequest(socket.getInputStream()).eagerly();

                RawHttpResponse response = switch (request.getUri().getPath()) {
                    case "/signin" -> {
                        if ("POST".equals(request.getMethod())) {
                            yield authenticationController.handleSingIn(request);
                        } else {
                            yield methodNotAllowed();
                        }
                    }
                    case "/signup" -> {
                        if ("POST".equals(request.getMethod())) {
                            yield authenticationController.handleSingUp(request);
                        } else {
                            yield methodNotAllowed();
                        }
                    }
                    case "/token" -> {
                        if ("POST".equals(request.getMethod())) {
                            yield authenticationController.handleToken(request);
                        } else {
                            yield methodNotAllowed();
                        }
                    }
                    case "/refresh" -> {
                        if ("POST".equals(request.getMethod())) {
                            yield authenticationController.handleRefresh(request);
                        } else {
                            yield methodNotAllowed();
                        }
                    }
                    case "/money" -> switch (request.getMethod()) {
                        case "GET" -> accountController.handleGetMoney(request);
                        case "POST" -> accountController.handleSendMoney(request);
                        default -> methodNotAllowed();
                    };
                    default -> urlNotFound();
                };

                response.writeTo(socket.getOutputStream());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private RawHttpResponse methodNotAllowed() {
        return httpUtils.buildResponse(
                HttpStatus.SC_METHOD_NOT_ALLOWED,
                "{ \"message\":\"Method not allowed\"}"
        );
    }

    private RawHttpResponse urlNotFound() {
        return httpUtils.buildResponse(
                HttpStatus.SC_NOT_FOUND,
                "{ \"message\":\"URL not found\"}"
        );
    }
}