package ru.practicum.shareit.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import org.hibernate.Hibernate;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
                return false;
            }
            Booking booking = (Booking) o;
            return id != null && Objects.equals(id, booking.getId());
        }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Booking{" + "id=" + id + ", start_date=" + startDate + ", end_date=" + endDate +
                // getId() usually doesn't initialize the proxy
                ", itemId=" + (item != null ? item.getId() : null) +
                // same here
                ", bookerId=" + (booker != null ? booker.getId() : null) + ", status=" + status + '}';
    }
}