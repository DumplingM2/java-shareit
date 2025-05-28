package ru.practicum.shareit.server.user.mapper;

import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.common.dto.user.UserDto;
import ru.practicum.shareit.server.user.User;

public interface UserMapper {

    UserDto mapToDto(User user);

    User mapToUser(NewUserDto userDto);

    User updateUserFields(UpdateUserDto userDto, User user);
}
