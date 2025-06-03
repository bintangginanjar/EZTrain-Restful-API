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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_prices")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutePriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double price;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(updatable = true, name = "updated_at")
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "coach_type_id", nullable = false, referencedColumnName = "id")
    private CoachTypeEntity coachTypeEntity;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false, referencedColumnName = "id")
    private RouteEntity routeEntity;

    @OneToMany(mappedBy = "routePriceEntity", cascade = CascadeType.ALL)
    private List<TicketEntity> tickets;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity userEntity;

}
