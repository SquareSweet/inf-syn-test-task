package controller;

import Service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.BalanceResponse;
import dto.TransferRequest;
import dto.TransferResponse;
import exception.AuthenticationException;
import exception.InvalidTokenException;
import exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.BodyReader;
import utils.HttpUtils;
import utils.JwtTokenUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class AccountController {
    private final AccountService accountService;
    private final ObjectMapper mapper;
    private final HttpUtils httpUtils;
    private final JwtTokenUtils tokenUtils;
    private final Logger fileLogger;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
        mapper = new ObjectMapper();
        httpUtils = new HttpUtils();
        tokenUtils = new JwtTokenUtils();
        fileLogger = LoggerFactory.getLogger("actions");
    }

    public RawHttpResponse handleGetMoney(RawHttpRequest request) {
        try {
            String username = getUsername(request);

            BalanceResponse balanceResponse = accountService.checkBalance(username);
            fileLogger.info("User {} requested balance: ${}", username, balanceResponse.getBalance());
            return httpUtils.buildResponse(
                    HttpStatus.SC_OK,
                    mapper.writeValueAsString(balanceResponse)
            );
        } catch (AuthenticationException | InvalidTokenException e) {
            log.debug(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_UNAUTHORIZED, e);
        } catch (UserNotFoundException | IllegalArgumentException e) {
            log.debug(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_BAD_REQUEST, e);
        } catch (IOException e) {
            log.error(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_UNPROCESSABLE_ENTITY, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_SERVER_ERROR, e);
        }
    }

    public RawHttpResponse handleSendMoney(RawHttpRequest request) {
        try {
            String username = getUsername(request);

            BodyReader bodyReader = request.getBody().orElseThrow(
                    () -> new IllegalArgumentException("Request body is absent")
            );
            TransferRequest transferRequest = mapper.readValue(
                    bodyReader.asRawString(StandardCharsets.UTF_8),
                    TransferRequest.class
            );

            TransferResponse registrationResponse = accountService.transferToUsername(username, transferRequest);
            fileLogger.info("User {} has sent ${} to user {}", username, transferRequest.getAmount(), transferRequest.getReceiverUsername());
            return httpUtils.buildResponse(
                    HttpStatus.SC_OK,
                    mapper.writeValueAsString(registrationResponse)
            );
        } catch (AuthenticationException | InvalidTokenException e) {
            log.debug(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_UNAUTHORIZED, e);
        } catch (UserNotFoundException | IllegalArgumentException e) {
            log.debug(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_BAD_REQUEST, e);
        } catch (IOException e) {
            log.error(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_UNPROCESSABLE_ENTITY, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_SERVER_ERROR, e);
        }
    }

    private String getUsername(RawHttpRequest request) {
        List<String> headersAuth = request.getHeaders().get("Authorization");
        if (headersAuth.isEmpty())
            throw new AuthenticationException("Authorization header not found");

        final String headerAuth = headersAuth.get(0);
        if (headerAuth.isBlank() || !headerAuth.startsWith("Bearer ")) {
            throw new AuthenticationException("Token not found");
        }

        if (!tokenUtils.validateAccessToken(headerAuth.substring(7))) {
            throw new InvalidTokenException("Invalid token");
        }

        return tokenUtils.getUsername(headerAuth.substring(7));
    }
}
