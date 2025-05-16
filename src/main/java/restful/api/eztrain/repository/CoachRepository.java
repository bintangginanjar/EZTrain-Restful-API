package restful.api.eztrain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import restful.api.eztrain.entity.CoachEntity;
import java.util.Optional;


@Repository
public interface CoachRepository extends JpaRepository<CoachEntity, Long>, JpaSpecificationExecutor<CoachEntity>{

    Optional<CoachEntity> findByCoachName(String coachName);    

}
