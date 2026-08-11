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
 * ADMIN_EMAIL/ADMIN_PASSWORD tanımlıysa ve bu e-postayla kullanıcı yoksa, açılışta ilk
 * PLATFORM_ADMIN'i oluşturur. Register uç noktası her zaman COMPANY_ADMIN ürettiği için
 * platform yöneticisini oluşturmanın başka yolu yok.
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
