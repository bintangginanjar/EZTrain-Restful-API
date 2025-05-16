package restful.api.eztrain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import restful.api.eztrain.entity.CoachTypeEntity;
import java.util.Optional;

public interface CoachTypeRepository extends JpaRepository<CoachTypeEntity, Long> {    

    Optional<CoachTypeEntity> findByName(String name);

}
