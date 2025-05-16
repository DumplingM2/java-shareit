package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Comment;

@Component
@SuppressWarnings("unused")
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentDto mapToDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getItem().getId(),
                comment.getAuthor().getName(), comment.getCreatedAt().toString());
    }

    @Override
    public Comment mapToComment(NewCommentDto newCommentDto) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        return comment;
    }
}