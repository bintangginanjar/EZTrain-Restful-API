package restful.api.eztrain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import restful.api.eztrain.entity.CoachTypeEntity;
import restful.api.eztrain.entity.RouteEntity;
import restful.api.eztrain.entity.RoutePriceEntity;


public interface RoutePriceRepository extends JpaRepository<RoutePriceEntity, Long>, JpaSpecificationExecutor<RoutePriceEntity>{

    Optional<RoutePriceEntity> findByCoachTypeEntity(CoachTypeEntity coachTypeEntity);
    
    Optional<RoutePriceEntity> findByRouteEntity(RouteEntity routeEntity);

    Page<RoutePriceEntity> findByRouteEntityOrCoachTypeEntity(RouteEntity routeEntity, CoachTypeEntity coachTypeEntity);

}
