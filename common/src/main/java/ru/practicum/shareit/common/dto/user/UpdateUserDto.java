package ru.practicum.shareit.common.dto.user;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserDto {

    private String name;

    @Email(message = "Invalid email format")
    private String email;
}