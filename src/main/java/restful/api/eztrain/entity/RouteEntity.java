package restful.api.eztrain.entity;

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
@Table(name = "routes")
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
    
    private Double tripDistance;
    private Integer tripDuration;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity userEntity;

    @OneToMany(mappedBy = "routeEntity", cascade = CascadeType.ALL)
    private List<ScheduleEntity> schedules;

}
