package restful.api.eztrain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import restful.api.eztrain.entity.TrainEntity;
import restful.api.eztrain.entity.UserEntity;

@Repository
public interface TrainRepository extends JpaRepository <TrainEntity, Long>, JpaSpecificationExecutor<TrainEntity> {

    Optional<TrainEntity> findByName(String name);

    Optional<TrainEntity> findFirstByUserEntityAndId(UserEntity user, Long id);
    
}
