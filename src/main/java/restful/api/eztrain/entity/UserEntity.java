package restful.api.eztrain.entity;

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
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    private String token;

    @Column(name = "token_expired_at")
    private Long tokenExpiredAt;

    private String fullName;

    private String phoneNumber;

    private Boolean isVerified;

    private Boolean isActive;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(updatable = true, name = "updated_at")
    private Date updatedAt;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<StationEntity> stations;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<RouteEntity> routes;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<TrainEntity> trains;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<CoachEntity> coaches;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<SeatEntity> seats;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<TicketEntity> tickets;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<PaymentEntity> payments;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<VoucherEntity> vouchers;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<ScheduleEntity> schedules;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private List<RoleEntity> roles;
}
