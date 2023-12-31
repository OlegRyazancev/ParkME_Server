package ru.ryazancev.parkingreservationsystem.web.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.ryazancev.parkingreservationsystem.util.validation.OnCreate;
import ru.ryazancev.parkingreservationsystem.util.validation.OnUpdate;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    @NotNull(message = "Id must not be null",
            groups = OnUpdate.class)
    private Long id;

    @NotNull(message = "Name must not be null",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255,
            message = "Name length must be smaller than 255 symbols",
            groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @NotNull(message = "Email must not be null",
            groups = {OnCreate.class, OnUpdate.class})
    @Email(message = "Email must be in correct form",
            regexp = "^[A-Za-z0-9+_.-]+@(.+)$",
            groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255,
            message = "Email length must be smaller than 255 symbols",
            groups = {OnCreate.class, OnUpdate.class})
    private String email;

    @NotNull(message = "Password must be not null",
            groups = {OnCreate.class, OnUpdate.class})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "Password confirmation must be not null",
            groups = OnCreate.class)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordConfirmation;

}


