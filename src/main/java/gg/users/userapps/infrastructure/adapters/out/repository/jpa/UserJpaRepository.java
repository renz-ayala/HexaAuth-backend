package gg.users.userapps.infrastructure.adapters.out.repository.jpa;

import gg.users.userapps.infrastructure.adapters.out.repository.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    @Query(
            value = "SELECT * FROM user1.fn_get_roles_by_user_id(:userId)",
            nativeQuery = true
    )
    List<String> findRolesByUserIdFn(@Param("userId") Long userId);
}
