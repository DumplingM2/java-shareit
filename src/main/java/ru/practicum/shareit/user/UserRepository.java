package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("unused")
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

}