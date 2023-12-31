package ru.ryazancev.integration.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import ru.ryazancev.integration.BaseIT;
import ru.ryazancev.parkingreservationsystem.models.parking.Zone;
import ru.ryazancev.parkingreservationsystem.repositories.ZoneRepository;
import ru.ryazancev.testutils.JsonUtils;
import ru.ryazancev.testutils.paths.APIPaths;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class ZoneControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ZoneRepository zoneRepository;

    private final Long ZONE_ID_FOR_TESTS = 1L;
    private Zone testZone;

    @BeforeEach
    public void setUp() {
        testZone = findObjectForTests(zoneRepository, ZONE_ID_FOR_TESTS);
    }

    @DisplayName("Get zones")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testGetZones_returnsListOfAllZones() throws Exception {
        //Arrange
        List<Zone> zones = zoneRepository.findAll();
        String zonesJson = JsonUtils.createJsonNodeForObjects(
                        zones,
                        List.of("id",
                                "number",
                                "totalPlaces",
                                "freePlaces"))
                .toString();

        //Act && Assert
        mockMvc.perform(get(APIPaths.ZONES))
                .andExpect(status().isOk())
                .andExpect(content().json(zonesJson));
    }

    @DisplayName("Get zone by id with right user details")
    @Test
    @WithUserDetails("test1@gmail.com")
    public void testGetZoneById_returnsStatusIsOkAndZoneJSON() throws Exception {
        //Act && Assert
        mockMvc.perform(get(APIPaths.ZONE_BY_ID, testZone.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(testZone.getId()))
                .andExpect(jsonPath("$.number")
                        .value(testZone.getNumber()))
                .andExpect(jsonPath("$.places")
                        .exists());
    }

}