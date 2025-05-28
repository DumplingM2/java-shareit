package ru.practicum.shareit.server.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.common.dto.user.UserDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
@SuppressWarnings("unused")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Processing request to fetch all users...");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserDto> saveUser(@RequestBody NewUserDto userDto) {
        log.info("Processing request to save a new user...");
        UserDto savedUser = userService.saveUser(userDto);
        return ResponseEntity.created(java.net.URI.create("/users/" + savedUser.getId()))
                .body(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        log.info("Processing request to fetch user by ID: {}", id);
        return ResponseEntity.ok(userService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(@RequestBody UpdateUserDto updatedUserDto,
                                          @PathVariable Long id) {
        log.info("Processing request to update user with ID: {}", id);
        return ResponseEntity.ok(userService.update(updatedUserDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Processing request to delete user with ID: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}