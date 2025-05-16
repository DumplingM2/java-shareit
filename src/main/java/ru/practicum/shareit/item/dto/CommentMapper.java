package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Comment;

public interface CommentMapper {

    CommentDto mapToDto(Comment comment);

    Comment mapToComment(NewCommentDto newCommentDto);

}