package com.example.carbovirarestapi.admin;

import com.example.carbovirarestapi.user.Role;
import com.example.carbovirarestapi.user.User;
import com.example.carbovirarestapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Uygulama her başladığında çalışır; {@code ADMIN_EMAIL}/{@code ADMIN_PASSWORD} ortam
 * değişkenleri tanımlıysa ve bu e-postaya sahip bir kullanıcı yoksa ilk PLATFORM_ADMIN
 * kullanıcısını otomatik oluşturur. İdempotenttir — kullanıcı zaten varsa hiçbir şey yapmaz.
 * <p>
 * Register uç noktası her zaman COMPANY_ADMIN oluşturduğundan, platform yöneticisini
 * oluşturmanın başka bir yolu yoktur; bu yüzden bootstrap ortam değişkeni ile yapılır.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminBootstrapProperties adminBootstrapProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = adminBootstrapProperties.email();
        String password = adminBootstrapProperties.password();

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return;
        }
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName("Platform Yöneticisi")
                .role(Role.PLATFORM_ADMIN)
                .company(null)
                .build();
        userRepository.save(admin);
        log.info("İlk PLATFORM_ADMIN kullanıcısı oluşturuldu: {}", email);
    }
}
