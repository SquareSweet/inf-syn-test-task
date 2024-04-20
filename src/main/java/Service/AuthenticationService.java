package Service;

import dto.AuthenticationRequest;
import dto.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse signUp(AuthenticationRequest authRequest);
    AuthenticationResponse signIn(AuthenticationRequest authRequest);
    AuthenticationResponse createAccessToken(String refreshToken);
    AuthenticationResponse createRefreshToken(String refreshToken);
    void invalidateToken(String refreshToken);
}
