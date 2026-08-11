package com.example.carbovirarestapi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carbovirarestapi.auth.dto.AuthResponse;
import com.example.carbovirarestapi.auth.dto.LoginRequest;
import com.example.carbovirarestapi.auth.dto.RegisterRequest;
import com.example.carbovirarestapi.common.exception.DuplicateResourceException;
import com.example.carbovirarestapi.common.exception.InvalidCredentialsException;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyRepository;
import com.example.carbovirarestapi.security.JwtService;
import com.example.carbovirarestapi.user.Role;
import com.example.carbovirarestapi.user.User;
import com.example.carbovirarestapi.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AuthServiceImpl'in bağımlılıkları arayüz olduğu için (UserRepository,
 * CompanyRepository, PasswordEncoder, AuthenticationManager, JwtService)
 * gerçek bir Spring context'e veya veritabanına ihtiyaç duymadan, saf
 * Mockito ile hızlıca test edilebiliyor — SOLID'in pratikteki faydası.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_createsCompanyAndUser_whenEmailAndTaxNumberAreUnique() {
        RegisterRequest request = new RegisterRequest(
                "Acme Geri Dönüşüm", "1234567890", "Metal", "İstanbul", "Örnek Adres",
                "Ayşe Yılmaz", "ayse@acme.com", "sifre1234");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(companyRepository.existsByTaxNumber(request.taxNumber())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(jwtService.generateToken(any())).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.role()).isEqualTo(Role.COMPANY_ADMIN);
        assertThat(response.companyName()).isEqualTo(request.companyName());

        verify(companyRepository).save(any(Company.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest(
                "Acme", "111", "Metal", "İstanbul", "Adres", "Ayşe", "ayse@acme.com", "sifre1234");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(companyRepository, never()).save(any());
    }

    @Test
    void register_throwsDuplicateResourceException_whenTaxNumberAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest(
                "Acme", "111", "Metal", "İstanbul", "Adres", "Ayşe", "ayse@acme.com", "sifre1234");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(companyRepository.existsByTaxNumber(request.taxNumber())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("ayse@acme.com", "sifre1234");
        Company company = Company.builder().name("Acme").taxNumber("111").build();
        User user = User.builder()
                .email(request.email())
                .password("encoded")
                .fullName("Ayşe Yılmaz")
                .role(Role.COMPANY_ADMIN)
                .company(company)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.companyName()).isEqualTo("Acme");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_throwsInvalidCredentialsException_whenAuthenticationFails() {
        LoginRequest request = new LoginRequest("ayse@acme.com", "wrong-password");
        doThrow(new BadCredentialsException("Kimlik doğrulama başarısız"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
