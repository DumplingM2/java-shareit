package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.dto.BookingShortDto;

@Repository
@SuppressWarnings("unused")
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND (" +
            "   (:stateName = 'ALL') OR " +
            "   (:stateName = 'CURRENT' AND :now BETWEEN b.startDate AND b.endDate) OR " +
            "   (:stateName = 'PAST' AND b.endDate < :now) OR " +
            "   (:stateName = 'FUTURE' AND b.startDate > :now) OR " +
            "   (:stateName = 'WAITING' AND b.status = ru.practicum.shareit.booking.BookingStatus.WAITING) OR " +
            "   (:stateName = 'REJECTED' AND b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED)" +
            ") ")
    Page<Booking> findBookingsByBookerAndState(
            @Param("bookerId") Long bookerId,
            @Param("stateName") String stateName,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND (" +
            "   (:stateName = 'ALL') OR " +
            "   (:stateName = 'CURRENT' AND :now BETWEEN b.startDate AND b.endDate) OR " +
            "   (:stateName = 'PAST' AND b.endDate < :now) OR " +
            "   (:stateName = 'FUTURE' AND b.startDate > :now) OR " +
            "   (:stateName = 'WAITING' AND b.status = ru.practicum.shareit.booking.BookingStatus.WAITING) OR " +
            "   (:stateName = 'REJECTED' AND b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED)" +
            ") ")
    Page<Booking> findBookingsByItemOwnerAndState(
            @Param("ownerId") Long ownerId,
            @Param("stateName") String stateName,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingShortDto(b.id, b.booker.id, b.item.id, b.startDate, b.endDate) " +
            "FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.status = ru.practicum.shareit.booking.BookingStatus.APPROVED " +
            "AND b.startDate <= :now " +
            "ORDER BY b.item.id, b.startDate DESC")
    List<BookingShortDto> findPastAndCurrentApprovedBookingsShortForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingShortDto(b.id, b.booker.id, b.item.id, b.startDate, b.endDate) " +
            "FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.status = ru.practicum.shareit.booking.BookingStatus.APPROVED " +
            "AND b.startDate > :now " +
            "ORDER BY b.startDate ASC")
    List<BookingShortDto> findNextApprovedBookingsShortForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);
}