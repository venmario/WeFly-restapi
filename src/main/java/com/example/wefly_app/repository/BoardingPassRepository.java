package com.example.wefly_app.repository;

import com.example.wefly_app.entity.BoardingPass;
import com.example.wefly_app.entity.SeatAvailability;
import com.example.wefly_app.entity.enums.SeatClass;
import com.example.wefly_app.request.checkin.BoardingPassDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardingPassRepository extends JpaSpecificationExecutor<BoardingPass>, PagingAndSortingRepository<BoardingPass, Long> {
//    @Query("SELECT sa FROM SeatAvailability sa " +
//            "JOIN sa.flightSchedule fs " +
//            "JOIN fs.flightClasses fc " +
//            "WHERE sa.available = true " +
//            "AND fc.id = :flightClassId " +
//            "AND sa.seatClass = fc.seatClass")
//    List<SeatAvailability> findAvailableSeats(@Param("flightClassId") Long flightClassId);

    @Query("SELECT new com.example.wefly_app.request.checkin.BoardingPassDTO(" +
            "fs.departureDate, f.flightCode, fc.seatClass, dep.iata, arr.iata, f.departureTime, f.arrivalTime) " +
            "FROM TransactionDetail td " +
            "JOIN td.flightClass fc " +
            "JOIN fc.flightSchedule fs " +
            "JOIN fs.flight f " +
            "JOIN f.departureAirport dep " +
            "JOIN f.arrivalAirport arr " +
            "WHERE td.id = :transactionDetailId")
    BoardingPassDTO findFlightDetailsByTransactionDetailId(@Param("transactionDetailId") Long transactionDetailId);
}
