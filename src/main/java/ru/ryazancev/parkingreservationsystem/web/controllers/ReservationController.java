package ru.ryazancev.parkingreservationsystem.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.ryazancev.parkingreservationsystem.models.reservation.Reservation;
import ru.ryazancev.parkingreservationsystem.services.ReservationService;
import ru.ryazancev.parkingreservationsystem.web.dto.reservation.ReservationDTO;
import ru.ryazancev.parkingreservationsystem.web.dto.reservation.ReservationInfoDTO;
import ru.ryazancev.parkingreservationsystem.util.validation.OnUpdate;
import ru.ryazancev.parkingreservationsystem.util.mappers.reservation.ReservationInfoMapper;
import ru.ryazancev.parkingreservationsystem.util.mappers.reservation.ReservationMapper;

import java.util.List;

@RestController
@RequestMapping("api/v1/reservations")
@RequiredArgsConstructor
@Validated
public class ReservationController {


    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    @PutMapping
    @PreAuthorize("@customSecurityExpression.canAccessReservation(#reservationDTO.id)")
    public ReservationDTO changeTimeTo(@Validated(OnUpdate.class) @RequestBody ReservationDTO reservationDTO) {
        Reservation reservation = reservationMapper.toEntity(reservationDTO);
        Reservation updatedReservation = reservationService.changeTimeTo(reservation);

        return reservationMapper.toDTO(updatedReservation);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@customSecurityExpression.canAccessReservation(#reservationDTO.id)")
    public void deleteById(@PathVariable("id") Long id) {
        reservationService.delete(id);
    }
}
