package org.kucherenkoos.carsharingservice.mapper;

import org.kucherenkoos.carsharingservice.dto.user.UserRegistrationRequestDto;
import org.kucherenkoos.carsharingservice.dto.user.UserResponseDto;
import org.kucherenkoos.carsharingservice.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(UserRegistrationRequestDto dto);

    UserResponseDto toDto(User user);
}
