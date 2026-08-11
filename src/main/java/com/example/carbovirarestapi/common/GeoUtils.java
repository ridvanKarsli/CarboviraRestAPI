package com.example.carbovirarestapi.common;

/**
 * İki koordinat arasındaki kuş uçuşu mesafeyi Haversine formülüyle hesaplar. Gerçek yol/nakliye
 * mesafesi değil ama "50 km içinde mi" gibi kaba bir filtreleme için yeterli — asıl amaç zaten
 * atık taşımacılığının belli bir mesafeden sonra ekonomik olmaktan çıktığını gözeten bir arama.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
    }

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
