package com.example.carbovirarestapi.listing;

/** Bir ilanın atık mı yoksa aranan hammadde mi olduğunu belirtir. */
public enum ListingType {

    /** Firmanın elden çıkarmak istediği atık/yan ürün. */
    WASTE,

    /** Firmanın üretim için ihtiyaç duyduğu hammadde talebi. */
    RAW_MATERIAL
}
