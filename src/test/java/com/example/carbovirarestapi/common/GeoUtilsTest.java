package com.example.carbovirarestapi.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class GeoUtilsTest {

    @Test
    void distanceKm_isZero_forSameCoordinates() {
        double distance = GeoUtils.distanceKm(41.0082, 28.9784, 41.0082, 28.9784);

        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void distanceKm_matchesKnownDistance_istanbulToAnkara() {
        // İstanbul (41.0082, 28.9784) - Ankara (39.9334, 32.8597), kuş uçuşu ~350 km civarı.
        double distance = GeoUtils.distanceKm(41.0082, 28.9784, 39.9334, 32.8597);

        assertThat(distance).isCloseTo(350.0, within(20.0));
    }
}
