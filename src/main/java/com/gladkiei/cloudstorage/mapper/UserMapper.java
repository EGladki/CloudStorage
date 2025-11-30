package com.gladkiei.cloudstorage.mapper;

import com.gladkiei.cloudstorage.dto.AuthRequestDto;
import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.models.User;
import com.gladkiei.cloudstorage.security.UserDetailsImpl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    User registerRequesDtoToUser(AuthRequestDto dto);

    UserResponseDto userToUserResponseDto(User user);

    @Mapping(target = "id", source = "user.id" )
    UserResponseDto principalToUserResponseDto(UserDetailsImpl principal);
}
