package ru.practicum.shareit.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewItemRequestDto {
    @NotBlank(message = "Request description cannot be blank")
    private String description;
}