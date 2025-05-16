package restful.api.eztrain.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "coaches")
public class CoachEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String coachName;

    private Integer coachNumber;
    
    //private String coachType;
    @ManyToOne
    @JoinColumn(name = "coach_type_id", nullable = false, referencedColumnName = "id")
    private CoachTypeEntity coachTypeEntity;

    private Boolean isActive;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(updatable = true, name = "updated_at")
    private Date updatedAt;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "coaches_seats",
        joinColumns = @JoinColumn(name = "coach_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "seat_id", referencedColumnName = "id")
    )
    private List<SeatEntity> seats;

    @OneToMany(mappedBy = "coachEntity", cascade = CascadeType.ALL)
    private List<TicketEntity> tickets;

    @Builder.Default
    @ManyToMany(mappedBy = "coaches")
    private List<TrainEntity> trains = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity userEntity;

}
