package gg.users.userapps.infraestructure.adapters.out.repository;

import gg.users.userapps.domain.model.User;
import gg.users.userapps.domain.ports.out.UserRepository;
import gg.users.userapps.infraestructure.adapters.out.repository.entities.UserEntity;
import gg.users.userapps.infraestructure.adapters.out.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void createUser(User user) {
        String sql = "CALL user1.sp_create_user(?, ?, ?, ?, ?, NULL, NULL, NULL)";

        Map<String, Object> result = jdbcTemplate.queryForMap(
                sql,
                user.getUsername(),
                user.getPassword(),
                user.getName(),
                user.getLastName(),
                user.getEmail()
        );

        Number userIdNumber = (Number) result.get("p_user_id");
        Long userId = userIdNumber != null ? userIdNumber.longValue() : null;

        Number codeRespNumber = (Number) result.get("p_code_resp");
        Integer codeResp = codeRespNumber != null ? codeRespNumber.intValue() : 0;

        String msg = (String) result.get("p_msg");

        log.info("Creación de usuario => ID: {}, Código: {}, Mensaje: {}", userId, codeResp, msg);

        if (codeResp == 0) {
            throw new RuntimeException(msg);
        }

        user.setAccountId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUser(String username, String password) {
        String sql = "CALL user1.sp_validate_password(?, ?, NULL)";

        Map<String, Object> result = jdbcTemplate.queryForMap(sql, username, password);

        Number isValidNumber = (Number) result.get("p_is_valid");
        Integer isValid = isValidNumber != null ? isValidNumber.intValue() : 0;

        if (isValid == 1) {
            Optional<UserEntity> userEntity = userJpaRepository.findByUsername(username);
            return userEntity.map(this::mapToDomain).orElse(null);
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        return userJpaRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        String sql = "CALL user1.sp_change_password(?, ?, ?, NULL, NULL)";

        Map<String, Object> result = jdbcTemplate.queryForMap(
                sql,
                username,
                oldPassword,
                newPassword
        );

        Number codeRespNumber = (Number) result.get("p_code_resp");
        Integer codeResp = codeRespNumber != null ? codeRespNumber.intValue() : 0;
        String msg = (String) result.get("p_msg");

        log.info("Respuesta SP sp_change_password => Código: {}, Mensaje: {}", codeResp, msg);

        if (codeResp == 0) {
            throw new RuntimeException(msg);
        }
    }

    private User mapToDomain(UserEntity userEntity) {
        User user = new User();
        user.setAccountId(userEntity.getCuentaId());
        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPassword());
        user.setName(userEntity.getName());
        user.setLastName(userEntity.getLastName());
        user.setEmail(userEntity.getEmail());
        user.setTsCrea(userEntity.getTsCrea());
        return user;
    }
}