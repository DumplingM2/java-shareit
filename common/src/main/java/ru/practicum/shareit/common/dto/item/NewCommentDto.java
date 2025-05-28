package ru.practicum.shareit.common.dto.item;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewCommentDto {
    @NotBlank(message = "Comment text cannot be blank")
    private String text;
}
