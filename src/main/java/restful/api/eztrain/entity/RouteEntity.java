package restful.api.eztrain.entity;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "routes",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"origin_station_id", "destination_station_id"}
    )    
)
public class RouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "origin_station_id", nullable = false, referencedColumnName = "id")
    private StationEntity origin;

    @ManyToOne
    @JoinColumn(name = "destination_station_id", nullable = false, referencedColumnName = "id")
    private StationEntity destination;
    
    @Column(nullable = false)
    private Double tripDistance;

    @Column(nullable = false)
    private Double tripDuration;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(updatable = true, name = "updated_at")
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity userEntity;

    @OneToMany(mappedBy = "routeEntity", cascade = CascadeType.ALL)
    private List<RoutePriceEntity> routePrices;

    @OneToMany(mappedBy = "routeEntity", cascade = CascadeType.ALL)
    private List<ScheduleEntity> schedules;

    @OneToMany(mappedBy = "routeEntity", cascade = CascadeType.ALL)
    private List<TicketEntity> tickets;

}
