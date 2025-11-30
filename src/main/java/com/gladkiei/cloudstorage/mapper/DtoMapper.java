package com.gladkiei.cloudstorage.mapper;

import com.gladkiei.cloudstorage.dto.AuthRequestDto;
import com.gladkiei.cloudstorage.dto.AuthResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DtoMapper {
    DtoMapper INSTANCE = Mappers.getMapper(DtoMapper.class);

    AuthResponseDto requestDtoToResponseDto(AuthRequestDto authRequestDto);
}
