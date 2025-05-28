package ru.practicum.shareit.server.item;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.practicum.shareit.server.user.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("Item Repository DataJpa Tests")
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16"));

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }

    private User owner1;
    private User owner2;
    private Item item1Owner1;
    private Item item2Owner1;
    private Item item3Owner2;
    private Item item4Owner1Unavailable;

    @BeforeEach
    void setUp() {
        owner1 = new User();
        owner1.setName("Owner One");
        owner1.setEmail("owner1@example.com");
        owner1 = entityManager.persistAndFlush(owner1);

        owner2 = new User();
        owner2.setName("Owner Two");
        owner2.setEmail("owner2@example.com");
        owner2 = entityManager.persistAndFlush(owner2);

        item1Owner1 = new Item();
        item1Owner1.setName("Drill");
        item1Owner1.setDescription("Powerful cordless drill");
        item1Owner1.setAvailable(true);
        item1Owner1.setOwner(owner1);

        item2Owner1 = new Item();
        item2Owner1.setName("Ladder");
        item2Owner1.setDescription("Sturdy aluminum ladder");
        item2Owner1.setAvailable(true);
        item2Owner1.setOwner(owner1);

        item3Owner2 = new Item();
        item3Owner2.setName("Screwdriver Set");
        item3Owner2.setDescription("Various types of screwdrivers");
        item3Owner2.setAvailable(true);
        item3Owner2.setOwner(owner2);

        item4Owner1Unavailable = new Item();
        item4Owner1Unavailable.setName("Paint Sprayer");
        item4Owner1Unavailable.setDescription("Professional paint sprayer (currently broken)");
        item4Owner1Unavailable.setAvailable(false);
        item4Owner1Unavailable.setOwner(owner1);

        entityManager.persist(item1Owner1);
        entityManager.persist(item2Owner1);
        entityManager.persist(item3Owner2);
        entityManager.persist(item4Owner1Unavailable);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByOwnerId should return all items for a given owner")
    void findByOwnerId_whenOwnerHasMultipleItems_shouldReturnAllItems() {
        List<Item> foundItems = itemRepository.findByOwnerId(owner1.getId());

        assertThat("Should return 3 items for owner1", foundItems, hasSize(3));
        assertThat("Should contain the specific items belonging to owner1", foundItems,
                containsInAnyOrder(item1Owner1, item2Owner1, item4Owner1Unavailable));
        assertTrue(
                foundItems.stream().allMatch(item -> item.getOwner().getId().equals(owner1.getId())),
                "All returned items should belong to owner1");
    }

    @Test
    @DisplayName("findByOwnerId should return only items for the specific owner")
    void findByOwnerId_whenMultipleOwnersExist_shouldReturnOnlyOwner1Items() {
        List<Item> foundItems = itemRepository.findByOwnerId(owner1.getId());

        assertThat("Should return 3 items for owner1 even with other owners present", foundItems,
                hasSize(3));
        assertThat("Should contain the specific items belonging to owner1", foundItems,
                containsInAnyOrder(item1Owner1, item2Owner1, item4Owner1Unavailable));
        assertFalse(foundItems.contains(item3Owner2),
                "Should not contain items belonging to other owners (owner2's item)");
    }


    @Test
    @DisplayName("findByOwnerId should return an empty list for owner with no items")
    void findByOwnerId_whenOwnerHasNoItems_shouldReturnEmptyList() {
        User owner3 = new User();
        owner3.setName("Owner Three");
        owner3.setEmail("owner3@example.com");
        owner3 = entityManager.persistAndFlush(owner3);

        List<Item> foundItems = itemRepository.findByOwnerId(owner3.getId());

        assertThat("Should return an empty list for an owner with no items", foundItems,
                is(empty()));
    }

    @Test
    @DisplayName("findByOwnerId should return an empty list for non-existent owner ID")
    void findByOwnerId_whenOwnerIdDoesNotExist_shouldReturnEmptyList() {
        long nonExistentOwnerId = 999L;

        List<Item> foundItems = itemRepository.findByOwnerId(nonExistentOwnerId);

        assertThat("Should return an empty list for a non-existent owner ID", foundItems,
                is(empty()));
    }

    @Test
    @DisplayName("search should find available items matching name (case-insensitive)")
    void search_whenTextMatchesName_shouldReturnAvailableItems() {
        List<Item> foundItems = itemRepository.search("dRilL");

        assertThat("Should return exactly 1 item matching the name", foundItems, hasSize(1));
        assertThat("The found item should be item1Owner1", foundItems.getFirst(),
                equalTo(item1Owner1));
    }

    @Test
    @DisplayName("search should find available items matching description (case-insensitive)")
    void search_whenTextMatchesDescription_shouldReturnAvailableItems() {
        List<Item> foundItems = itemRepository.search("aLuMinUm");

        assertThat("Should return exactly 1 item matching the description", foundItems, hasSize(1));
        assertThat("The found item should be item2Owner1", foundItems.getFirst(),
                equalTo(item2Owner1));
    }

    @Test
    @DisplayName("search should find available items matching partial name/description")
    void search_whenTextMatchesPartial_shouldReturnAvailableItems() {
        List<Item> foundItems = itemRepository.search("drive");

        assertThat("Should return exactly 1 item matching the partial text 'drive'", foundItems,
                hasSize(1));
        assertThat("The found item should be item3Owner2", foundItems.getFirst(),
                equalTo(item3Owner2));
    }


    @Test
    @DisplayName("search should find multiple available items matching text")
    void search_whenTextMatchesMultiple_shouldReturnAllMatchingAvailableItems() {
        List<Item> foundItems = itemRepository.search("er");

        assertThat("Should return all 3 available items matching 'er'", foundItems, hasSize(3));
        assertThat("Should contain item1Owner1, item2Owner1, and item3Owner2", foundItems,
                containsInAnyOrder(item1Owner1, item2Owner1, item3Owner2));
    }

    @Test
    @DisplayName("search should NOT find unavailable items matching text")
    void search_whenTextMatchesUnavailableItem_shouldNotReturnIt() {
        List<Item> foundItems = itemRepository.search("sPraYer");

        assertThat("Should return an empty list when text only matches an unavailable item",
                foundItems, is(empty()));
    }


    @Test
    @DisplayName("search should return empty list when text matches nothing")
    void search_whenTextMatchesNothing_shouldReturnEmptyList() {
        List<Item> foundItems = itemRepository.search("nonexistentkeyword");

        assertThat("Should return an empty list when text matches nothing", foundItems,
                is(empty()));
    }

    @Test
    @DisplayName("search should return all available items when text is empty")
    void search_whenTextIsEmpty_shouldReturnAllAvailableItems() {
        List<Item> foundItems = itemRepository.search("");

        assertThat("Should return all available items when text is empty", foundItems, hasSize(3));
        assertThat("Should contain item1Owner1, item2Owner1, and item3Owner2 when text is empty",
                foundItems, containsInAnyOrder(item1Owner1, item2Owner1, item3Owner2));
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null name")
    void save_whenNullName_shouldThrowException() {
        Item itemWithNullName = new Item();
        itemWithNullName.setOwner(owner1);
        itemWithNullName.setDescription("Desc");
        itemWithNullName.setAvailable(true);
        itemWithNullName.setName(null);

        assertThrows(DataIntegrityViolationException.class, () -> {
            itemRepository.save(itemWithNullName);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving item with null name");
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null description")
    void save_whenNullDescription_shouldThrowException() {
        Item itemWithNullDesc = new Item();
        itemWithNullDesc.setOwner(owner1);
        itemWithNullDesc.setName("Name");
        itemWithNullDesc.setAvailable(true);
        itemWithNullDesc.setDescription(null);

        assertThrows(DataIntegrityViolationException.class, () -> {
            itemRepository.save(itemWithNullDesc);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving item with null description");
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null availability")
    void save_whenNullAvailability_shouldThrowException() {
        Item itemWithNullAvail = new Item();
        itemWithNullAvail.setOwner(owner1);
        itemWithNullAvail.setName("Name");
        itemWithNullAvail.setDescription("Desc");
        itemWithNullAvail.setAvailable(null);

        assertThrows(DataIntegrityViolationException.class, () -> {
            itemRepository.save(itemWithNullAvail);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving item with null availability");
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null owner")
    void save_whenNullOwner_shouldThrowException() {
        Item itemWithNullOwner = new Item();
        itemWithNullOwner.setName("Name");
        itemWithNullOwner.setDescription("Desc");
        itemWithNullOwner.setAvailable(true);
        itemWithNullOwner.setOwner(null);

        assertThrows(DataIntegrityViolationException.class, () -> {
            itemRepository.save(itemWithNullOwner);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving item with null owner");
    }
}