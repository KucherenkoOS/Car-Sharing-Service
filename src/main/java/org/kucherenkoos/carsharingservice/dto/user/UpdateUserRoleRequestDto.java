package org.kucherenkoos.carsharingservice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Update role for user (Manager operation")
public class UpdateUserRoleRequestDto {
    @Schema(description =
            "Role name, not case-sensitive because all will be in UpperCase", example = "manager")
    @NotBlank(message = "Role cannot be empty")
    private String role;
}
