# Carbovira — Endüstriyel Simbiyoz Platformu

![CI](https://github.com/ridvanKarsli/CarboviraRestAPI/actions/workflows/ci.yml/badge.svg)

Firmaların atıklarını başka bir firmanın hammaddesi olarak ilan edebildiği, arayabildiği ve
platform üzerinden iletişime geçebildiği bir REST API.

## Notlar

Paketler katmana değil özelliğe göre bölünmüş (`auth`, `company`, `listing`, `messaging`, `admin`...).
Her modül kendi controller/service/repository/dto'sunu içinde barındırıyor, tek bir dev katman
(controller katmanı, service katmanı vs.) yerine. `common` ve `security` ortak altyapı için.

Controller'lar somut servis sınıfına değil arayüze bağımlı (`AuthService`, `CompanyService` vs.) —
implementasyonu test'te kolayca mock'layabiliyoruz, bkz. `AuthServiceImplTest`. `User` entity'si
Spring Security'nin `UserDetails`'ini uygulamıyor, o adaptasyon `security.UserPrincipal`'da; entity
framework detaylarına bulaşmasın diye.

Şema Hibernate `ddl-auto=update` ile değil Flyway ile yönetiliyor (`ddl-auto=validate`, uyuşmazlık
varsa build'i patlatır). DTO ↔ entity dönüşümü MapStruct ile, elle mapper yazmıyoruz.

İlan aramasında filtreler (tip/kategori/şehir/kelime) `ListingSpecifications` içinde Criteria API ile
tek sorguda birleşiyor. Kategori/şehir karşılaştırmasında Java'da `toLowerCase()` çağırmıyoruz, ikisini
de SQL `LOWER()`'a bırakıyoruz — "İstanbul" gibi değerlerde Java'nın Türkçe olmayan locale ile ürettiği
küçük harf sonucu veritabanınınkiyle eşleşmiyordu, bir tur debug edip öyle düzelttim.

Mesajlaşmada aynı firma aynı ilana ikinci bir görüşme açamıyor — hem serviste hem de veritabanında
(`UNIQUE (listing_id, initiator_company_id)`) engelleniyor. Görüşme yanıtındaki "karşı taraf" alanı
kim sorduysa ona göre değişiyor (initiator'a ilan sahibi gösteriliyor, ilan sahibine initiator).

## Klasör Yapısı

```
com.example.carbovirarestapi
├── common     # BaseEntity, ApiError, custom exception'lar, GlobalExceptionHandler
├── config     # JPA auditing, OpenAPI/Swagger yapılandırması
├── security   # JWT üretimi/doğrulaması, SecurityConfig, UserPrincipal
├── user       # User entity, Role enum, UserRepository
├── company    # Company entity, DTO, Mapper, Service, Controller
├── auth       # Kayıt/giriş DTO'ları, AuthService, AuthController
├── listing    # Listing entity, Specification, DTO, Mapper, Service, Controller
├── messaging  # Conversation/Message entity, DTO, Mapper, Service, Controller
└── admin      # Firma onay akışı (PLATFORM_ADMIN), ilk admin bootstrap'i
```

## Yerel Geliştirme

1. Postgres'i ayağa kaldır:
   ```bash
   docker compose up -d
   ```
2. Çalıştır:
   ```bash
   ./gradlew bootRun
   ```
3. Swagger: http://localhost:8080/swagger-ui.html

### Ortam Değişkenleri

| Değişken | Varsayılan | Açıklama |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/carbovira` | Veritabanı bağlantısı |
| `DB_USERNAME` / `DB_PASSWORD` | `carbovira` / `carbovira` | docker-compose.yml ile aynı |
| `JWT_SECRET` | (dev varsayılanı) | production'da mutlaka değiştir, en az 256 bit |
| `JWT_EXPIRATION_MS` | `86400000` (24 saat) | token geçerlilik süresi |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | (boş) | ikisi de doluysa ve bu e-postayla kullanıcı yoksa açılışta ilk PLATFORM_ADMIN otomatik oluşur |

### Testler

```bash
./gradlew test
```

Postgres/Docker gerekmiyor, `test` profili bellek içi H2 kullanıyor
(`src/test/resources/application-test.properties`). Aynı build main'e her push/PR'da
GitHub Actions'ta da çalışıyor (`.github/workflows/ci.yml`).

## API

### Auth & Company

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/auth/register` | Firma + ilk kullanıcı (COMPANY_ADMIN) kaydı | herkese açık |
| POST | `/api/auth/login` | JWT token üretimi | herkese açık |
| GET | `/api/companies/me` | Giriş yapan kullanıcının firma profili | JWT gerekli |
| PUT | `/api/companies/me` | Firma profilini güncelle | JWT gerekli |
| GET | `/api/companies/{id}` | Herkese açık firma profili | JWT gerekli |

### Listing

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/listings` | Yeni atık/hammadde ilanı (ACTIVE başlar) | JWT gerekli |
| GET | `/api/listings` | ACTIVE ilanlarda ara (`type`, `category`, `city`, `q`, sayfalama) | JWT gerekli |
| GET | `/api/listings/mine` | Kendi firmamın tüm ilanları (durum fark etmez) | JWT gerekli |
| GET | `/api/listings/{id}` | İlan detayı | JWT gerekli |
| PUT | `/api/listings/{id}` | İlanı güncelle (sadece sahibi) | JWT gerekli |
| PATCH | `/api/listings/{id}/status` | Durum değiştir: ACTIVE/PASSIVE/ARCHIVED (sadece sahibi) | JWT gerekli |
| DELETE | `/api/listings/{id}` | İlanı sil (sadece sahibi) | JWT gerekli |

### Messaging

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/conversations` | İlan hakkında görüşme başlat/devam ettir | JWT gerekli |
| GET | `/api/conversations` | Taraf olduğum tüm görüşmeler | JWT gerekli |
| GET | `/api/conversations/{id}/messages` | Görüşmedeki mesajlar (sadece taraflar) | JWT gerekli |
| POST | `/api/conversations/{id}/messages` | Görüşmeye mesaj gönder (sadece taraflar) | JWT gerekli |

Kendi ilanına mesaj gönderemezsin (`400`). Aynı firma aynı ilana ikinci görüşme açamaz, tekrar mesaj
atınca mevcut görüşmeye eklenir. Taraf olmadığın bir görüşmeye erişim `403` döner.

### Admin

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| GET | `/api/admin/companies` | Firmaları listele (`verified` filtresiyle, ops.) | PLATFORM_ADMIN |
| PATCH | `/api/admin/companies/{id}/verify` | Firmayı onayla | PLATFORM_ADMIN |

Onay şu an sadece bilgilendirici bir rozet — onaylanmamış firmalar da ilan verip mesajlaşabiliyor,
`verified=false` sadece `CompanyResponse`'ta görünüyor. İleride zorunlu yapmak istersek
`ListingService.create()`'e tek bir kontrol eklemek yeterli.

İlk platform yöneticisi register'dan oluşmuyor (register her zaman COMPANY_ADMIN üretiyor).
`ADMIN_EMAIL`/`ADMIN_PASSWORD` tanımlıysa açılışta otomatik oluşuyor (`admin.AdminBootstrapRunner`).

---

Tüm istekler (auth uçları hariç) `Authorization: Bearer <token>` başlığı bekler. Başka bir firmanın
ilanında/görüşmesinde güncelleme/silme denemesi `403 Forbidden` döner.

## Sırada ne var

Bildirimler, ilan/firma için görsel yükleme, ilan onayının zorunlu hale getirilmesi gibi fikirler
`Carbovira_Proje_Raporu.docx` içinde duruyor — henüz yapılmadı.
