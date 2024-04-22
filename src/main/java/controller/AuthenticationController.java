package controller;

import Service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.*;
import exception.AuthenticationException;
import exception.InvalidTokenException;
import exception.UserNotFoundException;
import exception.UsernameAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.BodyReader;
import utils.HttpUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class AuthenticationController {
    private final AuthenticationService authService;
    private final ObjectMapper mapper;
    private final HttpUtils httpUtils;
    private final Logger fileLogger;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
        mapper = new ObjectMapper();
        httpUtils = new HttpUtils();
        fileLogger = LoggerFactory.getLogger("actions");
    }

    public RawHttpResponse handleSingUp(RawHttpRequest request) {
        try {
            BodyReader bodyReader = request.getBody().orElseThrow(
                    () -> new IllegalArgumentException("Request body is absent")
            );
            RegistrationRequest registrationRequest = mapper.readValue(
                    bodyReader.asRawString(StandardCharsets.UTF_8),
                    RegistrationRequest.class
            );

            RegistrationResponse registrationResponse = authService.signUp(registrationRequest);
            fileLogger.info("User {} has signed up", registrationRequest.getLogin());
            return httpUtils.buildResponse(
                    HttpStatus.SC_OK,
                    mapper.writeValueAsString(registrationResponse)
            );
        } catch (UsernameAlreadyExistsException | IllegalArgumentException e) {
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

    public RawHttpResponse handleSingIn(RawHttpRequest request) {
        try {
            BodyReader bodyReader = request.getBody().orElseThrow(
                    () -> new IllegalArgumentException("Request body is absent")
            );
            AuthenticationRequest authenticationRequest = mapper.readValue(
                    bodyReader.asRawString(StandardCharsets.UTF_8),
                    AuthenticationRequest.class
            );

            AuthenticationResponse authenticationResponse = authService.signIn(authenticationRequest);
            fileLogger.info("User {} has signed in", authenticationRequest.getLogin());
            return httpUtils.buildResponse(
                    HttpStatus.SC_OK,
                    mapper.writeValueAsString(authenticationResponse)
            );
        } catch (AuthenticationException | UserNotFoundException e) {
            log.debug(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_UNAUTHORIZED, e);
        } catch (IllegalArgumentException e) {
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

    public RawHttpResponse handleToken(RawHttpRequest request) {
        try {
            BodyReader bodyReader = request.getBody().orElseThrow(
                    () -> new IllegalArgumentException("Request body is absent")
            );
            RefreshTokenRequest refreshTokenRequest = mapper.readValue(
                    bodyReader.asRawString(StandardCharsets.UTF_8),
                    RefreshTokenRequest.class
            );

            AuthenticationResponse authenticationResponse = authService.createAccessToken(refreshTokenRequest.getRefreshToken());
            return httpUtils.buildResponse(
                    HttpStatus.SC_OK,
                    mapper.writeValueAsString(authenticationResponse)
            );
        } catch (InvalidTokenException e) {
            log.debug(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_UNAUTHORIZED, e);
        } catch (IllegalArgumentException e) {
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

    public RawHttpResponse handleRefresh(RawHttpRequest request) {
        try {
            BodyReader bodyReader = request.getBody().orElseThrow(
                    () -> new IllegalArgumentException("Request body is absent")
            );
            RefreshTokenRequest refreshTokenRequest = mapper.readValue(
                    bodyReader.asRawString(StandardCharsets.UTF_8),
                    RefreshTokenRequest.class
            );

            AuthenticationResponse authenticationResponse = authService.createRefreshToken(refreshTokenRequest.getRefreshToken());
            return httpUtils.buildResponse(
                    HttpStatus.SC_OK,
                    mapper.writeValueAsString(authenticationResponse)
            );
        } catch (InvalidTokenException e) {
            log.debug(e.getMessage());
            return httpUtils.buildExceptionResponse(HttpStatus.SC_UNAUTHORIZED, e);
        } catch (IllegalArgumentException e) {
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
}
