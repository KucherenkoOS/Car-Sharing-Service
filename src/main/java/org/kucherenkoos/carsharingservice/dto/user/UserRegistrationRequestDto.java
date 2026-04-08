package org.kucherenkoos.carsharingservice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.kucherenkoos.carsharingservice.validation.FieldMatch;

@Getter
@Setter
@FieldMatch(first = "password", second = "repeatPassword",
        message = "Password must match")
@Schema(description = "Request DTO for registration")
public class UserRegistrationRequestDto {

    @Schema(description = "Email", example = "example@mail.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "Password", example = "securePass123")
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @Schema(description = "Repeat password", example = "securePass123")
    @NotBlank
    @Size(min = 8, max = 20)
    private String repeatPassword;

    @Schema(description = "First name", example = "Kevin")
    @NotBlank
    private String firstName;

    @Schema(description = "Last name", example = "Estre")
    @NotBlank
    private String lastName;
}
