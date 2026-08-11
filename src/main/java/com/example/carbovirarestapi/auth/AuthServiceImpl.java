package com.example.carbovirarestapi.auth;

import com.example.carbovirarestapi.auth.dto.AuthResponse;
import com.example.carbovirarestapi.auth.dto.LoginRequest;
import com.example.carbovirarestapi.auth.dto.RegisterRequest;
import com.example.carbovirarestapi.common.exception.DuplicateResourceException;
import com.example.carbovirarestapi.common.exception.InvalidCredentialsException;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyRepository;
import com.example.carbovirarestapi.security.JwtService;
import com.example.carbovirarestapi.security.UserPrincipal;
import com.example.carbovirarestapi.user.Role;
import com.example.carbovirarestapi.user.User;
import com.example.carbovirarestapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Bu e-posta adresi zaten kayıtlı: " + request.email());
        }
        if (companyRepository.existsByTaxNumber(request.taxNumber())) {
            throw new DuplicateResourceException("Bu vergi numarası zaten kayıtlı: " + request.taxNumber());
        }

        Company company = Company.builder()
                .name(request.companyName())
                .taxNumber(request.taxNumber())
                .sector(request.sector())
                .city(request.city())
                .address(request.address())
                .build();
        companyRepository.save(company);

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(Role.COMPANY_ADMIN)
                .company(company)
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(UserPrincipal.from(user));
        return toAuthResponse(token, user, company);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("E-posta veya şifre hatalı.");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("E-posta veya şifre hatalı."));

        String token = jwtService.generateToken(UserPrincipal.from(user));
        return toAuthResponse(token, user, user.getCompany());
    }

    private AuthResponse toAuthResponse(String token, User user, Company company) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                company.getId(),
                company.getName()
        );
    }
}
