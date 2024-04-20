package Service;

import dao.UserDao;
import dto.AuthenticationRequest;
import dto.AuthenticationResponse;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JwtTokenUtils;

@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserDao userDao;
    private final JwtTokenUtils tokenUtils;
    private final Logger fileLog;

    public AuthenticationServiceImpl(UserDao userDao) {
        this.userDao = userDao;
        tokenUtils = new JwtTokenUtils();
        fileLog = LoggerFactory.getLogger("actions");
    }

    @Override
    public AuthenticationResponse signUp(AuthenticationRequest authRequest) {
        if (authRequest.getLogin() == null || authRequest.getLogin().isEmpty() ||
                authRequest.getPassword() == null || authRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Login and password should not be empty");
        }
        String hashedPassword = BCrypt.hashpw(authRequest.getPassword(), BCrypt.gensalt());
        User user = userDao.create(new User(null, authRequest.getLogin(), hashedPassword));
        fileLog.info("Created user id: {}, username: {}", user.getId(), user.getUsername());
        String accessToken = tokenUtils.generateAccessToken(user);
        String refreshToken = tokenUtils.generateRefreshToken(user);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @Override
    public AuthenticationResponse signIn(AuthenticationRequest authRequest) {
        if (authRequest.getLogin() == null || authRequest.getLogin().equals(" ") ||
                authRequest.getPassword() == null || authRequest.getPassword().isEmpty()){
            throw new IllegalArgumentException("Login and password should not be empty");
        }
        User user = userDao.getByUsername(authRequest.getLogin()).orElseThrow();
        BCrypt.checkpw(authRequest.getPassword(), user.getPassword());
        fileLog.info("User id: {}, username: {} has logged in", user.getId(), user.getUsername());
        String accessToken = tokenUtils.generateAccessToken(user);
        String refreshToken = tokenUtils.generateRefreshToken(user);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @Override
    public AuthenticationResponse createAccessToken(String refreshToken) {
        //TODO: проверка инвалидации
        if (tokenUtils.validateRefreshToken(refreshToken)) {
            Claims claims = tokenUtils.getRefreshClaims(refreshToken);
            User user = userDao.getByUsername(claims.getSubject()).orElseThrow();
            String accessToken = tokenUtils.generateAccessToken(user);
            return new AuthenticationResponse(accessToken, null);
        }
        return new AuthenticationResponse(null, null);
    }

    @Override
    public AuthenticationResponse createRefreshToken(String refreshToken) {
        //TODO: проверка инвалидации
        if (tokenUtils.validateRefreshToken(refreshToken)) {
            final Claims claims = tokenUtils.getRefreshClaims(refreshToken);
            User user = userDao.getByUsername(claims.getSubject()).orElseThrow();
            String accessToken = tokenUtils.generateAccessToken(user);
            String newRefreshToken = tokenUtils.generateRefreshToken(user);
            return new AuthenticationResponse(accessToken, newRefreshToken);
        }
        throw new RuntimeException("Invalid token");
    }

    @Override
    public void invalidateToken(String refreshToken) {
        //TODO: инваоидация
    }
}