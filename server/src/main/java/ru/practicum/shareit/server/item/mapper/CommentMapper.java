package ru.practicum.shareit.server.item.mapper;

import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.server.item.Comment;

public interface CommentMapper {

    CommentDto mapToDto(Comment comment);

    Comment mapToComment(NewCommentDto newCommentDto);

}
