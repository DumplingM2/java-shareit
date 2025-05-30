package ru.practicum.shareit.server.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.PostgreSQLContainer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("User Repository DataJpa Tests")
@Testcontainers
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16"));

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("User One");
        user1.setEmail("user.one@example.com");

        user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user.two@example.com");
    }

    @Test
    @DisplayName("existsByEmail should return true when email exists")
    void existsByEmail_whenEmailExists_shouldReturnTrue() {
        entityManager.persist(user1);
        entityManager.flush();
        entityManager.detach(user1);

        boolean exists = userRepository.existsByEmail(user1.getEmail());

        assertTrue(exists, "existsByEmail should return true for an existing email");
    }

    @Test
    @DisplayName("existsByEmail should return false when email does not exist")
    void existsByEmail_whenEmailDoesNotExist_shouldReturnFalse() {
        entityManager.persist(user1);
        entityManager.flush();
        entityManager.detach(user1);

        String nonExistentEmail = "non.existent@example.com";

        boolean exists = userRepository.existsByEmail(nonExistentEmail);

        assertFalse(exists, "existsByEmail should return false for a non-existent email");
    }

    @Test
    @DisplayName("existsByEmail should return false when repository is empty")
    void existsByEmail_whenRepositoryIsEmpty_shouldReturnFalse() {

        boolean exists = userRepository.existsByEmail("any@example.com");

        assertFalse(exists, "existsByEmail should return false when the repository is empty");
    }

    @Test
    @DisplayName("save should persist user with unique email")
    void save_whenEmailIsUnique_shouldPersistUser() {

        User savedUser = userRepository.save(user1);
        entityManager.flush();

        assertNotNull(savedUser.getId(), "Saved user should have an ID assigned after saving");
        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertNotNull(foundUser, "User should be found in the database after saving");
        assertThat("Saved user's email should match the original email", foundUser.getEmail(),
                equalTo(user1.getEmail()));
        assertThat("Saved user's name should match the original name", foundUser.getName(),
                equalTo(user1.getName()));
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for duplicate email")
    void save_whenDuplicateEmail_shouldThrowException() {
        entityManager.persistAndFlush(user1);
        entityManager.detach(user1);

        User duplicateUser = new User();
        duplicateUser.setName("Duplicate User");
        duplicateUser.setEmail(user1.getEmail());

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(duplicateUser);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException for duplicate email");
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null email")
    void save_whenNullEmail_shouldThrowException() {
        User userWithNullEmail = new User();
        userWithNullEmail.setName("Null Email User");
        userWithNullEmail.setEmail(null);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(userWithNullEmail);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving user with null email");
    }

    @Test
    @DisplayName("save should throw DataIntegrityViolationException for null name")
    void save_whenNullName_shouldThrowException() {
        User userWithNullName = new User();
        userWithNullName.setName(null);
        userWithNullName.setEmail("null.name@example.com");

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(userWithNullName);
            entityManager.flush();
        }, "Should throw DataIntegrityViolationException when saving user with null name");
    }

    @Test
    @DisplayName("findById should retrieve persisted user")
    void findById_whenUserExists_shouldReturnUser() {
        User persistedUser = entityManager.persistAndFlush(user1);

        Optional<User> foundUserOpt = userRepository.findById(persistedUser.getId());

        assertTrue(foundUserOpt.isPresent(),
                "Optional should contain a user when found by existing ID");
        assertThat("Found user's ID should match the persisted user's ID",
                foundUserOpt.get().getId(), equalTo(persistedUser.getId()));
        assertThat("Found user's email should match the persisted user's email",
                foundUserOpt.get().getEmail(), equalTo(user1.getEmail()));
    }

    @Test
    @DisplayName("findById should return empty optional when user does not exist")
    void findById_whenUserDoesNotExist_shouldReturnEmpty() {
        Long nonExistentId = 999L;

        Optional<User> foundUserOpt = userRepository.findById(nonExistentId);

        assertTrue(foundUserOpt.isEmpty(), "Optional should be empty when user is not found by ID");
    }
}