package com.example.carbovirarestapi.listing;

/** Bir ilanın yaşam döngüsü durumu. */
public enum ListingStatus {

    /** Aramada görünür, teklif alabilir. */
    ACTIVE,

    /** Sahibi tarafından geçici olarak gizlenmiş; aramada görünmez. */
    PASSIVE,

    /** Bir görüşme sonucunda eşleşmiş; aramada görünmez. */
    MATCHED,

    /** Kalıcı olarak kapatılmış. */
    ARCHIVED
}
