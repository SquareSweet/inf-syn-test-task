package Service;

import dao.UserDao;
import dto.AuthenticationRequest;
import dto.AuthenticationResponse;
import dto.RegistrationRequest;
import dto.RegistrationResponse;
import exception.AuthenticationException;
import exception.InvalidTokenException;
import exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.JwtTokenUtils;

@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserDao userDao;
    private final JwtTokenUtils tokenUtils;

    public AuthenticationServiceImpl(UserDao userDao) {
        this.userDao = userDao;
        tokenUtils = new JwtTokenUtils();
    }

    @Override
    public RegistrationResponse signUp(RegistrationRequest regRequest) {
        if (regRequest.getLogin() == null || regRequest.getLogin().isEmpty() ||
                regRequest.getPassword() == null || regRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Login and password should not be empty");
        }
        String hashedPassword = BCrypt.hashpw(regRequest.getPassword(), BCrypt.gensalt());
        User user = userDao.create(new User(null, regRequest.getLogin(), hashedPassword));
        log.debug("Created user id: {}, username: {}", user.getId(), user.getUsername());
        String accessToken = tokenUtils.generateAccessToken(user);
        String refreshToken = tokenUtils.generateRefreshToken(user);
        return new RegistrationResponse(accessToken, refreshToken);
    }

    @Override
    public AuthenticationResponse signIn(AuthenticationRequest authRequest) {
        if (authRequest.getLogin() == null || authRequest.getLogin().equals(" ") ||
                authRequest.getPassword() == null || authRequest.getPassword().isEmpty()){
            throw new IllegalArgumentException("Login and password should not be empty");
        }
        User user = userDao.getByUsername(authRequest.getLogin()).orElseThrow(
                () -> new UserNotFoundException("Username " + " not found")
        );
        if (!BCrypt.checkpw(authRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Wrong password");
        }
        log.debug("User id: {}, username: {} has logged in", user.getId(), user.getUsername());
        String accessToken = tokenUtils.generateAccessToken(user);
        String refreshToken = tokenUtils.generateRefreshToken(user);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @Override
    public AuthenticationResponse createAccessToken(String refreshToken) {
        if (tokenUtils.validateRefreshToken(refreshToken)) {
            Claims claims = tokenUtils.getRefreshClaims(refreshToken);
            User user = userDao.getByUsername(claims.getSubject()).orElseThrow();
            String accessToken = tokenUtils.generateAccessToken(user);
            return new AuthenticationResponse(accessToken, null);
        }
        throw new InvalidTokenException("Invalid token");
    }

    @Override
    public AuthenticationResponse createRefreshToken(String refreshToken) {
        if (tokenUtils.validateRefreshToken(refreshToken)) {
            final Claims claims = tokenUtils.getRefreshClaims(refreshToken);
            User user = userDao.getByUsername(claims.getSubject()).orElseThrow();
            String accessToken = tokenUtils.generateAccessToken(user);
            String newRefreshToken = tokenUtils.generateRefreshToken(user);
            return new AuthenticationResponse(accessToken, newRefreshToken);
        }
        throw new InvalidTokenException("Invalid token");
    }
}
