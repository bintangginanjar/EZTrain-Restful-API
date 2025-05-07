package restful.api.eztrain.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "schedules")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false, referencedColumnName = "id")
    private TrainEntity trainEntity;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false, referencedColumnName = "id")
    private RouteEntity routeEntity;    

    private LocalDateTime departureDatetime;
    private LocalDateTime arrivalDatetime;
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity userEntity;

    @OneToMany(mappedBy = "scheduleEntity", cascade = CascadeType.ALL)
    private List<TicketEntity> tickets;
}
