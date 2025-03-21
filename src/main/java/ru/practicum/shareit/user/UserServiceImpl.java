package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final InMemoryUserRepository userRepository;

    public UserServiceImpl(InMemoryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        // Проверка, что email не занят
        if (user.getEmail() != null && emailExists(user.getEmail())) {
            throw new ConflictException("Email already in use: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String newEmail = user.getEmail();
        if (newEmail != null && !newEmail.equals(existing.getEmail()) && emailExists(newEmail)) {
            throw new ConflictException("Email already in use: " + newEmail);
        }

        if (user.getName() != null) {
            existing.setName(user.getName());
        }
        if (newEmail != null) {
            existing.setEmail(newEmail);
        }
        return userRepository.save(existing);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Дополнительный метод для проверки дубликата email
    private boolean emailExists(String email) {
        return userRepository.findAll().stream()
                .anyMatch(u -> email.equals(u.getEmail()));
    }
}
