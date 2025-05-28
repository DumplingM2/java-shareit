package ru.practicum.shareit.server.request;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("SELECT ir FROM ItemRequest ir LEFT JOIN FETCH ir.items WHERE ir.requestor.id = :requestorId ORDER BY ir.created DESC")
    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(@Param("requestorId") Long requestorId);

    @Query("SELECT ir FROM ItemRequest ir LEFT JOIN FETCH ir.items WHERE ir.requestor.id <> :userId")
    Page<ItemRequest> findAllByRequestorIdNot(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ir FROM ItemRequest ir LEFT JOIN FETCH ir.items WHERE ir.id = :id")
    Optional<ItemRequest> findByIdFetchingItems(@Param("id") Long id);

}