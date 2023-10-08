package ru.ryazancev.parkingreservationsystem.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.ryazancev.parkingreservationsystem.web.dto.validation.OnCreate;
import ru.ryazancev.parkingreservationsystem.web.dto.validation.OnUpdate;

@Data
public class CarDTO {

    @NotNull(message = "Id must not be null", groups = OnUpdate.class)
    private Long id;

    @NotNull(message = "Number must not be null", groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^[A-Z]\\d{2}[A-Z]{2}\\d{2}$\n", message = "Car number should be in this format: A000AA00", groups = {OnCreate.class, OnUpdate.class})
    private String number;
}
