package org.kucherenkoos.carsharingservice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Update DTO for user profile")
public class UpdateUserProfileDto {

    @Schema(description = "First name", example = "Kevin")
    @Size(min = 2, max = 50)
    private String firstName;

    @Schema(description = "Last name", example = "Estre")
    @Size(min = 2, max = 50)
    private String lastName;

    @Schema(description = "New password", example = "securePass123")
    @Size(min = 8, max = 20)
    private String password;
}
