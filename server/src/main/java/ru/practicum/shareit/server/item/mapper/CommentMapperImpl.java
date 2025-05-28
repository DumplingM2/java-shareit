package ru.practicum.shareit.server.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.server.item.Comment;

@Component
@SuppressWarnings("unused")
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentDto mapToDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getItem().getId(),
                comment.getAuthor().getName(), comment.getCreated().toString());
    }

    @Override
    public Comment mapToComment(NewCommentDto newCommentDto) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        return comment;
    }
}
