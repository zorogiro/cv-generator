package tn.esprit.cv_generator.service.auth;

import tn.esprit.cv_generator.dto.AuthResponse;
import tn.esprit.cv_generator.dto.LoginRequest;
import tn.esprit.cv_generator.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
