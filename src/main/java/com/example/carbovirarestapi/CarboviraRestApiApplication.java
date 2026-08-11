package com.example.carbovirarestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarboviraRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarboviraRestApiApplication.class, args);
    }

}

// Paketler katmana göre değil özelliğe göre bölünmüş (auth, company, listing,
// messaging, admin...) — her biri kendi controller/service/repository/dto'sunu
// içinde barındırıyor. Detay için README'deki klasör yapısına bak.
