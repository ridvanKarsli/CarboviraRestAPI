package com.example.carbovirarestapi.user;

/** Sistemdeki kullanıcı rolleri. */
public enum Role {

    /** Firmayı temsil eden, firma profilini ve ilanlarını yönetebilen ana kullanıcı. */
    COMPANY_ADMIN,

    /** Firma adına ilan/mesaj işlemleri yapabilen çalışan kullanıcı. */
    EMPLOYEE,

    /** Platform genelinde onay ve moderasyon yetkisine sahip yönetici. */
    PLATFORM_ADMIN
}
