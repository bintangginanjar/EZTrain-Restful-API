package restful.api.eztrain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import restful.api.eztrain.entity.SeatEntity;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, Long>, JpaSpecificationExecutor<SeatEntity>{

    Optional<SeatEntity> findBySeatNumber(String seatNumber);

}
