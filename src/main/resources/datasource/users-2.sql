CREATE SCHEMA IF NOT EXISTS user1;
DROP TABLE IF EXISTS user1.user CASCADE;
CREATE TABLE user1.user (
                            user_id    BIGSERIAL PRIMARY KEY,
                            username   VARCHAR(50) NOT NULL UNIQUE,
                            password   VARCHAR(255) NOT NULL,
                            name    VARCHAR(100),
                            last_name  VARCHAR(100),
                            email     VARCHAR(150),
                            active     INT DEFAULT 1,
                            ts_crea    TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

create table user1.role (
                            role_id serial not null,
                            name character varying(50) not null,
                            constraint role_pkey primary key (role_id),
                            constraint role_name_key unique (name)
) TABLESPACE pg_default;

drop table user1.user_role
create table user1.user_role (
                                 user_id bigint not null,
                                 role_id integer not null,
                                 constraint user_role_pkey primary key (user_id, role_id),
                                 constraint fk_role foreign KEY (role_id) references user1.role (role_id) on delete CASCADE,
                                 constraint fk_user foreign KEY (user_id) references user1."user" (user_id) on delete CASCADE
) TABLESPACE pg_default;

create table user1.verification_codes (
                                          email character varying(150) not null,
                                          code character varying(10) not null,
                                          created_at timestamp without time zone null default CURRENT_TIMESTAMP,
                                          constraint verification_codes_pkey primary key (email, code)
) TABLESPACE pg_default;

truncate user1.user cascade;
select * from user1.user
select * from user1.verification_codes
select * from user1.role
select * from user1.user_role

    insert into user1.user_role (user_id, role_id)
values (1, 2)

-- crypto
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE PROCEDURE user1.sp_get_user_by_username(
    IN p_username VARCHAR,
    OUT p_name VARCHAR,
    OUT p_last_name VARCHAR,
    OUT p_email VARCHAR,
    OUT p_active INT
)
LANGUAGE plpgsql AS $$
BEGIN
SELECT name, last_name, email, active
INTO p_name, p_last_name, p_email, p_active
FROM user1.user
WHERE username = p_username;
END;
$$;

CREATE OR REPLACE PROCEDURE user1.sp_validate_password(
    IN p_username VARCHAR,
    IN p_password_plain VARCHAR,
    OUT p_is_valid INT
)
LANGUAGE plpgsql AS $$
DECLARE
v_password_hash VARCHAR;
BEGIN
SELECT u.password
INTO v_password_hash
FROM user1.user u
WHERE u.username = p_username
  AND u.active = 1;

IF v_password_hash IS NULL THEN
        p_is_valid := 0;
        RETURN;
END IF;

    IF v_password_hash = crypt(p_password_plain, v_password_hash) THEN
        p_is_valid := 1;
ELSE
        p_is_valid := 0;
END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE user1.sp_create_user(
    IN p_username VARCHAR,
    IN p_password_plain VARCHAR,
    IN p_name VARCHAR,
    IN p_last_name VARCHAR,
    IN p_email VARCHAR,
    OUT p_user_id BIGINT,
    OUT p_code_resp INT,
    OUT p_msg VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM user1.user WHERE username = p_username) THEN
        p_user_id := NULL;
        p_code_resp := 0;
        p_msg := 'El nombre de usuario ya está registrado';
        RETURN;
END IF;

    IF EXISTS (SELECT 1 FROM user1.user WHERE email = p_email) THEN
        p_user_id := NULL;
        p_code_resp := 0;
        p_msg := 'El correo electrónico ya está registrado';
        RETURN;
END IF;

INSERT INTO user1.user (username, password, name, last_name, email, active)
VALUES (
           p_username,
           crypt(p_password_plain, gen_salt('bf')),
           p_name,
           p_last_name,
           p_email,
           0
       )
    RETURNING user_id INTO p_user_id;

p_code_resp := 1;
    p_msg := 'Usuario creado exitosamente';

EXCEPTION WHEN OTHERS THEN
    p_user_id := NULL;
    p_code_resp := 0;
    p_msg := 'Error al registrar usuario: ' || SQLERRM;
END;
$$;

CREATE OR REPLACE PROCEDURE user1.sp_change_password(
    IN p_username VARCHAR,
    IN p_old_password VARCHAR,
    IN p_new_password VARCHAR,
    OUT p_code_resp INT,
    OUT p_msg VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
v_is_valid INT;
BEGIN
SELECT p_is_valid INTO v_is_valid
FROM (
         SELECT
             CASE
                 WHEN u.password = crypt(p_old_password, u.password)
                     THEN 1 ELSE 0 END AS p_is_valid
         FROM user1.user u
         WHERE username = p_username
           AND active = 1
     ) t;

IF v_is_valid IS NULL OR v_is_valid = 0 THEN
        p_code_resp := 0;
        p_msg := 'La contraseña actual es incorrecta';
        RETURN;
END IF;

UPDATE user1.user
SET password = crypt(p_new_password, gen_salt('bf'))
WHERE username = p_username AND active = 1;

p_code_resp := 1;
    p_msg := 'Contraseña actualizada exitosamente';

EXCEPTION WHEN OTHERS THEN
    p_code_resp := 0;
    p_msg := 'Error al actualizar contraseña: ' || SQLERRM;
END;
$$;

-- codigos email
CREATE TABLE IF NOT EXISTS user1.verification_codes (
                                                        email      VARCHAR(150) NOT NULL,
    code       VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (email, code)
    );

CREATE OR REPLACE PROCEDURE user1.sp_generate_verify_code(
    IN p_email VARCHAR,
    IN p_code VARCHAR,
    OUT p_code_resp INT,
    OUT p_msg VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
DELETE FROM user1.verification_codes WHERE email = p_email;

INSERT INTO user1.verification_codes (email, code)
VALUES (p_email, p_code);

p_code_resp := 1;
    p_msg := 'Código registrado exitosamente en BD';
EXCEPTION WHEN OTHERS THEN
    p_code_resp := 0;
    p_msg := 'Error al registrar código: ' || SQLERRM;
END;
$$;

CREATE OR REPLACE PROCEDURE user1.sp_verify_code(
    IN p_email VARCHAR,
    IN p_code VARCHAR,
    OUT p_code_resp INT,
    OUT p_msg VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
v_exists INT;
BEGIN
SELECT COUNT(1) INTO v_exists
FROM user1.verification_codes
WHERE email = p_email
  AND code = p_code;

IF v_exists > 0 THEN
        p_code_resp := 1;
        p_msg := 'Código válido encontrado';
ELSE
        p_code_resp := 0;
        p_msg := 'Código o email incorrecto';
END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE user1.sp_update_code_at_database(
    IN p_email VARCHAR,
    IN p_code VARCHAR,
    OUT p_code_resp INT,
    OUT p_msg VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
DELETE FROM user1.verification_codes
WHERE email = p_email AND code = p_code;

p_code_resp := 1;
    p_msg := 'Código consumido y eliminado de la BD';
EXCEPTION WHEN OTHERS THEN
    p_code_resp := 0;
    p_msg := 'Error al eliminar el código utilizado';
END;
$$;


SELECT password FROM user1.user WHERE username = 'ggrenz';

create or replace function user1.fn_get_roles_by_user_id(
    p_user_id bigint)
    returns table (
        name varchar
    )
    language plpgsql
    as $$
begin
return query
select r.name
from user1.role r
         inner join user1.user_role ur on r.role_id = ur.role_id
where ur.user_id = p_user_id;
end;
    $$
