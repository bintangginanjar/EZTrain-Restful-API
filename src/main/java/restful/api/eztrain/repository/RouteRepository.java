package restful.api.eztrain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import restful.api.eztrain.entity.RouteEntity;
import restful.api.eztrain.entity.StationEntity;

public interface RouteRepository extends JpaRepository<RouteEntity, Long>, JpaSpecificationExecutor<StationEntity>{

    Optional<RouteEntity> findByOriginAndDestination(StationEntity origin, StationEntity destination);

    Page<RouteEntity> findByOriginOrDestination(StationEntity origin, StationEntity destination, Pageable pageable);

}
