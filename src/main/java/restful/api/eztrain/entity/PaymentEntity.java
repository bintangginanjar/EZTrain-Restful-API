package restful.api.eztrain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentMethod;

    @Column(nullable = false)
    private Double originalAmount;

    private Double discountAmount;
    private Double finalAmount;

    private String paymentStatus;
    private LocalDateTime paidAt;

    @OneToOne
    @JoinColumn(name = "ticket_id")
    private TicketEntity ticket;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false, referencedColumnName = "id")
    private VoucherEntity voucherEntity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity userEntity;

}
