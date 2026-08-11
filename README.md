# Carbovira — Endüstriyel Simbiyoz Platformu

Firmaların atıklarını başka bir firmanın hammaddesi olarak ilan edebildiği, arayabildiği ve
platform üzerinden iletişime geçebildiği bir REST API.

## Mimari İlkeler

- **Feature-based (özellik bazlı) paket yapısı**: `auth`, `company`, `listing`, `messaging`, `user`, `security`,
  `common`, `config`. Her modül kendi controller / service (arayüz + impl) / repository / dto katmanını bir
  arada barındırır.
- **Dinamik arama filtreleri Specification (Criteria API) ile**: `listing.ListingSpecifications`, tip/kategori/
  şehir/anahtar kelime gibi hepsi opsiyonel filtreleri tek sorguda birleştirir; her kombinasyon için ayrı
  repository metodu yazmaya gerek kalmaz. Gerçek bir veritabanına karşı `ListingRepositorySpecificationTest`
  ile doğrulanır. Büyük/küçük harf duyarsız karşılaştırmalarda Türkçe "İ/I" karakterlerinin Java ile SQL
  arasında farklı sonuç üretmesine ("Türkçe I problemi") karşı, karşılaştırmanın iki tarafı da aynı SQL
  `LOWER()` fonksiyonundan geçirilir.
- **Mesajlaşma "get-or-create" deseniyle**: Aynı firma aynı ilan için ikinci bir görüşme açamaz — hem
  `ConversationServiceImpl` hem de veritabanı seviyesinde (`UNIQUE (listing_id, initiator_company_id)`)
  garanti edilir. Görüşme yanıtındaki "karşı taraf", her kullanıcı için o kullanıcının bakış açısına göre
  hesaplanır.
- **SOLID**:
  - *SRP*: Controller sadece HTTP çevirisi yapar, iş kuralı Service'te, veri erişimi Repository'de, hata
    haritalama tek noktada `GlobalExceptionHandler`'da toplanır.
  - *OCP/DIP*: Controller'lar somut sınıflara değil `AuthService` / `CompanyService` arayüzlerine bağımlıdır;
    implementasyon değişse de controller etkilenmez, testlerde kolayca sahtelenebilir (`AuthServiceImplTest`).
  - `User` entity'si bilerek Spring Security'nin `UserDetails` arayüzünü uygulamaz; bu uyarlama
    `security.UserPrincipal` içinde izole edilmiştir — domain modeli framework detayından bağımsızdır.
- **Şema yönetimi Flyway ile yapılır** (`spring.jpa.hibernate.ddl-auto=validate`); `ddl-auto=update` gibi
  öngörülemeyen şema sürüklenmesine izin veren yaklaşımlar bilerek kullanılmamıştır.
- **DTO ↔ Entity dönüşümü MapStruct ile** derleme zamanında üretilir, elle yazılan mapper kodu yoktur.

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

1. PostgreSQL'i ayağa kaldır:
   ```bash
   docker compose up -d
   ```
2. Uygulamayı çalıştır:
   ```bash
   ./gradlew bootRun
   ```
3. Swagger UI: http://localhost:8080/swagger-ui.html

### Ortam Değişkenleri

| Değişken | Varsayılan | Açıklama |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/carbovira` | Veritabanı bağlantısı |
| `DB_USERNAME` / `DB_PASSWORD` | `carbovira` / `carbovira` | docker-compose.yml ile aynı |
| `JWT_SECRET` | (dev varsayılanı) | **Production'da mutlaka değiştirilmeli**, en az 256 bit |
| `JWT_EXPIRATION_MS` | `86400000` (24 saat) | Token geçerlilik süresi |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | (boş) | İkisi de doluysa ve bu e-postayla kullanıcı yoksa, uygulama açılışında ilk PLATFORM_ADMIN otomatik oluşturulur |

### Testler

```bash
./gradlew test
```

Testler gerçek bir Postgres/Docker gerektirmez; `test` profili bellek içi H2 kullanır
(`src/test/resources/application-test.properties`).

## API Uç Noktaları

### Faz 1 — Auth & Company

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/auth/register` | Firma + ilk kullanıcı (COMPANY_ADMIN) kaydı | Herkese açık |
| POST | `/api/auth/login` | JWT token üretimi | Herkese açık |
| GET | `/api/companies/me` | Giriş yapan kullanıcının firma profili | JWT gerekli |
| PUT | `/api/companies/me` | Firma profilini güncelle | JWT gerekli |
| GET | `/api/companies/{id}` | Herkese açık firma profili | JWT gerekli |

### Faz 2 — Listing (İlan)

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/listings` | Yeni atık/hammadde ilanı oluştur (ACTIVE başlar) | JWT gerekli |
| GET | `/api/listings` | ACTIVE ilanlarda ara (`type`, `category`, `city`, `q`, sayfalama) | JWT gerekli |
| GET | `/api/listings/mine` | Kendi firmamın tüm ilanları (durum fark etmez) | JWT gerekli |
| GET | `/api/listings/{id}` | İlan detayı | JWT gerekli |
| PUT | `/api/listings/{id}` | İlanı güncelle (sadece sahibi) | JWT gerekli |
| PATCH | `/api/listings/{id}/status` | Durumu değiştir: ACTIVE/PASSIVE/ARCHIVED (sadece sahibi) | JWT gerekli |
| DELETE | `/api/listings/{id}` | İlanı sil (sadece sahibi) | JWT gerekli |

### Faz 3 — Messaging (Görüşme)

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/conversations` | İlan hakkında görüşme başlat/devam ettir (ilk/yeni mesajla birlikte) | JWT gerekli |
| GET | `/api/conversations` | Taraf olduğum tüm görüşmeler | JWT gerekli |
| GET | `/api/conversations/{id}/messages` | Görüşmedeki mesajlar (sadece taraflar) | JWT gerekli |
| POST | `/api/conversations/{id}/messages` | Görüşmeye mesaj gönder (sadece taraflar) | JWT gerekli |

Kendi ilanınıza mesaj gönderemezsiniz (`400`). Aynı firma aynı ilan için ikinci bir görüşme açamaz;
tekrar mesaj gönderildiğinde mevcut görüşmeye eklenir. Taraf olmadığınız bir görüşmeye erişim `403` döner.

### Faz 4 — Admin (Firma Onayı)

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| GET | `/api/admin/companies` | Firmaları listele (`verified` filtresiyle, ops.) | PLATFORM_ADMIN |
| PATCH | `/api/admin/companies/{id}/verify` | Firmayı onayla | PLATFORM_ADMIN |

Onay şu an için **sadece bilgilendirici bir rozet**: onaylanmamış firmalar da ilan verip mesajlaşabilir,
`verified=false` sadece `CompanyResponse` üzerinden görünür. İleride zorunlu hale getirilmek istenirse
`ListingService.create()` içine tek bir kontrol eklemek yeterlidir.

İlk platform yöneticisi, register uç noktasından oluşturulamaz (register her zaman COMPANY_ADMIN üretir).
`ADMIN_EMAIL`/`ADMIN_PASSWORD` ortam değişkenleri tanımlıysa uygulama açılışında otomatik oluşturulur
(bkz. `admin.AdminBootstrapRunner`).

Tüm istekler (auth uçları hariç) `Authorization: Bearer <token>` başlığı bekler. Başka bir firmanın
ilanında güncelleme/silme denemesi `403 Forbidden` ile sonuçlanır.

## Yol Haritası

Faz 1 (Auth & Company), Faz 2 (Listing), Faz 3 (Messaging) ve Faz 4 (Admin onayı) tamamlandı. Sonraki
olası adımlar: bildirimler, ilan/firma için dosya-görsel yükleme, ilan onayı zorunlu hale getirme —
proje raporunda (`Carbovira_Proje_Raporu.docx`) detaylandırılmıştır.
