package restful.api.eztrain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import restful.api.eztrain.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity>{

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findFirstByToken(String token);

}
