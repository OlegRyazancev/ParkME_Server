package ru.ryazancev.parkingreservationsystem.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ryazancev.config.IntegrationTestBase;
import ru.ryazancev.parkingreservationsystem.models.car.Car;


import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class CarRepositoryTest extends IntegrationTestBase {

    @Autowired
    private CarRepository carRepository;

    @DisplayName("Find all cars by user id")
    @Test
    public void testFindAllCarsByUserId_returnsListOfCars() {
        //Arrange
        Long userId = 1L;

        //Act
        List<Car> cars = carRepository.findAllByUserId(userId);

        //Assert
        assertNotNull(cars);
        assertEquals(3, cars.size());
    }

    @DisplayName("Assign car to user")
    @Test
    public void testAssignCarToUser_returnsNothing() {
        //Arrange
        Long userId = 1L;
        Car car = Car.builder()
                .number("D000DD00")
                .build();

        Car savedCar = carRepository.save(car);

        //Act
        carRepository.assignToUser(userId, savedCar.getId());

        //Assert
        List<Car> userCars = carRepository.findAllByUserId(userId);
        boolean carIsAssigned = userCars.stream()
                .anyMatch(c -> c.getNumber().equals(savedCar.getNumber()));

        assertTrue(carIsAssigned);
    }
}