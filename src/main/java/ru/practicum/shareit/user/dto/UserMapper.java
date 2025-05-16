package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

public interface UserMapper {

    UserDto mapToDto(User user);

    User mapToUser(NewUserDto userDto);

    User updateUserFields(UpdateUserDto userDto, User user);
}