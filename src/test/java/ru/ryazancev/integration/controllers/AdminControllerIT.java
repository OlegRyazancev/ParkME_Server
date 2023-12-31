package ru.ryazancev.integration.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import ru.ryazancev.integration.BaseIT;
import ru.ryazancev.parkingreservationsystem.models.car.Car;
import ru.ryazancev.parkingreservationsystem.models.parking.Place;
import ru.ryazancev.parkingreservationsystem.models.parking.PlaceStatus;
import ru.ryazancev.parkingreservationsystem.models.parking.Zone;
import ru.ryazancev.parkingreservationsystem.models.reservation.Reservation;
import ru.ryazancev.parkingreservationsystem.models.user.User;
import ru.ryazancev.parkingreservationsystem.repositories.*;
import ru.ryazancev.parkingreservationsystem.web.dto.zone.ZoneDTO;
import ru.ryazancev.testutils.JsonUtils;
import ru.ryazancev.testutils.paths.APIPaths;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class AdminControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ZoneRepository zoneRepository;


    @DisplayName("Get cars by admin")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testGetCars_returnsListOfCars() throws Exception {
        //Arrange
        List<Car> cars = carRepository.findAll();
        String carsJson = JsonUtils.createJsonNodeForObjects(
                        cars,
                        List.of("id",
                                "number"))
                .toString();

        //Act && Assert
        mockMvc.perform(get(APIPaths.ADMIN_CARS))
                .andExpect(status().isOk())
                .andExpect(content().json(carsJson));
    }

    @DisplayName("Get users by admin")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testGetUsers_returnsListOfUsers() throws Exception {
        //Arrange
        List<User> users = userRepository.findAll();
        String usersJson = JsonUtils.createJsonNodeForObjects(
                        users,
                        List.of("id",
                                "name",
                                "email"))
                .toString();

        //Act && Assert
        mockMvc.perform(get(APIPaths.ADMIN_USERS))
                .andExpect(status().isOk())
                .andExpect(content().json(usersJson));
    }

    @DisplayName("Get reservations by admin")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testGetReservations_returnsListOfReservations() throws Exception {
        //Arrange
        List<Reservation> reservations = reservationRepository.findAll();
        String reservationsJson = JsonUtils.createJsonNodeForObjects(
                        reservations,
                        List.of("id",
                                "timeFrom",
                                "timeTo", "zone",
                                "place",
                                "car",
                                "user"))
                .toString();

        String extractedReservationsJson = JsonUtils.extractJsonArray(reservationsJson);

        //Act && Assert
        mockMvc.perform(get(APIPaths.ADMIN_RESERVATIONS))
                .andExpect(status().isOk())
                .andExpect(content().json(extractedReservationsJson));
    }

    @DisplayName("Get place by id")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testGetPlaceById_returnsPlaceJson() throws Exception {
        //Arrange
        Place place = findObjectForTests(placeRepository, 1L);
        String placeJson = JsonUtils.createJsonNodeForObject(
                        place,
                        List.of("id",
                                "number",
                                "status"))
                .toString();

        //Act && Assert
        mockMvc.perform(get(APIPaths.ADMIN_PLACE_BY_ID, place.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(placeJson));
    }

    @DisplayName("Create zone")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testCreateZone_returnsNewZoneJSONWithId() throws Exception {
        //Arrange
        ZoneDTO creatingZone = ZoneDTO.builder()
                .number(999)
                .build();
        String zoneJson = JsonUtils.createJsonNodeForObject(
                        creatingZone,
                        List.of("number", "totalPlaces", "freePlaces"))
                .toString();

        //Act && Assert
        mockMvc.perform(post(APIPaths.ADMIN_ZONES)
                        .content(zoneJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .exists())
                .andExpect(jsonPath("$.number")
                        .value(creatingZone.getNumber()))
                .andExpect(jsonPath("$.totalPlaces")
                        .exists())
                .andExpect(jsonPath("$.freePlaces")
                        .exists());

        //Assert
        Optional<Zone> createdZone =
                zoneRepository.findByNumber(creatingZone.getNumber());
        assertTrue(createdZone.isPresent());
        assertEquals(creatingZone.getNumber(), createdZone.get().getNumber());
    }

    @DisplayName("Create places in zone")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testCreatePlacesInZone_returnsListOfCreatedPlaces() throws Exception {
        //Arrange
        Zone zone = findObjectForTests(zoneRepository, 1L);
        int oldSize = zone.getPlaces().size();
        int initialPlacesCount = 2;

        //Act
        mockMvc.perform(post(APIPaths.ADMIN_ZONE_PLACES, zone.getId())
                        .param("places", String.valueOf(initialPlacesCount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(initialPlacesCount)));

        //Assert
        int finalPlacesCount = placeRepository.findAllByZoneId(zone.getId()).size();
        assertEquals(oldSize, finalPlacesCount - initialPlacesCount);
    }


    @DisplayName("Update zone")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testUpdateZone_returnsUpdatedZoneJSON() throws Exception {
        //Arrange
        Zone zoneToUpdate = findObjectForTests(zoneRepository, 1L);
        ZoneDTO updatingZone = ZoneDTO.builder()
                .id(zoneToUpdate.getId())
                .number(999)
                .build();
        String zoneJson = JsonUtils.createJsonNodeForObject(
                        updatingZone,
                        List.of("id",
                                "number"))
                .toString();

        //Act && Assert
        mockMvc.perform(put(APIPaths.ADMIN_ZONES, zoneToUpdate.getId())
                        .content(zoneJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(updatingZone.getId()))
                .andExpect(jsonPath("$.number")
                        .value(updatingZone.getNumber()))
                .andExpect(jsonPath("$.totalPlaces")
                        .exists())
                .andExpect(jsonPath("$.freePlaces")
                        .exists());

        //Assert
        Optional<Zone> updatedZone =
                zoneRepository.findById(updatingZone.getId());
        assertTrue(updatedZone.isPresent());
        assertEquals(updatingZone.getId(), updatedZone.get().getId());
        assertEquals(updatingZone.getNumber(), updatedZone.get().getNumber());
    }

    @DisplayName("Change place status")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testChangePlaceStatus_returnsPlaceJSONWithChangedStatus() throws Exception {
        //Arrange
        Place placeToUpdate = findObjectForTests(placeRepository, 1L);
        String status = PlaceStatus.DISABLE.name();

        //Act && Assert
        mockMvc.perform(put(APIPaths.ADMIN_PLACE_STATUS, placeToUpdate.getId())
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(placeToUpdate.getId()))
                .andExpect(jsonPath("$.number")
                        .value(placeToUpdate.getNumber()))
                .andExpect(jsonPath("$.status")
                        .value(status));

        //Assert
        Optional<Place> updatedPlace =
                placeRepository.findById(placeToUpdate.getId());
        assertTrue(updatedPlace.isPresent());
        assertEquals(placeToUpdate.getNumber(), updatedPlace.get().getNumber());
        assertEquals(PlaceStatus.DISABLE, updatedPlace.get().getStatus());
    }

    @DisplayName("Delete zone and associated places")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testDeleteZoneWithPlaces_returnsNothing() throws Exception {
        //Arrange
        Zone zoneToDelete = findObjectForTests(zoneRepository, 4L);

        //Act && Assert
        mockMvc.perform(delete(APIPaths.ADMIN_ZONE_BY_ID, zoneToDelete.getId()))
                .andExpect(status().isOk());

        //Assert
        assertFalse(zoneRepository.existsById(zoneToDelete.getId()));
        assertTrue(placeRepository.findAllByZoneId(zoneToDelete.getId()).isEmpty());

    }

    @DisplayName("Delete place by id")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testDeletePlaceById_returnsNothing() throws Exception {
        //Arrange
        Long placeToDeleteId = 1L;

        //Act && Assert
        mockMvc.perform(delete(APIPaths.ADMIN_PLACE_BY_ID, placeToDeleteId))
                .andExpect(status().isOk());

        //Assert
        assertFalse(placeRepository.existsById(placeToDeleteId));
    }
}
