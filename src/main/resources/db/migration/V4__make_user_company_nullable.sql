-- PLATFORM_ADMIN kullanicilari herhangi bir firmanin parcasi olmadigindan
-- company_id artik zorunlu degil (sadece PLATFORM_ADMIN disindaki roller icin uygulama
-- katmaninda zorunlu tutulur; bkz. AuthServiceImpl, AdminBootstrapRunner).
ALTER TABLE users ALTER COLUMN company_id DROP NOT NULL;
