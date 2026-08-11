# Carbovira — Endüstriyel Simbiyoz Platformu

Firmaların atıklarını başka bir firmanın hammaddesi olarak ilan edebildiği, arayabildiği ve
platform üzerinden iletişime geçebildiği bir REST API.

## Mimari İlkeler

- **Feature-based (özellik bazlı) paket yapısı**: `auth`, `company`, `user`, `security`, `common`, `config`.
  Her modül kendi controller / service (arayüz + impl) / repository / dto katmanını bir arada barındırır.
  İlerleyen fazlarda eklenecek `listing` ve `messaging` modülleri de aynı şablonu izleyecek.
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
└── auth       # Kayıt/giriş DTO'ları, AuthService, AuthController
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

### Testler

```bash
./gradlew test
```

Testler gerçek bir Postgres/Docker gerektirmez; `test` profili bellek içi H2 kullanır
(`src/test/resources/application-test.properties`).

## API Uç Noktaları (Faz 1)

| Metot | Yol | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/auth/register` | Firma + ilk kullanıcı (COMPANY_ADMIN) kaydı | Herkese açık |
| POST | `/api/auth/login` | JWT token üretimi | Herkese açık |
| GET | `/api/companies/me` | Giriş yapan kullanıcının firma profili | JWT gerekli |
| PUT | `/api/companies/me` | Firma profilini güncelle | JWT gerekli |
| GET | `/api/companies/{id}` | Herkese açık firma profili | JWT gerekli |

Tüm istekler (auth uçları hariç) `Authorization: Bearer <token>` başlığı bekler.

## Yol Haritası

Sonraki fazlar (`listing`, arama/filtreleme, `messaging`, admin onay akışı) proje raporunda
(`Carbovira_Proje_Raporu.docx`) detaylandırılmıştır ve aynı feature-based mimariyi izleyecektir.
