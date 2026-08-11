package com.example.carbovirarestapi.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carbovirarestapi.user.User;
import com.example.carbovirarestapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void run_createsAdmin_whenConfiguredAndNotYetExisting() throws Exception {
        AdminBootstrapProperties properties = new AdminBootstrapProperties("admin@carbovira.com", "cok-guclu-sifre");
        when(userRepository.existsByEmail("admin@carbovira.com")).thenReturn(false);
        when(passwordEncoder.encode("cok-guclu-sifre")).thenReturn("encoded");

        new AdminBootstrapRunner(properties, userRepository, passwordEncoder).run(null);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void run_doesNothing_whenAdminAlreadyExists() throws Exception {
        AdminBootstrapProperties properties = new AdminBootstrapProperties("admin@carbovira.com", "cok-guclu-sifre");
        when(userRepository.existsByEmail("admin@carbovira.com")).thenReturn(true);

        new AdminBootstrapRunner(properties, userRepository, passwordEncoder).run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_doesNothing_whenNotConfigured() throws Exception {
        AdminBootstrapProperties properties = new AdminBootstrapProperties("", "");

        new AdminBootstrapRunner(properties, userRepository, passwordEncoder).run(null);

        verify(userRepository, never()).save(any());
        verify(userRepository, never()).existsByEmail(any());
    }
}
