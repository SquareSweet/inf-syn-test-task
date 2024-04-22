package Service;

import dto.AuthenticationRequest;
import dto.AuthenticationResponse;
import dto.RegistrationRequest;
import dto.RegistrationResponse;

public interface AuthenticationService {
    RegistrationResponse signUp(RegistrationRequest regRequest);
    AuthenticationResponse signIn(AuthenticationRequest authRequest);
    AuthenticationResponse createAccessToken(String refreshToken);
    AuthenticationResponse createRefreshToken(String refreshToken);
}
