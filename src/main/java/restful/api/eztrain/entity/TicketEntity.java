package restful.api.eztrain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookingReference;
    private String bookingStatus;
    private Double price;

    @CreationTimestamp
    private LocalDateTime bookedAt;

    private LocalDateTime checkedInAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false, referencedColumnName = "id")
    private TrainEntity trainEntity;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false, referencedColumnName = "id")
    private ScheduleEntity scheduleEntity;

    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false, referencedColumnName = "id")
    private CoachEntity coachEntity;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false, referencedColumnName = "id")
    private SeatEntity seatEntity;

}
