package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("unused")
public interface CommentRepository extends JpaRepository<Comment, Long> {

}