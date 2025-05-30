package ru.practicum.shareit.server.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.common.dto.user.UserDto;
import ru.practicum.shareit.server.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Implementation Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;
    private UserDto userDto1;
    private UserDto userDto2;
    private NewUserDto newUserDto;
    private UpdateUserDto updateUserDto;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("User One");
        user1.setEmail("one@example.com");

        user2 = new User();
        user2.setId(2L);
        user2.setName("User Two");
        user2.setEmail("two@example.com");

        userDto1 = new UserDto(1L, "User One", "one@example.com");
        userDto2 = new UserDto(2L, "User Two", "two@example.com");

        newUserDto = new NewUserDto("New User", "new@example.com");
        updateUserDto = new UpdateUserDto("Updated Name", "updated@example.com");
    }

    @Test
    @DisplayName("getAllUsers should return list of users when users exist")
    void getAllUsers_whenUsersExist_shouldReturnUserDtoList() {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.mapToDto(user1)).thenReturn(userDto1);
        when(userMapper.mapToDto(user2)).thenReturn(userDto2);

        List<UserDto> result = userService.getAllUsers();

        assertThat("Result list should not be null", result, is(notNullValue()));
        assertThat("Result list should contain exactly 2 users", result, hasSize(2));
        assertThat("First user in the result list should be the expected userDto1", result.get(0),
                equalTo(userDto1));
        assertThat("Second user in the result list should be the expected userDto2", result.get(1),
                equalTo(userDto2));
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).mapToDto(user1);
        verify(userMapper, times(1)).mapToDto(user2);
    }

    @Test
    @DisplayName("getAllUsers should return empty list when no users exist")
    void getAllUsers_whenNoUsersExist_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAllUsers();

        assertThat("Result list should not be null", result, is(notNullValue()));
        assertThat("Result list should be empty", result, is(empty()));
        verify(userRepository, times(1)).findAll();
        verify(userMapper, never()).mapToDto(any(User.class));
    }

    @Test
    @DisplayName("saveUser should save user and return DTO when email is unique")
    void saveUser_whenEmailIsUnique_shouldSaveAndReturnUserDto() {
        User userToSave = new User();
        userToSave.setName(newUserDto.getName());
        userToSave.setEmail(newUserDto.getEmail());

        User savedUser = new User();
        savedUser.setId(3L);
        savedUser.setName(newUserDto.getName());
        savedUser.setEmail(newUserDto.getEmail());

        UserDto savedUserDto = new UserDto(3L, newUserDto.getName(), newUserDto.getEmail());

        when(userMapper.mapToUser(newUserDto)).thenReturn(userToSave);
        when(userRepository.existsByEmail(newUserDto.getEmail())).thenReturn(false);
        when(userRepository.save(userToSave)).thenReturn(savedUser);
        when(userMapper.mapToDto(savedUser)).thenReturn(savedUserDto);

        UserDto result = userService.saveUser(newUserDto);

        assertThat("Saved user DTO should not be null", result, is(notNullValue()));
        assertThat("Saved user DTO should match the expected DTO", result, equalTo(savedUserDto));
        verify(userMapper, times(1)).mapToUser(newUserDto);
        verify(userRepository, times(1)).existsByEmail(newUserDto.getEmail());
        verify(userRepository, times(1)).save(
                userToSave);
        verify(userMapper, times(1)).mapToDto(savedUser);
    }

    @Test
    @DisplayName("saveUser should throw EmailAlreadyExistsException when email exists")
    void saveUser_whenEmailExists_shouldThrowEmailAlreadyExistsException() {
        User userToSave = new User();
        userToSave.setName(newUserDto.getName());
        userToSave.setEmail(newUserDto.getEmail());

        when(userMapper.mapToUser(newUserDto)).thenReturn(userToSave);
        when(userRepository.existsByEmail(newUserDto.getEmail())).thenReturn(true);
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.saveUser(newUserDto),
                "Should throw EmailAlreadyExistsException when email already exists");

        assertThat("Exception message should contain 'already exists'", exception.getMessage(),
                containsString("already exists"));
        assertThat("Exception message should contain the conflicting email", exception.getMessage(),
                containsString(newUserDto.getEmail()));

        verify(userMapper, times(1)).mapToUser(newUserDto);
        verify(userRepository, times(1)).existsByEmail(newUserDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).mapToDto(any(User.class));
    }

    @Test
    @DisplayName("getById should return UserDto when user exists")
    void getById_whenUserExists_shouldReturnUserDto() {
        Long userId = user1.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(userMapper.mapToDto(user1)).thenReturn(userDto1);

        UserDto result = userService.getById(userId);

        assertThat("Found user DTO should not be null", result, is(notNullValue()));
        assertThat("Found user DTO should match the expected userDto1", result, equalTo(userDto1));
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).mapToDto(user1);
    }

    @Test
    @DisplayName("getById should throw NotFoundException when user does not exist")
    void getById_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getById(userId),
                "Should throw NotFoundException when user is not found");

        assertThat("Exception message should contain 'not found'", exception.getMessage(),
                containsString("not found"));
        assertThat("Exception message should contain the user ID", exception.getMessage(),
                containsString(String.valueOf(userId)));

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).mapToDto(any(User.class));
    }

    @Test
    @DisplayName("update should update user and return DTO when user exists and email is unique")
    void update_whenUserExistsAndEmailUnique_shouldUpdateAndReturnDto() {
        Long userId = user1.getId();
        User existingUser = user1;
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(updateUserDto.getName());
        updatedUser.setEmail(updateUserDto.getEmail());

        UserDto updatedUserResultDto = new UserDto(userId, updateUserDto.getName(),
                updateUserDto.getEmail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(updateUserDto.getEmail())).thenReturn(false);
        when(userMapper.updateUserFields(updateUserDto, existingUser)).thenReturn(updatedUser);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.mapToDto(updatedUser)).thenReturn(updatedUserResultDto);

        UserDto result = userService.update(updateUserDto, userId);

        assertThat("Updated user DTO should not be null", result, is(notNullValue()));
        assertThat("Updated user DTO should match the expected DTO", result,
                equalTo(updatedUserResultDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(updateUserDto.getEmail());
        verify(userMapper, times(1)).updateUserFields(updateUserDto, existingUser);
        verify(userRepository, times(1)).save(updatedUser);
        verify(userMapper, times(1)).mapToDto(updatedUser);
    }

    @Test
    @DisplayName("update should update user when only name is changed")
    void update_whenOnlyNameChanged_shouldUpdateAndReturnDto() {
        Long userId = user1.getId();
        UpdateUserDto nameOnlyUpdateDto = new UpdateUserDto("New Name Only", null);
        User existingUser = user1;
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(nameOnlyUpdateDto.getName());
        updatedUser.setEmail(existingUser.getEmail());

        UserDto resultDto = new UserDto(userId, nameOnlyUpdateDto.getName(),
                existingUser.getEmail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userMapper.updateUserFields(nameOnlyUpdateDto, existingUser)).thenReturn(updatedUser);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.mapToDto(updatedUser)).thenReturn(resultDto);

        UserDto result = userService.update(nameOnlyUpdateDto, userId);

        assertThat("Updated user DTO should match the expected DTO with only name changed", result,
                equalTo(resultDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userMapper, times(1)).updateUserFields(nameOnlyUpdateDto, existingUser);
        verify(userRepository, times(1)).save(updatedUser);
        verify(userMapper, times(1)).mapToDto(updatedUser);
    }

    @Test
    @DisplayName("update should update user when email is unchanged")
    void update_whenEmailIsUnchanged_shouldUpdateAndReturnDto() {
        Long userId = user1.getId();
        UpdateUserDto sameEmailUpdateDto = new UpdateUserDto("Updated Name Again",
                user1.getEmail());
        User existingUser = user1;
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(sameEmailUpdateDto.getName());
        updatedUser.setEmail(sameEmailUpdateDto.getEmail());

        UserDto resultDto = new UserDto(userId, sameEmailUpdateDto.getName(),
                sameEmailUpdateDto.getEmail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userMapper.updateUserFields(sameEmailUpdateDto, existingUser)).thenReturn(updatedUser);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.mapToDto(updatedUser)).thenReturn(resultDto);

        UserDto result = userService.update(sameEmailUpdateDto, userId);

        assertThat("Updated user DTO should match the expected DTO when email is unchanged", result,
                equalTo(resultDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userMapper, times(1)).updateUserFields(sameEmailUpdateDto, existingUser);
        verify(userRepository, times(1)).save(updatedUser);
        verify(userMapper, times(1)).mapToDto(updatedUser);
    }

    @Test
    @DisplayName("update should throw EmailAlreadyExistsException when new email already exists")
    void update_whenNewEmailExists_shouldThrowEmailAlreadyExistsException() {
        Long userId = user1.getId();
        User existingUser = user1;
        UpdateUserDto conflictingEmailDto = new UpdateUserDto("Name Change", "two@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(conflictingEmailDto.getEmail())).thenReturn(true);
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.update(conflictingEmailDto, userId),
                "Should throw EmailAlreadyExistsException when new email already exists");

        assertThat("Exception message should contain 'already exists'", exception.getMessage(),
                containsString("already exists"));
        assertThat("Exception message should contain the conflicting email", exception.getMessage(),
                containsString(conflictingEmailDto.getEmail()));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(conflictingEmailDto.getEmail());
        verify(userMapper, never()).updateUserFields(any(), any());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).mapToDto(any(User.class));
    }

    @Test
    @DisplayName("update should throw NotFoundException when user does not exist")
    void update_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.update(updateUserDto, userId),
                "Should throw NotFoundException when user is not found");

        assertThat("Exception message should contain 'not found'", exception.getMessage(),
                containsString("not found"));
        assertThat("Exception message should contain the user ID", exception.getMessage(),
                containsString(String.valueOf(userId)));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userMapper, never()).updateUserFields(any(), any());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).mapToDto(any(User.class));
    }

    @Test
    @DisplayName("delete should call repository deleteById")
    void delete_whenCalled_shouldCallRepositoryDeleteById() {
        Long userId = user1.getId();

        assertDoesNotThrow(() -> userService.delete(userId),
                "Should not throw an exception when deleting a user");

        verify(userRepository, times(1)).deleteById(userId);
    }
}