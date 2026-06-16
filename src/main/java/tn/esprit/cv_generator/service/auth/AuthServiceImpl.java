package tn.esprit.cv_generator.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.cv_generator.dto.AuthResponse;
import tn.esprit.cv_generator.dto.LoginRequest;
import tn.esprit.cv_generator.dto.RegisterRequest;
import tn.esprit.cv_generator.entity.User;
import tn.esprit.cv_generator.exception.AuthException;
import tn.esprit.cv_generator.exception.DuplicateEmailException;
import tn.esprit.cv_generator.repository.UserRepository;
import tn.esprit.cv_generator.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email already in use: " + request.email());
        }
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .build();
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user.getEmail()));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new AuthException("Invalid email or password");
        }
        return new AuthResponse(jwtService.generateToken(request.email()));
    }
}
