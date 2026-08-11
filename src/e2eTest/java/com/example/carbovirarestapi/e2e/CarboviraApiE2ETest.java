package com.example.carbovirarestapi.e2e;

import com.example.carbovirarestapi.auth.dto.AuthResponse;
import com.example.carbovirarestapi.auth.dto.LoginRequest;
import com.example.carbovirarestapi.auth.dto.RegisterRequest;
import com.example.carbovirarestapi.common.dto.PageResponse;
import com.example.carbovirarestapi.common.exception.ApiError;
import com.example.carbovirarestapi.company.dto.CompanyResponse;
import com.example.carbovirarestapi.listing.ListingStatus;
import com.example.carbovirarestapi.listing.ListingType;
import com.example.carbovirarestapi.listing.dto.ListingCreateRequest;
import com.example.carbovirarestapi.listing.dto.ListingResponse;
import com.example.carbovirarestapi.listing.dto.ListingStatusUpdateRequest;
import com.example.carbovirarestapi.messaging.dto.ConversationResponse;
import com.example.carbovirarestapi.messaging.dto.ConversationStartRequest;
import com.example.carbovirarestapi.messaging.dto.MessageResponse;
import com.example.carbovirarestapi.messaging.dto.MessageSendRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Servis testlerinde her şeyi mock'luyoruz, burada tam tersi: gerçek bir Postgres'e
 * (Testcontainers ile ayağa kalkan) karşı, gerçek HTTP istekleriyle, uygulamayı hiç
 * dokunmadan uçtan uca test ediyoruz. Flyway migration'ları da bu container üzerinde
 * gerçekten çalışıyor — test profilindeki H2'nin aksine.
 *
 * Akış tek bir hikaye: iki firma kayıt olur, biri ilan açar, öbürü mesaj atar,
 * platform admin'i firmayı onaylar. Adımlar birbirine bağlı olduğundan @Order ile
 * sıralı çalıştırılıyor ve firma/ilan/görüşme kimlikleri instance alanlarında taşınıyor.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "app.admin.email=e2e-admin@carbovira.com",
        "app.admin.password=e2e-admin-sifresi-123"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CarboviraApiE2ETest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    private String sellerToken;
    private Long sellerCompanyId;
    private String buyerToken;
    private Long listingId;
    private Long conversationId;

    @Test
    @Order(1)
    void sellerCompanyCanRegister() {
        RegisterRequest request = new RegisterRequest(
                "Acme Geri Dönüşüm", "1111111111", "Metal", "İstanbul", "OSB 5. Cadde No:1",
                "Ayşe Yılmaz", "seller@e2e-test.com", "guclu-sifre-123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        sellerToken = response.getBody().token();
        sellerCompanyId = response.getBody().companyId();
    }

    @Test
    @Order(2)
    void duplicateEmailIsRejected() {
        RegisterRequest request = new RegisterRequest(
                "Başka Firma", "2222222222", "Kimya", "Ankara", "Adres",
                "Mehmet Demir", "seller@e2e-test.com", "guclu-sifre-123");

        ResponseEntity<ApiError> response = restTemplate.postForEntity("/api/auth/register", request, ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(3)
    void sellerCanFetchOwnCompanyProfile() {
        ResponseEntity<CompanyResponse> response = restTemplate.exchange(
                "/api/companies/me", HttpMethod.GET, authedEntity(sellerToken, null), CompanyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Acme Geri Dönüşüm");
        assertThat(response.getBody().verified()).isFalse();
    }

    @Test
    @Order(4)
    void sellerCanCreateListing() {
        ListingCreateRequest request = new ListingCreateRequest(
                ListingType.WASTE, "500 kg PET plastik atığı", "Plastik",
                "Temiz, tek tip PET şişe atığı", BigDecimal.valueOf(500), "kg", "İstanbul", null);

        ResponseEntity<ListingResponse> response = restTemplate.exchange(
                "/api/listings", HttpMethod.POST, authedEntity(sellerToken, request), ListingResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().status()).isEqualTo(ListingStatus.ACTIVE);
        listingId = response.getBody().id();
    }

    @Test
    @Order(5)
    void listingAppearsInSearch() {
        ResponseEntity<PageResponse<ListingResponse>> response = restTemplate.exchange(
                "/api/listings?type=WASTE&q=PET", HttpMethod.GET, authedEntity(sellerToken, null),
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).extracting(ListingResponse::id).contains(listingId);
    }

    @Test
    @Order(6)
    void buyerCompanyCanRegister() {
        RegisterRequest request = new RegisterRequest(
                "Yeşil Dönüşüm", "3333333333", "Plastik İşleme", "Bursa", "Adres",
                "Elif Kaya", "buyer@e2e-test.com", "guclu-sifre-123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        buyerToken = response.getBody().token();
    }

    @Test
    @Order(7)
    void sellerCannotMessageOwnListing() {
        ConversationStartRequest request = new ConversationStartRequest(listingId, "Merhaba");

        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/conversations", HttpMethod.POST, authedEntity(sellerToken, request), ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(8)
    void buyerCanStartConversation() {
        ConversationStartRequest request = new ConversationStartRequest(
                listingId, "Merhaba, bu ilandaki PET atığın haftalık ne kadarını tedarik edebilirsiniz?");

        ResponseEntity<ConversationResponse> response = restTemplate.exchange(
                "/api/conversations", HttpMethod.POST, authedEntity(buyerToken, request), ConversationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().counterpartCompanyId()).isEqualTo(sellerCompanyId);
        conversationId = response.getBody().id();
    }

    @Test
    @Order(9)
    void sellerSeesTheMessageAndCanReply() {
        ResponseEntity<PageResponse<MessageResponse>> messages = restTemplate.exchange(
                "/api/conversations/" + conversationId + "/messages", HttpMethod.GET,
                authedEntity(sellerToken, null), new ParameterizedTypeReference<>() {});

        assertThat(messages.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(messages.getBody().content()).hasSize(1);

        MessageSendRequest reply = new MessageSendRequest("Tabii, aylık 2 ton kadar tedarik edebiliriz.");
        ResponseEntity<MessageResponse> response = restTemplate.exchange(
                "/api/conversations/" + conversationId + "/messages", HttpMethod.POST,
                authedEntity(sellerToken, reply), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Order(10)
    void thirdPartyCannotReadTheConversation() {
        RegisterRequest request = new RegisterRequest(
                "Üçüncü Firma", "4444444444", "Lojistik", "İzmir", "Adres",
                "Can Öz", "outsider@e2e-test.com", "guclu-sifre-123");
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class);
        String outsiderToken = registerResponse.getBody().token();

        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/conversations/" + conversationId + "/messages", HttpMethod.GET,
                authedEntity(outsiderToken, null), ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(11)
    void ownerCanArchiveListing() {
        ListingStatusUpdateRequest request = new ListingStatusUpdateRequest(ListingStatus.ARCHIVED);

        ResponseEntity<ListingResponse> response = restTemplate.exchange(
                "/api/listings/" + listingId + "/status", HttpMethod.PATCH,
                authedEntity(sellerToken, request), ListingResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo(ListingStatus.ARCHIVED);
    }

    @Test
    @Order(12)
    void buyerCannotChangeSomeoneElsesListingStatus() {
        ListingStatusUpdateRequest request = new ListingStatusUpdateRequest(ListingStatus.ACTIVE);

        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/listings/" + listingId + "/status", HttpMethod.PATCH,
                authedEntity(buyerToken, request), ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(13)
    void wrongPasswordIsRejected() {
        LoginRequest request = new LoginRequest("seller@e2e-test.com", "yanlis-sifre");

        ResponseEntity<ApiError> response = restTemplate.postForEntity("/api/auth/login", request, ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(14)
    void adminBootstrapUserCanLoginAndVerifyCompany() {
        LoginRequest loginRequest = new LoginRequest("e2e-admin@carbovira.com", "e2e-admin-sifresi-123");
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, AuthResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String adminToken = loginResponse.getBody().token();

        ResponseEntity<CompanyResponse> verifyResponse = restTemplate.exchange(
                "/api/admin/companies/" + sellerCompanyId + "/verify", HttpMethod.PATCH,
                authedEntity(adminToken, null), CompanyResponse.class);

        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(verifyResponse.getBody().verified()).isTrue();
    }

    @Test
    @Order(15)
    void nonAdminCannotAccessAdminEndpoints() {
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/admin/companies", HttpMethod.GET, authedEntity(sellerToken, null), ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private HttpEntity<Object> authedEntity(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
