package com.example.carbovirarestapi.user;

import com.example.carbovirarestapi.common.entity.BaseEntity;
import com.example.carbovirarestapi.company.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sisteme giriş yapabilen kullanıcı. COMPANY_ADMIN/EMPLOYEE rolündeki kullanıcılar tam
 * olarak bir firmaya bağlıdır; PLATFORM_ADMIN rolündeki platform yöneticileri herhangi
 * bir firmanın parçası olmadığından company alanı onlar için null'dır.
 * Not: Bu sınıf bilerek Spring Security'nin UserDetails arayüzünü uygulamaz —
 * güvenlik adaptasyonu security.UserPrincipal içinde ayrıştırılmıştır (SRP,
 * domain modelini framework detaylarından bağımsız tutmak için).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /** PLATFORM_ADMIN dışındaki tüm roller için zorunludur; PLATFORM_ADMIN için null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
