# Carbovira

![CI](https://github.com/ridvanKarsli/CarboviraRestAPI/actions/workflows/ci.yml/badge.svg)

Firmaların atıklarını başka bir firmanın hammaddesi olarak ilan edebildiği, arayabildiği ve
platform üzerinden iletişime geçebildiği bir REST API. Endüstriyel simbiyoz fikrini
dijitalleştirmeye çalışıyorum.

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

Yakınlık aramasında (`/api/listings/nearby`) PostGIS falan yok, mesafe hesabı Haversine formülüyle
(`common.GeoUtils`) Java tarafında yapılıyor — aday ilanları çekip mesafeye göre filtrelip
sıralıyorum. İlan sayısı gerçekten büyürse bunun veritabanı seviyesine taşınması lazım ama şimdilik
yeterli. Atık taşımacılığının belli bir mesafeden sonra ekonomik olmaktan çıktığını okuyunca
eklemeye karar verdim.

İlan spesifikasyonları (`Listing.attributes`) sabit kolonlar yerine key-value tutuluyor çünkü her
malzeme tipinin alanları farklı (nem oranı, saflık yüzdesi, parçacık boyutu...) — hepsine ayrı kolon
açmak anlamsız olurdu. `specSheetUrl` de gerçek dosya yükleme değil, sadece bir link alanı.

Etki raporundaki karbon rakamı (`common.CarbonEstimator`) kategori adında geçen birkaç anahtar
kelimeye (metal, plastik, kağıt, cam, tekstil...) göre kaba bir kg CO2e/kg katsayısı uyguluyor,
bilinmeyen kategoriler için sabit bir ortalama değer kullanıyor. Ölçülmüş bir veri değil, yayınlanmış
ortalamalara dayanan bir tahmin — rapor öyle sunuluyor zaten. Satış tarafı kendi arşivlenen
ilanlarımdan, alış tarafı benim başlattığım görüşmelerdeki karşı tarafın arşivlenen ilanlarından
hesaplanıyor; `from`/`to` verilmezse tüm geçmiş, verilirse sadece o aralık (aylık, yıllık, ne
istenirse) dikkate alınıyor.

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

`frontend/` klasörü ayrı bir React (Vite) uygulaması, backend'i tüketen gerçek bir
kullanıcı arayüzü — kayıt/giriş, ilan arama/verme, mesajlaşma, firma profili, etki/karbon
raporu. Ayrıntı için `frontend/README.md`.

## Yerel Geliştirme

1. Postgres'i ayağa kaldır:
   ```bash
   docker compose up -d
   ```
2. Backend'i çalıştır:
   ```bash
   ./gradlew bootRun
   ```
3. Swagger: http://localhost:8080/swagger-ui.html
4. Frontend'i (opsiyonel, ayrı terminalde) çalıştır:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   http://localhost:5173'te açılır. Backend'de `app.cors.allowed-origins` bu adresi zaten
   izin veriyor.

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

Bunun dışında ayrı bir `e2eTest` task'ı var:

```bash
./gradlew e2eTest
```

Bu, servisleri mock'lamak yerine gerçek bir Postgres'e (Testcontainers ile ayağa kalkıyor,
Docker gerektirir) karşı gerçek HTTP istekleriyle tüm akışı test ediyor: kayıt, giriş, ilan
oluşturma/arama, mesajlaşma, admin onayı, yetkisiz erişim denemeleri
(`src/e2eTest/java/.../e2e/CarboviraApiE2ETest.java`). `test`'ten kasıtlı olarak ayrı
tuttum ki Docker açık değilken bile normal `./gradlew build` çalışsın. CI'da build'den
sonra ayrı bir adımda otomatik çalışıyor, GitHub'ın runner'larında Docker zaten hazır geliyor.

## API

Aşağıdakilerin hepsi `Authorization: Bearer <token>` ister, tek istisna `/api/auth/**`.
Detaylı örnekler için Swagger daha rahat: http://localhost:8080/swagger-ui.html

Auth & Company:
- `POST /api/auth/register` — firma + ilk kullanıcıyı (COMPANY_ADMIN) birlikte oluşturur, herkese açık
- `POST /api/auth/login` — herkese açık
- `GET /api/companies/me`, `PUT /api/companies/me` — kendi firma profilim (konum dahil)
- `GET /api/companies/{id}` — başka firmanın genel profili
- `GET /api/companies/me/impact-report` — toplam/aktif/arşiv ilan sayıları, birim bazlı toplam
  miktar, toplam görüşme sayısı ve (opsiyonel `from`/`to` ile) o dönemde satılan/satın alınan
  malzemeden önlenen tahmini karbon — sürdürülebilirlik raporlaması için kaba bir özet

Listing:
- `POST /api/listings` — yeni ilan, ACTIVE olarak başlar (opsiyonel `specSheetUrl` ve `attributes`)
- `GET /api/listings` — sadece ACTIVE ilanlarda arama (`type`, `category`, `city`, `q`, sayfalama)
- `GET /api/listings/nearby` — çağıran firmanın konumuna göre `radiusKm` içindeki ACTIVE ilanlar,
  en yakından en uzağa; çağıran firmanın profilinde konum girilmiş olmalı
- `GET /api/listings/mine` — kendi firmamın tüm ilanları, durum fark etmeksizin
- `GET /api/listings/{id}`
- `PUT /api/listings/{id}`, `PATCH /api/listings/{id}/status`, `DELETE /api/listings/{id}` — sadece ilan sahibi

Messaging:
- `POST /api/conversations` — ilan hakkında görüşme başlatır ya da varsa mevcut görüşmeye mesaj ekler
- `GET /api/conversations` — taraf olduğum görüşmeler
- `GET /api/conversations/{id}/messages`, `POST /api/conversations/{id}/messages` — sadece taraflar

Kendi ilanına mesaj gönderemezsin (400). Taraf olmadığın görüşmeye erişim 403.

Admin (sadece PLATFORM_ADMIN):
- `GET /api/admin/companies` — `verified` filtresi opsiyonel
- `PATCH /api/admin/companies/{id}/verify`

Onay şu an sadece bilgilendirici bir rozet, onaylanmamış firmalar da ilan verip mesajlaşabiliyor.
İleride zorunlu yapmak istersem `ListingService.create()`'e tek bir kontrol eklemek yeterli olur.
İlk platform yöneticisi register'dan gelmiyor, `ADMIN_EMAIL`/`ADMIN_PASSWORD` tanımlıysa açılışta
otomatik oluşuyor (`admin.AdminBootstrapRunner`).

## Sırada ne var

Bildirimler, ilan/firma için görsel yükleme, ilan onayının zorunlu hale getirilmesi — şimdilik
fikir aşamasında, vaktim olunca bakacağım.
