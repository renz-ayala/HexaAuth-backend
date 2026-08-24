package gg.users.userapps.infrastructure.adapters.out.repository;

import gg.users.userapps.domain.model.User;
import gg.users.userapps.domain.model.commands.CreateUserResponse;
import gg.users.userapps.domain.ports.out.UserRepository;
import gg.users.userapps.infrastructure.adapters.out.repository.entities.UserEntity;
import gg.users.userapps.infrastructure.adapters.out.repository.jpa.UserJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public CreateUserResponse createUser(User user) {
        var sql = "CALL user1.sp_create_user(?, ?, ?, ?, ?, NULL, NULL, NULL)";

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
        int codeResp = codeRespNumber != null ? codeRespNumber.intValue() : 0;

        String msg = (String) result.get("p_msg");

        log.info("Creación de usuario => ID: {}, Código: {}, Mensaje: {}", userId, codeResp, msg);

        var isSuccess = codeResp == 1;

        user.setAccountId(userId);

        return new CreateUserResponse(
                msg,
                isSuccess
        );
    }

    @Override
    @Transactional(readOnly = true)
    public User findUser(String username, String password) {
        String sql = "CALL user1.sp_validate_password(?, ?, NULL)";

        Map<String, Object> result = jdbcTemplate.queryForMap(sql, username, password);

        Number isValidNumber = (Number) result.get("p_is_valid");
        int isValid = isValidNumber != null ? isValidNumber.intValue() : 0;

        if (isValid != 1) {
            return null;
        }

        Optional<UserEntity> userEntity = userJpaRepository.findByUsername(username);
        return userEntity.map(this::mapToDomain).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(this::mapToDomain)
                .orElse(null);
    }

    @Override
    public boolean activateAccount(String username, boolean toActive) {
        UserEntity user = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var activeValue = toActive ? 1 : 0;
        user.setActive(activeValue);
        userJpaRepository.save(user);
        return true;
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

        var codeRespNumber = (Number) result.get("p_code_resp");
        int codeResp = codeRespNumber != null ? codeRespNumber.intValue() : 0;

        var msg = (String) result.get("p_msg");

        log.info("Respuesta SP sp_change_password => Código: {}, Mensaje: {}", codeResp, msg);

        if (codeResp == 0) {
            throw new RuntimeException(msg);
        }
    }

    @Override
    public List<String> getRoles(Long userId) {
        return userJpaRepository
                .findRolesByUserIdFn(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean resetPassword(String username, String newPassword) {
        var sql = "CALL user1.sp_reset_password(?, ?, NULL, NULL)";

        Map<String, Object> result = jdbcTemplate.queryForMap(
                sql,
                username,
                newPassword
        );

        var codeRespNumber = (Number) result.get("p_code_resp");
        int codeResp = codeRespNumber != null ? codeRespNumber.intValue() : 0;

        var msg = (String) result.get("p_msg");

        log.info("Respuesta SP sp_reset_password => Código: {}, Mensaje: {}", codeResp, msg);
        return codeResp == 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteUser(String username) {
        userJpaRepository.deleteByUsername(username);
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
        user.setActive(userEntity.getActive());
        return user;
    }
}