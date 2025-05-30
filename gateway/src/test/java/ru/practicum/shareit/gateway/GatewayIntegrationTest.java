package ru.practicum.shareit.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.common.dto.item.ItemShortDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;
import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.common.exception.ErrorMessage;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayIntegrationTest {

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    private static final int MOCK_SERVER_PORT = TestSocketUtils.findAvailableTcpPort();

    private MockWebServer mockWebServer;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("shareit-server.url", () -> "http://localhost:" + MOCK_SERVER_PORT);
    }

    @BeforeEach
    void setUpPerTest() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(MOCK_SERVER_PORT);
    }

    @AfterEach
    void tearDownPerTest() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    private void enqueueMockResponse(int status, String body) {
        mockWebServer.enqueue(new MockResponse().setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(body));
    }

    private void enqueueMockResponse(int status) {
        mockWebServer.enqueue(new MockResponse().setResponseCode(status));
    }

    private <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    private RecordedRequest takeRequestOrFail() throws InterruptedException {
        RecordedRequest request = mockWebServer.takeRequest(500, TimeUnit.MILLISECONDS);
        assertNotNull(request,
                "Expected a request to reach MockWebServer, but none arrived within the timeout.");
        return request;
    }

    @Nested
    @DisplayName("User Routes (/users)")
    class UserRoutesTests {

        @Test
        @DisplayName("POST /users - Created (Valid User)")
        void createUser_whenValid_shouldForwardAndReturnCreated() throws Exception {
            NewUserDto newUser = new NewUserDto("Test User", "test@example.com");
            String expectedResponseBody =
                    "{ \"id\": 1, \"name\": \"Test User\", \"email\": " + "\"test@example.com\" }";
            enqueueMockResponse(HttpStatus.CREATED.value(), expectedResponseBody);

            webTestClient.post().uri("/users").contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(newUser)).exchange().expectStatus().isCreated()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("POST", recordedRequest.getMethod(),
                    "Recorded request method should be POST");
            assertEquals("/users", recordedRequest.getPath(),
                    "Recorded request path should be /users");
            assertEquals(toJson(newUser), recordedRequest.getBody().readUtf8(),
                    "Recorded request body should match the sent NewUserDto JSON");
            assertNull(recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should not have X-Sharer-User-Id header");
        }

        @Test
        @DisplayName("POST /users - Bad Request (Invalid DTO - blank name)")
        void createUser_whenInvalidDtoNameBlank_shouldReturnBadRequest() {
            NewUserDto invalidUser = new NewUserDto(" ", "test@example.com");

            webTestClient.post().uri("/users").contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(invalidUser)).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for blank name should be specific")
                            .isEqualTo("Validation failed: Name cannot be " + "blank"));
        }

        @Test
        @DisplayName("POST /users - Bad Request (Invalid DTO - invalid email)")
        void createUser_whenInvalidDtoEmailInvalid_shouldReturnBadRequest() {
            NewUserDto invalidUser = new NewUserDto("Test User", "invalid-email");

            webTestClient.post().uri("/users").contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(invalidUser)).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for invalid email should be specific")
                            .isEqualTo("Validation failed: Invalid email " + "format"));
        }

        @Test
        @DisplayName("PATCH /users/{id} - OK (Valid Update)")
        void updateUser_whenValid_shouldForwardAndReturnOk() throws Exception {
            long userId = 1L;
            UpdateUserDto updateUser = new UpdateUserDto("Updated Name", null);
            String expectedResponseBody = "{ \"id\": 1, \"name\": \"Updated Name\", \"email\": "
                    + "\"original@example.com\" }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.patch().uri("/users/" + userId).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(updateUser)).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("PATCH", recordedRequest.getMethod(),
                    "Recorded request method should be PATCH");
            assertEquals("/users/" + userId, recordedRequest.getPath(),
                    "Recorded request path should be /users/{id}");
            assertEquals(toJson(updateUser), recordedRequest.getBody().readUtf8(),
                    "Recorded request body should match the sent UpdateUserDto JSON");
            assertNull(recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should not have X-Sharer-User-Id header");
        }

        @Test
        @DisplayName("PATCH /users/{id} - Bad Request (Invalid DTO - invalid email)")
        void updateUser_whenInvalidDtoEmailInvalid_shouldReturnBadRequest() {
            long userId = 1L;
            UpdateUserDto invalidUpdate = new UpdateUserDto(null, "invalid-email");

            webTestClient.patch().uri("/users/" + userId).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(invalidUpdate)).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for invalid email should be specific")
                                    .isEqualTo("Validation failed: Invalid email " + "format"));
        }

        @Test
        @DisplayName("GET /users/{id} - OK (Valid ID)")
        void getUserById_shouldForwardAndReturnOk() throws Exception {
            long userId = 1L;
            String expectedResponseBody =
                    "{ \"id\": 1, \"name\": \"Test User\", \"email\": " + "\"test@example.com\" }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri("/users/" + userId).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals("/users/" + userId, recordedRequest.getPath(),
                    "Recorded request path should be /users/{id}");
            assertNull(recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should not have X-Sharer-User-Id header");
        }

        @Test
        @DisplayName("GET /users/{id} - Not Found (Backend Error)")
        void getUserById_whenBackendReturnsNotFound_shouldProxyNotFound() throws Exception {
            long userId = 999L;
            ErrorMessage backendError = new ErrorMessage("User not found",
                    HttpStatus.NOT_FOUND.value());
            enqueueMockResponse(HttpStatus.NOT_FOUND.value(), toJson(backendError));

            webTestClient.get().uri("/users/" + userId).exchange().expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(ErrorMessage.class).isEqualTo(backendError);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals("/users/" + userId, recordedRequest.getPath(),
                    "Recorded request path should be /users/{id}");
        }

        @Test
        @DisplayName("GET /users - OK (Multiple Users)")
        void getAllUsers_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "[{ \"id\": 1, ... }, { \"id\": 2, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri("/users").exchange().expectStatus().isOk().expectHeader()
                    .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals("/users", recordedRequest.getPath(),
                    "Recorded request path should be /users");
            assertNull(recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should not have X-Sharer-User-Id header");
        }

        @Test
        @DisplayName("DELETE /users/{id} - No Content (Valid ID)")
        void deleteUser_shouldForwardAndReturnNoContent() throws Exception {
            long userId = 1L;
            enqueueMockResponse(HttpStatus.NO_CONTENT.value());

            webTestClient.delete().uri("/users/" + userId).exchange().expectStatus().isNoContent();

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("DELETE", recordedRequest.getMethod(),
                    "Recorded request method should be DELETE");
            assertEquals("/users/" + userId, recordedRequest.getPath(),
                    "Recorded request path should be /users/{id}");
            assertNull(recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should not have X-Sharer-User-Id header");
        }
    }

    @Nested
    @DisplayName("Item Routes (/items)")
    class ItemRoutesTests {

        private final String validUserIdHeader = "1";
        private final String itemsPath = "/items";
        private final long testItemId = 10L;

        @Test
        @DisplayName("POST /items - Created (Valid Item, Valid Header)")
        void createItem_whenValid_shouldForwardAndReturnCreated() throws Exception {
            NewItemDto newItem = new NewItemDto();
            newItem.setName("Test Item");
            newItem.setDescription("Desc");
            newItem.setAvailable(true);
            String expectedResponseBody = "{ \"id\": 10, \"name\": \"Test Item\", "
                    + "\"description\": \"Desc\", \"available\": true }";
            enqueueMockResponse(HttpStatus.CREATED.value(), expectedResponseBody);

            webTestClient.post().uri(itemsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newItem))
                    .exchange().expectStatus().isCreated().expectHeader()
                    .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("POST", recordedRequest.getMethod(),
                    "Recorded request method should be POST");
            assertEquals(itemsPath, recordedRequest.getPath(),
                    "Recorded request path should be /items");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
            assertThat(recordedRequest.getBody().readUtf8()).as(
                            "Recorded request body should contain the item name")
                    .contains("\"name\":\"Test Item\"");
        }

        @Test
        @DisplayName("POST /items - Bad Request (Missing Header)")
        void createItem_whenMissingHeader_shouldReturnBadRequest() {
            NewItemDto newItem = new NewItemDto();
            newItem.setName("Test Item");
            newItem.setDescription("Desc");
            newItem.setAvailable(true);

            webTestClient.post().uri(itemsPath).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(newItem)).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for missing header should be specific")
                            .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("POST /items - Bad Request (Invalid Header - non-numeric)")
        void createItem_whenInvalidHeader_shouldReturnBadRequest() {
            NewItemDto newItem = new NewItemDto();
            newItem.setName("Test Item");
            newItem.setDescription("Desc");
            newItem.setAvailable(true);

            webTestClient.post().uri(itemsPath).header(HEADER_USER_ID, "invalid")
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newItem))
                    .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for invalid header format should be specific")
                                    .isEqualTo("Invalid format for header 'X-Sharer-User-Id'"));
        }

        @Test
        @DisplayName("POST /items - Bad Request (Valid Header, Invalid DTO - blank name)")
        void createItem_whenInvalidDtoNameBlank_shouldReturnBadRequest() {
            NewItemDto invalidItem = new NewItemDto();
            invalidItem.setName(" ");
            invalidItem.setDescription("Desc");
            invalidItem.setAvailable(true);

            webTestClient.post().uri(itemsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(invalidItem))
                    .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for blank name should be specific")
                                    .isEqualTo("Validation failed: Name cannot be blank"));
        }

        @Test
        @DisplayName("POST /items - Bad Request (Valid Header, Invalid DTO - null available)")
        void createItem_whenInvalidDtoAvailableNull_shouldReturnBadRequest() {
            NewItemDto invalidItem = new NewItemDto();
            invalidItem.setName("Item");
            invalidItem.setDescription("Desc");
            invalidItem.setAvailable(null);

            webTestClient.post().uri(itemsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(invalidItem))
                    .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for null available status should be specific")
                                    .isEqualTo("Validation failed: Item status must be set"));
        }

        @Test
        @DisplayName("PATCH /items/{id} - OK (Valid Update, Valid Header)")
        void updateItem_whenValid_shouldForwardAndReturnOk() throws Exception {
            UpdateItemDto updateItem = new UpdateItemDto("Updated Item", null, null);
            String expectedResponseBody = "{ \"id\": " + testItemId
                    + ", \"name\": \"Updated Item\", \"description\": \"Original Desc\", "
                    + "\"available\": true }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.patch().uri(itemsPath + "/" + testItemId)
                    .header(HEADER_USER_ID, validUserIdHeader).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(updateItem)).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("PATCH", recordedRequest.getMethod(),
                    "Recorded request method should be PATCH");
            assertEquals(itemsPath + "/" + testItemId, recordedRequest.getPath(),
                    "Recorded request path should be /items/{id}");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
            assertThat(recordedRequest.getBody().readUtf8()).as(
                            "Recorded request body should contain the updated item name")
                    .contains("\"name\":\"Updated Item\"");
        }

        @Test
        @DisplayName("PATCH /items/{id} - Bad Request (Missing Header)")
        void updateItem_whenMissingHeader_shouldReturnBadRequest() {
            UpdateItemDto updateItem = new UpdateItemDto("Updated Item", null, null);

            webTestClient.patch().uri(itemsPath + "/" + testItemId)
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(updateItem))
                    .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /items/{id} - OK (Valid Header)")
        void getItemById_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody =
                    "{ \"id\": " + testItemId + ", \"name\": \"Test Item\", ... }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(itemsPath + "/" + testItemId)
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(itemsPath + "/" + testItemId, recordedRequest.getPath(),
                    "Recorded request path should be /items/{id}");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /items/{id} - Bad Request (Missing Header)")
        void getItemById_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(itemsPath + "/" + testItemId).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /items - OK (Valid Header)")
        void getUserItems_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "[{ \"id\": 10, ... }, { \"id\": 12, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(itemsPath).header(HEADER_USER_ID, validUserIdHeader).exchange()
                    .expectStatus().isOk().expectHeader()
                    .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(itemsPath, recordedRequest.getPath(),
                    "Recorded request path should be /items");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /items - Bad Request (Missing Header)")
        void getUserItems_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(itemsPath).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for missing header should be specific")
                            .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }


        @Test
        @DisplayName("GET /items/search - OK (Valid Header and Query)")
        void searchItems_whenValid_shouldForwardAndReturnOk() throws Exception {
            String searchText = "findme";
            String expectedResponseBody = "[{ \"id\": 11, \"name\": \"Found Item\", ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(
                            uriBuilder -> uriBuilder.path(itemsPath + "/search").queryParam("text",
                                            searchText)
                                    .build()).header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus()
                    .isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(String.class).isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(itemsPath + "/search?text=" + searchText, recordedRequest.getPath(),
                    "Recorded request path should be /items/search with text query param");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /items/search - Bad Request (Missing Header)")
        void searchItems_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(
                            uriBuilder -> uriBuilder.path(itemsPath + "/search").queryParam("text",
                                            "something")
                                    .build()).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for missing header should be specific")
                            .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }


        @Test
        @DisplayName("POST /items/{itemId}/comment - OK (Valid Comment, Valid Header)")
        void addComment_whenValid_shouldForwardAndReturnOk() throws Exception {
            long itemIdForComment = 20L;
            NewCommentDto newComment = new NewCommentDto();
            newComment.setText("Comment");
            String expectedResponseBody = "{ \"id\": 5, \"text\": \"Comment\", ... }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.post().uri(itemsPath + "/" + itemIdForComment + "/comment")
                    .header(HEADER_USER_ID, validUserIdHeader).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(newComment)).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("POST", recordedRequest.getMethod(),
                    "Recorded request method should be POST");
            assertEquals(itemsPath + "/" + itemIdForComment + "/comment", recordedRequest.getPath(),
                    "Recorded request path should be /items/{itemId}/comment");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
            assertThat(recordedRequest.getBody().readUtf8()).as(
                            "Recorded request body should contain the comment text")
                    .contains("\"text\":\"Comment\"");
        }

        @Test
        @DisplayName("POST /items/{itemId}/comment - Bad Request (Valid Header, Invalid DTO - blank text)")
        void addComment_whenInvalidDtoTextBlank_shouldReturnBadRequest() {
            long itemIdForComment = 20L;
            NewCommentDto invalidComment = new NewCommentDto();
            invalidComment.setText(" ");

            webTestClient.post().uri(itemsPath + "/" + itemIdForComment + "/comment")
                    .header(HEADER_USER_ID, validUserIdHeader).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(invalidComment)).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for blank comment text should be specific")
                                    .isEqualTo("Validation failed: Comment text cannot be blank"));
        }

        @Test
        @DisplayName("POST /items/{itemId}/comment - Bad Request (Missing Header)")
        void addComment_whenMissingHeader_shouldReturnBadRequest() {
            long itemIdForComment = 20L;
            NewCommentDto newComment = new NewCommentDto();
            newComment.setText("Comment");

            webTestClient.post().uri(itemsPath + "/" + itemIdForComment + "/comment")
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newComment))
                    .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }


        @Test
        @DisplayName("DELETE /items/{id} - No Content (Valid Header)")
        void deleteItem_whenValidHeader_shouldForwardAndReturnNoContent() throws Exception {
            enqueueMockResponse(HttpStatus.NO_CONTENT.value());

            webTestClient.delete().uri(itemsPath + "/" + testItemId)
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isNoContent();

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("DELETE", recordedRequest.getMethod(),
                    "Recorded request method should be DELETE");
            assertEquals(itemsPath + "/" + testItemId, recordedRequest.getPath(),
                    "Recorded request path should be /items/{id}");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }


        @Test
        @DisplayName("DELETE /items/{id} - Bad Request (Missing Header)")
        void deleteItem_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.delete().uri(itemsPath + "/" + testItemId).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

    }

    @Nested
    @DisplayName("Booking Routes (/bookings)")
    class BookingRoutesTests {

        private final String validUserIdHeader = "2";
        private final String bookingsPath = "/bookings";
        private final long testBookingId = 50L;
        private final LocalDateTime validStart = LocalDateTime.now().plusDays(1);
        private final LocalDateTime validEnd = LocalDateTime.now().plusDays(2);

        @Test
        @DisplayName("POST /bookings - Created (Valid Booking, Valid Header)")
        void createBooking_whenValid_shouldForwardAndReturnCreated() throws Exception {
            NewBookingDto newBooking = new NewBookingDto(1L, validStart, validEnd);
            String expectedResponseBody =
                    "{ \"id\": " + testBookingId + ", \"item\": { ... }, ... }";
            enqueueMockResponse(HttpStatus.CREATED.value(), expectedResponseBody);

            webTestClient.post().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newBooking))
                    .exchange().expectStatus().isCreated().expectHeader()
                    .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("POST", recordedRequest.getMethod(),
                    "Recorded request method should be POST");
            assertEquals(bookingsPath, recordedRequest.getPath(),
                    "Recorded request path should be /bookings");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
            assertThat(recordedRequest.getBody().readUtf8()).as(
                    "Recorded request body should contain the item ID").contains("\"itemId\":1");
        }

        @Test
        @DisplayName("POST /bookings - Bad Request (Valid Header, Invalid DTO - null start)")
        void createBooking_whenInvalidDtoStartNull_shouldReturnBadRequest() {
            NewBookingDto invalidBooking = new NewBookingDto(1L, null, validEnd);

            webTestClient.post().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(invalidBooking)).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for null start date should be specific")
                                    .isEqualTo("Validation failed: Start date cannot be null"));
        }

        @Test
        @DisplayName("POST /bookings - Bad Request (Valid Header, Invalid DTO - null end)")
        void createBooking_whenInvalidDtoEndNull_shouldReturnBadRequest() {
            NewBookingDto invalidBooking = new NewBookingDto(1L, validStart, null);

            webTestClient.post().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(invalidBooking)).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for null end date should be specific")
                                    .isEqualTo("Validation failed: End date cannot be null"));
        }

        @Test
        @DisplayName("POST /bookings - Bad Request (Valid Header, Invalid DTO - null item ID)")
        void createBooking_whenInvalidDtoItemIdNull_shouldReturnBadRequest() {
            NewBookingDto invalidBooking = new NewBookingDto(null, validStart, validEnd);

            webTestClient.post().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(invalidBooking)).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for null item ID should be specific")
                                    .isEqualTo("Validation failed: Item ID cannot be null"));
        }

        @Test
        @DisplayName("POST /bookings - Bad Request (Missing Header)")
        void createBooking_whenMissingHeader_shouldReturnBadRequest() {
            NewBookingDto newBooking = new NewBookingDto(1L, validStart, validEnd);

            webTestClient.post().uri(bookingsPath).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(newBooking)).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for missing header should be specific")
                            .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("PATCH /bookings/{id} - OK (Valid Header and Query)")
        void approveBooking_whenValid_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody =
                    "{ \"id\": " + testBookingId + ", \"status\": \"APPROVED\", ... }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.patch().uri(
                            uriBuilder -> uriBuilder.path(bookingsPath + "/" + testBookingId)
                                    .queryParam("approved", "true").build())
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("PATCH", recordedRequest.getMethod(),
                    "Recorded request method should be PATCH");
            assertEquals(bookingsPath + "/" + testBookingId + "?approved=true",
                    recordedRequest.getPath(),
                    "Recorded request path should be /bookings/{id} with approved=true query");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
            assertEquals(0, recordedRequest.getBodySize(),
                    "Recorded request body size should be 0 for PATCH with query params");
        }

        @Test
        @DisplayName("PATCH /bookings/{id} - Not Found (Missing query param)")
        void approveBooking_whenMissingApprovedQuery_shouldReturnNotFound() {
            webTestClient.patch().uri(bookingsPath + "/" + testBookingId)
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isNotFound();

        }

        @Test
        @DisplayName("PATCH /bookings/{id} - Not Found (Invalid query param value)")
        void approveBooking_whenInvalidApprovedQueryValue_shouldReturnNotFound() {
            webTestClient.patch().uri(
                            uriBuilder -> uriBuilder.path(bookingsPath + "/" + testBookingId)
                                    .queryParam("approved", "maybe").build())
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isNotFound();

        }

        @Test
        @DisplayName("PATCH /bookings/{id} - Bad Request (Missing Header)")
        void approveBooking_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.patch().uri(
                            uriBuilder -> uriBuilder.path(bookingsPath + "/" + testBookingId)
                                    .queryParam("approved", "true").build()).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /bookings/{id} - OK (Valid Header)")
        void getBookingById_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "{ \"id\": " + testBookingId + ", ... }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(bookingsPath + "/" + testBookingId)
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(bookingsPath + "/" + testBookingId, recordedRequest.getPath(),
                    "Recorded request path should be /bookings/{id}");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /bookings/{id} - Bad Request (Missing Header)")
        void getBookingById_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(bookingsPath + "/" + testBookingId).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }


        @Test
        @DisplayName("GET /bookings - OK (Valid Header, Valid State)")
        void getBookingsByBooker_whenValidState_shouldForwardAndReturnOk() throws Exception {
            String state = "WAITING";
            String expectedResponseBody = "[{ \"id\": 51, \"status\": \"WAITING\", ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path(bookingsPath).queryParam("state", state).build())
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(bookingsPath + "?state=" + state, recordedRequest.getPath(),
                    "Recorded request path should be /bookings with state query param");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /bookings - Bad Request (Valid Header, Invalid State)")
        void getBookingsByBooker_whenInvalidState_shouldReturnBadRequest() {
            String invalidState = "INVALID_STATUS";

            webTestClient.get().uri(
                            uriBuilder -> uriBuilder.path(bookingsPath).queryParam("state", invalidState)
                                    .build()).header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for invalid state should be specific")
                                    .isEqualTo("Unknown state: " + invalidState));
        }

        @Test
        @DisplayName("GET /bookings - OK (Valid Header, Missing State)")
        void getBookingsByBooker_whenMissingState_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "[{ \"id\": 51, ... }, { \"id\": 52, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .exchange().expectStatus().isOk().expectHeader()
                    .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(bookingsPath, recordedRequest.getPath(),
                    "Recorded request path should be /bookings");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /bookings - Bad Request (Missing Header)")
        void getBookingsByBooker_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path(bookingsPath).queryParam("state", "ALL").build())
                    .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /bookings/owner - OK (Valid Header, Valid State)")
        void getBookingsByOwner_whenValid_shouldForwardAndReturnOk() throws Exception {
            String state = "ALL";
            String expectedResponseBody = "[{ \"id\": 55, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(
                            uriBuilder -> uriBuilder.path(bookingsPath + "/owner").queryParam("state",
                                            state)
                                    .build()).header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus()
                    .isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(String.class).isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(bookingsPath + "/owner?state=" + state, recordedRequest.getPath(),
                    "Recorded request path should be /bookings/owner with state query param");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /bookings/owner - OK (Valid Header, Missing State)")
        void getBookingsByOwner_whenMissingState_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "[{ \"id\": 55, ... }, { \"id\": 56, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(bookingsPath + "/owner")
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(bookingsPath + "/owner", recordedRequest.getPath(),
                    "Recorded request path should be /bookings/owner");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }


        @Test
        @DisplayName("GET /bookings/owner - Bad Request (Missing Header)")
        void getBookingsByOwner_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(bookingsPath + "/owner").exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }
    }

    @Nested
    @DisplayName("Item Request Routes (/requests)")
    class ItemRequestRoutesTests {

        private final String validUserIdHeader = "1";
        private final String requestsPath = "/requests";
        private final String requestsAllPath = "/requests/all";
        private final long testRequestId = 5L;

        @Test
        @DisplayName("POST /requests - Created (Valid Request, Valid Header)")
        void createRequest_whenValid_shouldForwardAndReturnCreated() throws Exception {
            NewItemRequestDto newRequest = new NewItemRequestDto("Need a power drill");
            ItemRequestDto createdDto = new ItemRequestDto(testRequestId,
                    newRequest.getDescription(), LocalDateTime.now(), Collections.emptySet());
            enqueueMockResponse(HttpStatus.CREATED.value(), toJson(createdDto));

            webTestClient.post().uri(requestsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newRequest))
                    .exchange().expectStatus().isCreated().expectHeader()
                    .contentType(MediaType.APPLICATION_JSON).expectBody(ItemRequestDto.class)
                    .isEqualTo(createdDto);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("POST", recordedRequest.getMethod(),
                    "Recorded request method should be POST");
            assertEquals(requestsPath, recordedRequest.getPath(),
                    "Recorded request path should be /requests");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
            assertEquals(toJson(newRequest), recordedRequest.getBody().readUtf8(),
                    "Recorded request body should match the sent NewItemRequestDto JSON");
        }

        @Test
        @DisplayName("POST /requests - Bad Request (Valid Header, Invalid DTO - blank description)")
        void createRequest_whenInvalidDto_shouldReturnBadRequest() throws InterruptedException {
            NewItemRequestDto invalidRequest = new NewItemRequestDto("   ");

            webTestClient.post().uri(requestsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(invalidRequest)).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for blank description should be specific")
                                    .isEqualTo("Validation failed: Request description cannot be blank"));

            assertNull(mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS),
                    "No request should be sent to the backend on validation error");
        }

        @Test
        @DisplayName("POST /requests - Bad Request (Missing Header)")
        void createRequest_whenMissingHeader_shouldReturnBadRequest() {
            NewItemRequestDto newRequest = new NewItemRequestDto("Need something");

            webTestClient.post().uri(requestsPath).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(newRequest)).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for missing header should be specific")
                            .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /requests - OK (Valid Header)")
        void getOwnRequests_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            ItemRequestDto req1 = new ItemRequestDto(1L, "Desc1", LocalDateTime.now().minusDays(1),
                    Collections.emptySet());
            ItemRequestDto req2 = new ItemRequestDto(2L, "Desc2", LocalDateTime.now(),
                    Collections.emptySet());
            String expectedBody = toJson(List.of(req2, req1));
            enqueueMockResponse(HttpStatus.OK.value(), expectedBody);

            webTestClient.get().uri(requestsPath).header(HEADER_USER_ID, validUserIdHeader)
                    .exchange().expectStatus().isOk().expectHeader()
                    .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(requestsPath, recordedRequest.getPath(),
                    "Recorded request path should be /requests");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /requests - Bad Request (Missing Header)")
        void getOwnRequests_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(requestsPath).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for missing header should be specific")
                            .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /requests/all - OK (Valid Header)")
        void getAllRequests_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            ItemShortDto itemResp = new ItemShortDto(100L, "Resp", "Desc", true, 3L, 5L);
            ItemRequestDto req = new ItemRequestDto(5L, "Other User Request", LocalDateTime.now(),
                    Set.of(itemResp));
            String expectedBody = toJson(List.of(req));
            enqueueMockResponse(HttpStatus.OK.value(), expectedBody);

            webTestClient.get().uri(requestsAllPath)
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(requestsAllPath, recordedRequest.getPath(),
                    "Recorded request path should be /requests/all");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /requests/all - OK (Valid Header and Pagination)")
        void getAllRequests_whenValidHeaderAndParams_shouldForwardAndReturnOk() throws Exception {
            String from = "5";
            String size = "10";
            String expectedBody = "[]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedBody);

            webTestClient.get().uri(
                            uriBuilder -> uriBuilder.path(requestsAllPath).queryParam("from", from)
                                    .queryParam("size", size).build()).header(HEADER_USER_ID, validUserIdHeader)
                    .exchange().expectStatus().isOk().expectHeader()
                    .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(requestsAllPath + "?from=" + from + "&size=" + size,
                    recordedRequest.getPath(),
                    "Recorded request path should be /requests/all with pagination params");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }


        @Test
        @DisplayName("GET /requests/all - Bad Request (Missing Header)")
        void getAllRequests_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(requestsAllPath).exchange().expectStatus().isBadRequest()
                    .expectBody(ErrorMessage.class).value(error -> assertThat(error.getError()).as(
                                    "Error message for missing header should be specific")
                            .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /requests/{requestId} - OK (Valid Header)")
        void getRequestById_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            ItemRequestDto req = new ItemRequestDto(testRequestId, "Need this", LocalDateTime.now(),
                    Collections.emptySet());
            String expectedBody = toJson(req);
            enqueueMockResponse(HttpStatus.OK.value(), expectedBody);

            webTestClient.get().uri(requestsPath + "/" + testRequestId)
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                    .isEqualTo(expectedBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(requestsPath + "/" + testRequestId, recordedRequest.getPath(),
                    "Recorded request path should be /requests/{requestId}");
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID),
                    "Recorded request should have X-Sharer-User-Id header with correct value");
        }

        @Test
        @DisplayName("GET /requests/{requestId} - Not Found (Backend Error)")
        void getRequestById_whenBackendReturnsNotFound_shouldProxyNotFound() throws Exception {
            long reqId = 999L;
            ErrorMessage backendError = new ErrorMessage("Request not found",
                    HttpStatus.NOT_FOUND.value());
            enqueueMockResponse(HttpStatus.NOT_FOUND.value(), toJson(backendError));

            webTestClient.get().uri(requestsPath + "/" + reqId)
                    .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(ErrorMessage.class).isEqualTo(backendError);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod(),
                    "Recorded request method should be GET");
            assertEquals(requestsPath + "/" + reqId, recordedRequest.getPath(),
                    "Recorded request path should be /requests/{requestId}");
        }


        @Test
        @DisplayName("GET /requests/{requestId} - Bad Request (Missing Header)")
        void getRequestById_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(requestsPath + "/" + testRequestId).exchange().expectStatus()
                    .isBadRequest().expectBody(ErrorMessage.class).value(
                            error -> assertThat(error.getError()).as(
                                            "Error message for missing header should be specific")
                                    .isEqualTo("Required header 'X-Sharer-User-Id' is missing"));
        }
    }
}