package ru.ryazancev.parkingreservationsystem.web.dto.car;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import ru.ryazancev.parkingreservationsystem.models.car.CarType;
import ru.ryazancev.parkingreservationsystem.util.validation.OnCreate;
import ru.ryazancev.parkingreservationsystem.util.validation.OnUpdate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarDTO {

    @NotNull(message = "Id must not be null",
            groups = OnUpdate.class)
    private Long id;

    @NotNull(message = "Number must not be null",
            groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^[A-Z]\\d{3}[A-Z]{2}\\d{2,3}$",
            message = "Car number should be in this format: A000AA00",
            groups = {OnCreate.class, OnUpdate.class})
    private String number;

    @NotNull(message = "Car type must be not null")
    private CarType type;
}
