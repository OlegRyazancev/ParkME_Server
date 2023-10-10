package ru.ryazancev.parkingreservationsystem.repositories.mappers;

import lombok.SneakyThrows;
import ru.ryazancev.parkingreservationsystem.models.car.Car;
import ru.ryazancev.parkingreservationsystem.models.parking.Place;
import ru.ryazancev.parkingreservationsystem.models.parking.Status;
import ru.ryazancev.parkingreservationsystem.models.parking.Zone;
import ru.ryazancev.parkingreservationsystem.models.reservation.Reservation;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationRowMapper {

    @SneakyThrows
    public static Reservation mapRow(ResultSet resultSet) {
        if (resultSet.next()) {
            Reservation reservation = new Reservation();
            map(resultSet, reservation);
            return reservation;
        }
        return null;

    }

    @SneakyThrows
    public static List<Reservation> mapRows(ResultSet resultSet) {
        List<Reservation> reservations = new ArrayList<>();

        while (resultSet.next()) {
            Reservation reservation = new Reservation();
            map(resultSet, reservation);
            reservations.add(reservation);

        }
        return reservations;
    }

    private static void map(ResultSet resultSet, Reservation reservation) throws SQLException {
        reservation.setId(resultSet.getLong("reservation_id"));
        reservation.setTimeFrom(resultSet.getTimestamp("time_from").toLocalDateTime());
        reservation.setTimeTo(resultSet.getTimestamp("time_to").toLocalDateTime());


        if (hasColumn(resultSet, "zone_id")) {
            Zone zone = new Zone();
            zone.setId(resultSet.getLong("zone_id"));
            zone.setNumber(resultSet.getInt("zone_number"));
            reservation.setZone(zone);
        }

        if (hasColumn(resultSet, "place_id")) {
            Place place = new Place();
            place.setId(resultSet.getLong("place_id"));
            place.setNumber(resultSet.getInt("place_number"));
            place.setStatus(Status.valueOf(resultSet.getString("place_status")));
            reservation.setPlace(place);
        }

        if (hasColumn(resultSet, "car_id")) {
            Car car = new Car();
            car.setId(resultSet.getLong("car_id"));
            car.setNumber(resultSet.getString("car_number"));
            reservation.setCar(car);
        }

    }

    private static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i < columnCount; i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }
}
