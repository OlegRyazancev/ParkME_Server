package ru.ryazancev.parkingreservationsystem.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ryazancev.parkingreservationsystem.models.car.Car;
import ru.ryazancev.parkingreservationsystem.models.parking.Place;
import ru.ryazancev.parkingreservationsystem.models.parking.PlaceStatus;
import ru.ryazancev.parkingreservationsystem.models.parking.Zone;
import ru.ryazancev.parkingreservationsystem.models.reservation.Reservation;
import ru.ryazancev.parkingreservationsystem.models.reservation.ReservationStatus;
import ru.ryazancev.parkingreservationsystem.models.user.User;
import ru.ryazancev.parkingreservationsystem.repositories.*;
import ru.ryazancev.parkingreservationsystem.services.ReservationService;
import ru.ryazancev.parkingreservationsystem.util.ReservationUtils;
import ru.ryazancev.parkingreservationsystem.util.exceptions.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final PlaceRepository placeRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;

    @Override
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> getReservationsByUserId(final Long userId) {
        List<Reservation> reservations = reservationRepository
                .findAllByUserIdOrderByTimeFromDesc(userId);
        if (reservations.isEmpty()) {
            throw new IllegalStateException("User don't make any reservation");
        }
        return reservations;
    }

    @Transactional
    @Override
    public Reservation cancel(final Long reservationId) {

        Reservation existingReservation = reservationRepository
                .findById(reservationId).orElseThrow(() ->
                        new ResourceNotFoundException("Reservation not found")
                );

        switch (existingReservation.getStatus()) {
            case ACTIVE -> {
                Place foundPlace = placeRepository
                        .findById(existingReservation.getPlace().getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Place not found"));
                foundPlace.setStatus(PlaceStatus.FREE);
                placeRepository.save(foundPlace);
                existingReservation.setStatus(ReservationStatus.COMPLETED);
                return reservationRepository.save(existingReservation);
            }
            case COMPLETED -> throw new IllegalStateException(
                    "Cannot cancel completed reservation");
            default -> {
                existingReservation.setStatus(ReservationStatus.CANCELED);
                return reservationRepository.save(existingReservation);
            }
        }
    }

    @Transactional
    @Override
    public Reservation create(final Reservation reservation,
                              final Long userId) {
        if (reservation.getTimeTo().isBefore(reservation.getTimeFrom())) {
            throw new IllegalStateException("Please ensure that the time to"
                    + " is set after the time from");
        }
        Zone foundZone = zoneRepository
                .findByNumber(reservation.getZone().getNumber())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No zone with the specified number"
                        ));

        User foundUser = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Car foundCar = carRepository
                .findByNumber(reservation.getCar().getNumber())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Car not found"));

        Place foundPlace = foundZone.getPlaces().stream()
                .filter(place ->
                        place.getNumber().equals(
                                reservation.getPlace().getNumber()
                        ))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("No place with the"
                                + " specified number in the selected zone"));

        if (foundPlace.getStatus().equals(PlaceStatus.DISABLE)) {
            throw new IllegalStateException("Place is disabled");
        }

        List<Reservation> placeReservations =
                ReservationUtils.findActiveOrPlannedReservationsByPlace(
                        foundPlace, reservationRepository);
        List<Reservation> carReservations =
                ReservationUtils.findActiveOrPlannedResByCar(
                        foundCar, reservationRepository);

        ReservationUtils.validateNoOverlap(
                placeReservations,
                reservation,
                "Place already has reservations on these dates"
        );
        ReservationUtils.validateNoOverlap(
                carReservations,
                reservation,
                "Car already has reservations on these dates"
        );

        reservation.setZone(foundZone);
        reservation.setCar(foundCar);
        reservation.setPlace(foundPlace);
        reservation.setUser(foundUser);
        reservation.setStatus(ReservationStatus.PLANNED);
        reservationRepository.save(reservation);

        return reservation;
    }

    @Transactional
    @Override
    public Reservation changeTimeTo(final Reservation reservation) {
        Reservation existingRes = reservationRepository
                .findById(reservation.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found"));

        if (reservation.getTimeTo().isBefore(existingRes.getTimeFrom())) {
            throw new IllegalStateException("Please ensure that the time to"
                    + " is set after the time from");
        }

        reservation.setTimeFrom(existingRes.getTimeFrom());

        List<Reservation> placeReservations = new ArrayList<>(
                ReservationUtils.findActiveOrPlannedReservationsByPlace(
                        existingRes.getPlace(), reservationRepository));
        placeReservations.remove(existingRes);

        List<Reservation> carReservations = new ArrayList<>(
                ReservationUtils.findActiveOrPlannedResByCar(
                        existingRes.getCar(), reservationRepository));
        carReservations.remove(existingRes);

        ReservationUtils.validateNoOverlap(
                placeReservations,
                reservation,
                "Place already has reservations on these dates"
        );
        ReservationUtils.validateNoOverlap(
                carReservations,
                reservation,
                "Car already has reservations on these dates"
        );

        existingRes.setTimeTo(reservation.getTimeTo());

        return reservationRepository.save(existingRes);
    }

    @Transactional
    @Override
    public void delete(final Long reservationId) {
        Reservation foundReservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found"));

        foundReservation.getPlace().setStatus(PlaceStatus.FREE);
        placeRepository.save(foundReservation.getPlace());
        reservationRepository.deleteById(foundReservation.getId());
    }

}
