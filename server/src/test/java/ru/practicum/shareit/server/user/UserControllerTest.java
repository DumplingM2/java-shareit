package ru.practicum.shareit.server.user;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.common.dto.user.UserDto;
import ru.practicum.shareit.server.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.server.exception.NotFoundException;


@WebMvcTest(UserController.class)
@DisplayName("User Controller WebMvc Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserDto userDto1;
    private UserDto userDto2;
    private NewUserDto newUserDto;
    private UpdateUserDto updateUserDto;

    private final Long userId1 = 1L;
    private final Long userId2 = 2L;
    private final Long nonExistentUserId = 99L;


    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(userId1, "User One", "one@example.com");
        userDto2 = new UserDto(userId2, "User Two", "two@example.com");
        newUserDto = new NewUserDto("New User", "new@example.com");
        updateUserDto = new UpdateUserDto("Updated Name", "updated@example.com");
    }

    @Test
    @DisplayName("GET /users - Success (Multiple Users)")
    void getAllUsers_whenUsersExist_shouldReturnOkAndUserList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDto1, userDto2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(userId1.intValue())))
                .andExpect(jsonPath("$[0].name", is(userDto1.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto1.getEmail())))
                .andExpect(jsonPath("$[1].id", is(userId2.intValue())))
                .andExpect(jsonPath("$[1].name", is(userDto2.getName())))
                .andExpect(jsonPath("$[1].email", is(userDto2.getEmail())));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("GET /users - Success (No Users)")
    void getAllUsers_whenNoUsersExist_shouldReturnOkAndEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("POST /users - Success")
    void saveUser_whenValidDto_shouldReturnCreatedAndUserDto() throws Exception {
        UserDto savedUserDto = new UserDto(userId1, newUserDto.getName(), newUserDto.getEmail());
        when(userService.saveUser(any(NewUserDto.class))).thenReturn(savedUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + userId1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(userId1.intValue())))
                .andExpect(jsonPath("$.name", is(newUserDto.getName())))
                .andExpect(jsonPath("$.email", is(newUserDto.getEmail())));

        verify(userService, times(1)).saveUser(refEq(newUserDto));
    }

    @Test
    @DisplayName("POST /users - Failure (Email Exists)")
    void saveUser_whenEmailExists_shouldReturnConflict() throws Exception {
        String errorMessage = "Email already exists!";
        when(userService.saveUser(any(NewUserDto.class)))
                .thenThrow(new EmailAlreadyExistsException(errorMessage));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMessage)))
                .andExpect(jsonPath("$.responseCode", is(409)));

        verify(userService, times(1)).saveUser(refEq(newUserDto));
    }

    @Test
    @DisplayName("GET /users/{id} - Success")
    void getById_whenUserExists_shouldReturnOkAndUserDto() throws Exception {
        when(userService.getById(userId1)).thenReturn(userDto1);

        mockMvc.perform(get("/users/{id}", userId1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(userId1.intValue())))
                .andExpect(jsonPath("$.name", is(userDto1.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));

        verify(userService, times(1)).getById(userId1);
    }

    @Test
    @DisplayName("GET /users/{id} - Failure (Not Found)")
    void getById_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMessage = "User not found!";
        when(userService.getById(nonExistentUserId))
                .thenThrow(new NotFoundException(errorMessage));

        mockMvc.perform(get("/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMessage)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(userService, times(1)).getById(nonExistentUserId);
    }

    @Test
    @DisplayName("PATCH /users/{id} - Success")
    void update_whenValidRequest_shouldReturnOkAndUpdatedUserDto() throws Exception {
        UserDto updatedResultDto = new UserDto(userId1, updateUserDto.getName(),
                updateUserDto.getEmail());
        when(userService.update(any(UpdateUserDto.class), eq(userId1))).thenReturn(
                updatedResultDto);

        mockMvc.perform(patch("/users/{id}", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(userId1.intValue())))
                .andExpect(jsonPath("$.name", is(updateUserDto.getName())))
                .andExpect(jsonPath("$.email", is(updateUserDto.getEmail())));

        verify(userService, times(1)).update(refEq(updateUserDto), eq(userId1));
    }

    @Test
    @DisplayName("PATCH /users/{id} - Failure (User Not Found)")
    void update_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMessage = "Cannot update non-existent user";
        when(userService.update(any(UpdateUserDto.class), eq(nonExistentUserId)))
                .thenThrow(new NotFoundException(errorMessage));

        mockMvc.perform(patch("/users/{id}", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMessage)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(userService, times(1)).update(refEq(updateUserDto), eq(nonExistentUserId));
    }

    @Test
    @DisplayName("PATCH /users/{id} - Failure (Email Exists)")
    void update_whenEmailExists_shouldReturnConflict() throws Exception {
        String errorMessage = "Updated email already taken";
        when(userService.update(any(UpdateUserDto.class), eq(userId1)))
                .thenThrow(new EmailAlreadyExistsException(errorMessage));

        mockMvc.perform(patch("/users/{id}", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMessage)))
                .andExpect(jsonPath("$.responseCode", is(409)));

        verify(userService, times(1)).update(refEq(updateUserDto), eq(userId1));
    }

    @Test
    @DisplayName("DELETE /users/{id} - Success")
    void delete_whenUserExists_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).delete(userId1);

        mockMvc.perform(delete("/users/{id}", userId1))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(userId1);
    }
}