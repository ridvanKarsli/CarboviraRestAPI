# Carbovira Frontend

Backend'deki REST API'yi tüketen bir React (Vite) uygulaması: kayıt/giriş, ilan arama,
ilan verme, mesajlaşma, firma profili ve etki/karbon raporu.

## Çalıştırma

Backend'in ayrı bir terminalde `8080` portunda çalışıyor olması gerekiyor
(`../gradlew bootRun`).

```bash
npm install
npm run dev
```

Varsayılan olarak `http://localhost:5173`'te açılır ve `http://localhost:8080`'e istek
yapar. Farklı bir backend adresi kullanmak istersen `.env.example`'ı `.env.local` olarak
kopyalayıp `VITE_API_BASE_URL`'i değiştir.

Backend tarafında `app.cors.allowed-origins` (varsayılan `http://localhost:5173`) bu
adresi zaten izin veriyor; farklı bir portta çalıştırırsan backend'deki
`application.properties`'i de güncellemen gerekir.

## Build

```bash
npm run build
```

`dist/` altında statik dosyalar üretilir. Backend'i tek jar'dan servis etmek istersen bu
klasörün içeriğini `src/main/resources/static`'e kopyalayabilirsin — Spring Boot statik
kaynakları otomatik olarak kök yoldan servis eder.
