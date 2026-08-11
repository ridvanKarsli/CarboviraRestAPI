Bir firmanın atığı, başka bir firmanın hammaddesi olabilir. Carbovira'yı bu problem üzerine kurdum: firmaların atıklarını ilan edebildiği, ihtiyaç duyan firmaların arayıp bulabildiği ve doğrudan mesajlaşarak anlaşabildiği bir REST API — endüstriyel simbiyozun dijital altyapısı.

Projeyi baştan sona, gerçek bir üretim ortamının standartlarıyla kurdum:

Mimari: Spring Boot üzerinde feature-based paket yapısı, SOLID prensiplerine uygun servis/arayüz ayrımı, JWT tabanlı stateless kimlik doğrulama, Flyway ile kontrollü şema versiyonlama, MapStruct ile entity-DTO dönüşümü.

Test stratejisi: Servis katmanı için izole birim testler, ardından Testcontainers ile ayağa kaldırılan gerçek bir PostgreSQL'e karşı gerçek HTTP istekleriyle çalışan uçtan uca testler — kayıttan admin onayına kadar tüm kullanıcı akışını doğruluyor. Tamamı GitHub Actions üzerinde her push'ta otomatik çalışıyor.

Projeyi Spring Boot'un henüz çok yeni bir sürümüyle geliştirdim; framework'ün auto-configuration mekanizmasını modüllere ayıran köklü bir değişikliğin getirdiği, henüz hiçbir yerde dokümante edilmemiş çok sayıda soruna tek başıma kaynağından inerek çözüm ürettim.

Bu proje benim için sadece bir uygulama değil, bir çalışma disiplininin göstergesi: mimariden teste, CI/CD'den prodüksiyon kalitesinde hata yönetimine kadar her katmanda sorumluluk almak.

Kod tabanı: https://github.com/ridvanKarsli/CarboviraRestAPI
