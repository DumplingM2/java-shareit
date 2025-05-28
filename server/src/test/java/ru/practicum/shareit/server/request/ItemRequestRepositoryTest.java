package ru.practicum.shareit.server.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.user.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("ItemRequest Repository DataJpa Tests")
class ItemRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16"));

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }

    private User requestor1;
    private User requestor2;
    private User itemOwner;

    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;
    private ItemRequest request4NoItems;

    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() throws InterruptedException {
        requestor1 = new User();
        requestor1.setName("Requestor One");
        requestor1.setEmail("req1@example.com");
        requestor1 = entityManager.persistAndFlush(requestor1);

        requestor2 = new User();
        requestor2.setName("Requestor Two");
        requestor2.setEmail("req2@example.com");
        requestor2 = entityManager.persistAndFlush(requestor2);

        itemOwner = new User();
        itemOwner.setName("Item Owner");
        itemOwner.setEmail("owner@example.com");
        itemOwner = entityManager.persistAndFlush(itemOwner);

        // no auditing here so creation timestamp *might* not be set automatically
        LocalDateTime time1 = LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime time2 = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime time3 = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime time4 = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        request1 = new ItemRequest();
        request1.setDescription("Need hammer");
        request1.setRequestor(requestor1);
        request1.setCreated(time1);
        request1 = entityManager.persistAndFlush(request1);

        Thread.sleep(1);

        request2 = new ItemRequest();
        request2.setDescription("Need screwdriver set");
        request2.setRequestor(requestor1);
        request2.setCreated(time2);
        request2 = entityManager.persistAndFlush(request2);

        Thread.sleep(1);

        request3 = new ItemRequest();
        request3.setDescription("Need ladder");
        request3.setRequestor(requestor2);
        request3.setCreated(time3);
        request3 = entityManager.persistAndFlush(request3);

        Thread.sleep(1);

        request4NoItems = new ItemRequest();
        request4NoItems.setDescription("Need paint");
        request4NoItems.setRequestor(requestor1);
        request4NoItems.setCreated(time4);
        request4NoItems = entityManager.persistAndFlush(request4NoItems);

        item1 = new Item();
        item1.setName("Hammer Response");
        item1.setDescription("Good hammer");
        item1.setAvailable(true);
        item1.setOwner(itemOwner);
        item1.setRequest(request1);
        item1 = entityManager.persistAndFlush(item1);

        item2 = new Item();
        item2.setName("Screwdriver Response 1");
        item2.setDescription("Phillips head");
        item2.setAvailable(true);
        item2.setOwner(itemOwner);
        item2.setRequest(request2);
        item2 = entityManager.persistAndFlush(item2);

        item3 = new Item();
        item3.setName("Screwdriver Response 2");
        item3.setDescription("Flat head");
        item3.setAvailable(true);
        item3.setOwner(requestor2);
        item3.setRequest(request2);
        item3 = entityManager.persistAndFlush(item3);

        entityManager.clear();
    }

    @Test
    @DisplayName("findByRequestorIdOrderByCreatedDesc should return user's requests ordered by "
            + "creation desc")
    void findByRequestorId_shouldReturnOrderedRequests() {
        List<ItemRequest> results = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(
                requestor1.getId());

        assertThat("Should return exactly 3 requests for requestor1", results, hasSize(3));
        assertThat("First request should be the newest (request4NoItems)", results.get(0),
                equalTo(request4NoItems));
        assertThat("Second request should be request2", results.get(1), equalTo(request2));
        assertThat("Third request should be the oldest (request1)", results.get(2),
                equalTo(request1));
        assertThat("Request4NoItems should have an empty list of items", results.get(0).getItems(),
                is(empty()));
        assertThat("Request2 should have 2 items", results.get(1).getItems(), hasSize(2));
        assertThat("Request1 should have 1 item", results.get(2).getItems(), hasSize(1));
    }

    @Test
    @DisplayName("findByRequestorIdOrderByCreatedDesc should return empty list for user with no "
            + "requests")
    void findByRequestorId_whenUserHasNoRequests_shouldReturnEmptyList() {
        User requestor3 = new User();
        requestor3.setName("Requestor Three");
        requestor3.setEmail("req3@example.com");
        requestor3 = entityManager.persistAndFlush(requestor3);

        List<ItemRequest> results = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(
                requestor3.getId());

        assertThat("Should return an empty list for a user with no requests", results, is(empty()));
    }

    @Test
    @DisplayName("findByRequestorIdOrderByCreatedDesc should return empty list for non-existent "
            + "user")
    void findByRequestorId_whenUserDoesNotExist_shouldReturnEmptyList() {
        Long nonExistentUserId = 999L;

        List<ItemRequest> results = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(
                nonExistentUserId);

        assertThat("Should return an empty list for a non-existent user ID", results, is(empty()));
    }

    @Test
    @DisplayName("findAllByRequestorIdNot should return requests not by the specified user")
    void findAllByRequestorIdNot_shouldExcludeUserRequests() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());

        Page<ItemRequest> results = itemRequestRepository.findAllByRequestorIdNot(
                requestor1.getId(), pageable);

        assertThat("Page content should contain requests not made by requestor1",
                results.getContent(), hasSize(1));
        assertThat("The returned request should be request3 (made by requestor2)",
                results.getContent().getFirst(), equalTo(request3));
        assertThat("Request3 should have an empty list of items by default in this query",
                results.getContent().getFirst().getItems(), is(empty()));
        assertThat("Total elements should be 1 (only request3)", results.getTotalElements(),
                equalTo(1L));
    }

    @Test
    @DisplayName("findAllByRequestorIdNot should return empty page when all requests are by the user")
    void findAllByRequestorIdNot_whenAllRequestsByUser_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());
        entityManager.remove(entityManager.find(ItemRequest.class, request3.getId()));
        entityManager.flush();
        entityManager.clear();

        Page<ItemRequest> results = itemRequestRepository.findAllByRequestorIdNot(
                requestor1.getId(), pageable);

        assertThat("Page content should be empty when no requests are made by other users",
                results.getContent(), is(empty()));
        assertThat("Total elements should be 0 when no requests are made by other users",
                results.getTotalElements(), equalTo(0L));

    }

    @Test
    @DisplayName("findAllByRequestorIdNot should respect pagination and sort order")
    void findAllByRequestorIdNot_shouldApplyPaginationAndSort() {
        LocalDateTime olderTime = LocalDateTime.now().minusDays(10);
        ItemRequest request0 = new ItemRequest();
        request0.setDescription("Old request by req2");
        request0.setRequestor(requestor2);
        request0.setCreated(olderTime);
        entityManager.persistAndFlush(request0);
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 1, Sort.by("created").descending());
        Page<ItemRequest> resultsPage0 = itemRequestRepository.findAllByRequestorIdNot(
                requestor1.getId(), pageable);
        assertThat("First page should contain 1 element", resultsPage0.getContent(), hasSize(1));
        assertThat("First element should be the newest request by another user (request3)",
                resultsPage0.getContent().getFirst(), equalTo(request3));
        assertThat("Total elements across all pages should be 2", resultsPage0.getTotalElements(),
                equalTo(2L));
        assertThat("Total pages should be 2 for 2 elements with page size 1",
                resultsPage0.getTotalPages(), equalTo(2));
        pageable = PageRequest.of(1, 1, Sort.by("created").descending());
        Page<ItemRequest> resultsPage1 = itemRequestRepository.findAllByRequestorIdNot(
                requestor1.getId(), pageable);
        assertThat("Second page should contain 1 element", resultsPage1.getContent(), hasSize(1));
        assertThat("Second element should be the older request by another user (request0)",
                resultsPage1.getContent().getFirst().getDescription(), equalTo("Old request by req2"));
        assertThat("Total elements across all pages should be 2", resultsPage1.getTotalElements(),
                equalTo(2L));
        assertThat("Total pages should be 2 for 2 elements with page size 1",
                resultsPage1.getTotalPages(), equalTo(2));
    }

    @Test
    @DisplayName("findByIdFetchingItems should return request with items fetched")
    void findByIdFetchingItems_whenRequestExistsWithItems_shouldReturnOptionalWithFetchedItems() {
        Optional<ItemRequest> resultOpt = itemRequestRepository.findByIdFetchingItems(
                request2.getId());

        assertTrue(resultOpt.isPresent(), "Optional should contain a value when request exists");
        ItemRequest result = resultOpt.get();
        assertThat("Returned request should be request2", result, equalTo(request2));
        assertDoesNotThrow(() -> result.getItems().size(),
                "Accessing items should not throw LazyInitializationException");
        assertThat("Items list should contain the items associated with request2",
                result.getItems(), hasSize(2));
        assertThat("Items list should contain item2 and item3 in any order", result.getItems(),
                containsInAnyOrder(item2, item3));
    }

    @Test
    @DisplayName("findByIdFetchingItems should return request with empty items fetched")
    void findByIdFetchingItems_whenRequestExistsWithoutItems_shouldReturnOptionalWithEmptyFetchedItems() {
        Optional<ItemRequest> resultOpt = itemRequestRepository.findByIdFetchingItems(
                request4NoItems.getId());

        assertTrue(resultOpt.isPresent(), "Optional should contain a value when request exists");
        ItemRequest result = resultOpt.get();
        assertThat("Returned request should be request4NoItems", result, equalTo(request4NoItems));
        assertDoesNotThrow(() -> result.getItems().size(),
                "Accessing items should not throw LazyInitializationException");
        assertThat("Items list should be empty for a request with no associated items",
                result.getItems(), is(empty()));
    }

    @Test
    @DisplayName("findByIdFetchingItems should return empty optional when request not found")
    void findByIdFetchingItems_whenRequestDoesNotExist_shouldReturnEmptyOptional() {
        Long nonExistentId = 999L;

        Optional<ItemRequest> resultOpt = itemRequestRepository.findByIdFetchingItems(nonExistentId);

        assertTrue(resultOpt.isEmpty(), "Optional should be empty when request is not found by ID");
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null description")
    void save_whenNullDescription_shouldThrowException() {
        ItemRequest badRequest = new ItemRequest();
        badRequest.setDescription(null);
        badRequest.setRequestor(requestor1);
        badRequest.setCreated(LocalDateTime.now());
        assertThrows(DataIntegrityViolationException.class, () -> {
            itemRequestRepository.save(badRequest);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving item request with null "
                + "description");
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null requestor")
    void save_whenNullRequestor_shouldThrowException() {
        ItemRequest badRequest = new ItemRequest();
        badRequest.setDescription("Valid desc");
        badRequest.setRequestor(null);
        badRequest.setCreated(LocalDateTime.now());
        assertThrows(DataIntegrityViolationException.class, () -> {
            itemRequestRepository.save(badRequest);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving item request with null "
                + "requestor");
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null created")
    void save_whenNullCreated_shouldThrowException() {
        ItemRequest badRequest = new ItemRequest();
        badRequest.setDescription("Valid desc");
        badRequest.setRequestor(requestor1);
        badRequest.setCreated(null);
        assertThrows(DataIntegrityViolationException.class, () -> {
            itemRequestRepository.save(badRequest);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving item request with null "
                + "created timestamp");
    }
}