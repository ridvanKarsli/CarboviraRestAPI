package com.example.carbovirarestapi.security;

import com.example.carbovirarestapi.user.Role;
import com.example.carbovirarestapi.user.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security'nin ihtiyaç duyduğu UserDetails sözleşmesi ile domain
 * modelimiz (User) arasındaki uyarlayıcı (adapter). User entity'sinin
 * framework'e bağımlı olmasını engeller (SRP + bağımlılıkların tersine çevrilmesi).
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Role role;
    private final Long companyId;
    private final boolean enabled;

    private UserPrincipal(Long id, String email, String password, Role role, Long companyId, boolean enabled) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.companyId = companyId;
        this.enabled = enabled;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getCompany().getId(),
                user.isEnabled()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
