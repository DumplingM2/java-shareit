package ru.practicum.shareit.server.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unused")
public interface CommentRepository extends JpaRepository<Comment, Long> {

}
