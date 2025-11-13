package com.gladkiei.cloudstorage.mapper;

import com.gladkiei.cloudstorage.dto.RegisterRequestDto;
import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    User registerRequesDtoToUser(RegisterRequestDto dto);

    UserResponseDto userToUserResponseDto(User user);
}
