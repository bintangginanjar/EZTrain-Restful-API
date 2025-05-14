package restful.api.eztrain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;

public interface StationRepository extends JpaRepository<StationEntity, Long>, JpaSpecificationExecutor<StationEntity> {

    Optional<StationEntity> findFirstByUserEntityAndId(UserEntity user, Long stationId);

    Optional<StationEntity> findByCode(String code);

    List<StationEntity> findAllByUserEntity(UserEntity user);
}
