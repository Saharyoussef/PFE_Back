/*
 * --- General Rules ---
 * Use underscore_names instead of CamelCase
 * Table names should be plural
 * Spell out id fields (item_id instead of id)
 * Don't use ambiguous column names
 * Name foreign key columns the same as the columns they refer to
 * Use caps for all SQL keywords
 *uq: unique   /  fk: foreign key / p: paramater / v: variable
 */

BEGIN;

-- Authrozation Server
-- this will be the angular application

CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id VARCHAR(100) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at TIMESTAMP(6) WITH TIME ZONE DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL
);

-- User Service

CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    user_uuid VARCHAR(40) NOT NULL,
    username VARCHAR(25) NOT NULL,
    first_name VARCHAR(25) NOT NULL,
    last_name VARCHAR(25) NOT NULL,
    email VARCHAR(40) NOT NULL,
    member_id VARCHAR(40) NOT NULL,
    phone VARCHAR(15) DEFAULT NULL,
    address VARCHAR(100) DEFAULT NULL,
    bio VARCHAR(100) DEFAULT NULL,
    qr_code_secret VARCHAR(50) DEFAULT NULL,
    qr_code_image_uri TEXT DEFAULT NULL,
    image_url VARCHAR(255) DEFAULT 'https://cdn-icons-png.flaticon.com/512/149/149071.png',
    last_login TIMESTAMP(6) WITH TIME ZONE DEFAULT NULL,
    login_attempts INTEGER DEFAULT 0,
    mfa BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    account_non_expired BOOLEAN NOT NULL DEFAULT FALSE,
    account_non_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_user_uuid UNIQUE (user_uuid),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_member_id UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS roles (
    role_id BIGSERIAL PRIMARY KEY,
    role_uuid VARCHAR(40) NOT NULL,
    name VARCHAR(25) NOT NULL,
    authority TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT uq_roles_role_uuid UNIQUE (role_uuid)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_role_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (role_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS credentials (
    credential_id BIGSERIAL PRIMARY KEY,
    credential_uuid VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_credentials_credential_uuid UNIQUE (credential_uuid),
    CONSTRAINT uq_credentials_user_id UNIQUE (user_id),
    CONSTRAINT fk_credentials_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS account_tokens (
    account_token_id BIGSERIAL PRIMARY KEY,
    token VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_account_tokens_token UNIQUE (token),
    CONSTRAINT uq_account_tokens_user_id UNIQUE (user_id),
    CONSTRAINT fk_account_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS password_tokens (
    password_token_id BIGSERIAL PRIMARY KEY,
    token VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_password_tokens_token UNIQUE (token),
    CONSTRAINT uq_password_tokens_user_id UNIQUE (user_id),
    CONSTRAINT fk_password_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS devices (
    device_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device VARCHAR(40) NOT NULL,
    client VARCHAR(40) NOT NULL,
    ip_address VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_devices_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Dashboards Service

CREATE TABLE IF NOT EXISTS grafanadashboard (
    grafanadashboard_id BIGSERIAL PRIMARY KEY,
    grafanadashboard_uuid VARCHAR(40) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    url TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_grafanadashboard_uuid UNIQUE (grafanadashboard_uuid)
);


CREATE TABLE IF NOT EXISTS screenshot (
    screenshot_id BIGSERIAL PRIMARY KEY,
    screenshot_uuid VARCHAR(40) NOT NULL,
    grafanadashboard_id BIGINT NOT NULL,
    url TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_screenshots_screenshot_uuid UNIQUE (screenshot_uuid),
    CONSTRAINT fk_screenshots_grafanadashboard_id FOREIGN KEY (grafanadashboard_id) REFERENCES grafanadashboard (grafanadashboard_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS screenshotdata (
    screenshotdata_id BIGSERIAL PRIMARY KEY,
    screenshotdata_uuid VARCHAR(40) NOT NULL,
    screenshot_id BIGINT NOT NULL,
    metrics_data JSONB,
    anomaly_data JSONB,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_screenshotsdata_screenshotdata_uuid UNIQUE (screenshotdata_uuid),
    CONSTRAINT fk_screenshotsdata_screenshot_id FOREIGN KEY (screenshot_id) REFERENCES screenshot (screenshot_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
-- Stored Procedures

CREATE OR REPLACE PROCEDURE create_user (IN p_user_uuid VARCHAR(40), IN p_first_name VARCHAR(25), IN p_last_name VARCHAR(25), IN p_email VARCHAR(40), IN p_username VARCHAR(25), IN p_password VARCHAR(255), IN p_credential_uuid VARCHAR(40), IN p_token VARCHAR(40), IN p_member_id VARCHAR(40))
    LANGUAGE PLPGSQL
    AS $$
    DECLARE
        v_user_id BIGINT;
    BEGIN
        INSERT INTO users (user_uuid, first_name, last_name, email, username, member_id) VALUES (p_user_uuid, p_first_name, p_last_name, p_email, p_username, p_member_id) RETURNING user_id INTO v_user_id;
        INSERT INTO credentials (credential_uuid, user_id, password) VALUES (p_credential_uuid, v_user_id, p_password);
        INSERT INTO user_roles (user_id, role_id) VALUES (v_user_id, (SELECT roles.role_id FROM roles WHERE roles.name = 'USER'));
        INSERT INTO account_tokens (user_id, token) VALUES (v_user_id, p_token);
    END;
    $$

-- Functions

CREATE OR REPLACE FUNCTION enable_user_mfa (IN p_user_uuid VARCHAR(40), IN p_qr_code_secret VARCHAR(50), IN p_qr_code_image_uri TEXT)
    RETURNS TABLE(qr_code_image_uri TEXT, member_id VARCHAR, role VARCHAR, authorities TEXT, account_non_expired BOOLEAN, account_non_locked BOOLEAN, created_at TIMESTAMP WITH TIME ZONE, email VARCHAR, enabled BOOLEAN, first_name VARCHAR, user_id BIGINT, image_url VARCHAR, last_login TIMESTAMP WITH TIME ZONE, last_name VARCHAR, mfa BOOLEAN, updated_at TIMESTAMP WITH TIME ZONE, user_uuid VARCHAR, phone VARCHAR, bio VARCHAR, address VARCHAR)
    LANGUAGE PLPGSQL
    AS $$
    BEGIN
        UPDATE users SET mfa = TRUE, qr_code_secret = p_qr_code_secret, qr_code_image_uri = p_qr_code_image_uri WHERE users.user_uuid = p_user_uuid;
        RETURN QUERY SELECT u.qr_code_image_uri, u.member_id, r.name AS role, r.authority AS authorities, u.account_non_expired, u.account_non_locked, u.created_at, u.email, u.enabled, u.first_name, u.user_id, u.image_url, u.last_login, u.last_name, u.mfa, u.updated_at, u.user_uuid, u.phone, u.bio, u.address FROM users u JOIN user_roles ur ON ur.user_id = u.user_id JOIN roles r ON r.role_id = ur.role_id WHERE u.user_uuid = p_user_uuid;
    END;
    $$

CREATE OR REPLACE FUNCTION disable_user_mfa (IN p_user_uuid VARCHAR(40))
    RETURNS TABLE(member_id VARCHAR, role VARCHAR, authorities TEXT, account_non_expired BOOLEAN, account_non_locked BOOLEAN, created_at TIMESTAMP WITH TIME ZONE, email VARCHAR, enabled BOOLEAN, first_name VARCHAR, user_id BIGINT, image_url VARCHAR, last_login TIMESTAMP WITH TIME ZONE, last_name VARCHAR, mfa BOOLEAN, updated_at TIMESTAMP WITH TIME ZONE, user_uuid VARCHAR, phone VARCHAR, bio VARCHAR, address VARCHAR)
    LANGUAGE PLPGSQL
    AS $$
    BEGIN
        UPDATE users SET mfa = FALSE, qr_code_secret = NULL, qr_code_image_uri = NULL WHERE users.user_uuid = p_user_uuid;
        RETURN QUERY SELECT u.member_id, r.name AS role, r.authority AS authorities, u.account_non_expired, u.account_non_locked, u.created_at, u.email, u.enabled, u.first_name, u.user_id, u.image_url, u.last_login, u.last_name, u.mfa, u.updated_at, u.user_uuid, u.phone, u.bio, u.address FROM users u JOIN user_roles ur ON ur.user_id = u.user_id JOIN roles r ON r.role_id = ur.role_id WHERE u.user_uuid = p_user_uuid;
    END;
    $$

CREATE OR REPLACE FUNCTION toggle_account_expired (IN p_user_uuid VARCHAR(40))
    RETURNS TABLE(qr_code_image_uri TEXT, member_id VARCHAR, role VARCHAR, authorities TEXT, account_non_expired BOOLEAN, account_non_locked BOOLEAN, created_at TIMESTAMP WITH TIME ZONE, email VARCHAR, enabled BOOLEAN, first_name VARCHAR, user_id BIGINT, image_url VARCHAR, last_login TIMESTAMP WITH TIME ZONE, last_name VARCHAR, mfa BOOLEAN, updated_at TIMESTAMP WITH TIME ZONE, user_uuid VARCHAR, phone VARCHAR, bio VARCHAR, address VARCHAR)
    LANGUAGE PLPGSQL
    AS $$
    BEGIN
        UPDATE users SET account_non_expired = NOT users.account_non_expired WHERE users.user_uuid = p_user_uuid;
        RETURN QUERY SELECT u.qr_code_image_uri, u.member_id, r.name AS role, r.authority AS authorities, u.account_non_expired, u.account_non_locked, u.created_at, u.email, u.enabled, u.first_name, u.user_id, u.image_url, u.last_login, u.last_name, u.mfa, u.updated_at, u.user_uuid, u.phone, u.bio, u.address FROM users u JOIN user_roles ur ON ur.user_id = u.user_id JOIN roles r ON r.role_id = ur.role_id WHERE u.user_uuid = p_user_uuid;
    END;
    $$

CREATE OR REPLACE FUNCTION toggle_account_locked (IN p_user_uuid VARCHAR(40))
    RETURNS TABLE(qr_code_image_uri TEXT, member_id VARCHAR, role VARCHAR, authorities TEXT, account_non_expired BOOLEAN, account_non_locked BOOLEAN, created_at TIMESTAMP WITH TIME ZONE, email VARCHAR, enabled BOOLEAN, first_name VARCHAR, user_id BIGINT, image_url VARCHAR, last_login TIMESTAMP WITH TIME ZONE, last_name VARCHAR, mfa BOOLEAN, updated_at TIMESTAMP WITH TIME ZONE, user_uuid VARCHAR, phone VARCHAR, bio VARCHAR, address VARCHAR)
    LANGUAGE PLPGSQL
    AS $$
    BEGIN
        UPDATE users SET account_non_locked = NOT users.account_non_locked WHERE users.user_uuid = p_user_uuid;
        RETURN QUERY SELECT u.qr_code_image_uri, u.member_id, r.name AS role, r.authority AS authorities, u.account_non_expired, u.account_non_locked, u.created_at, u.email, u.enabled, u.first_name, u.user_id, u.image_url, u.last_login, u.last_name, u.mfa, u.updated_at, u.user_uuid, u.phone, u.bio, u.address FROM users u JOIN user_roles ur ON ur.user_id = u.user_id JOIN roles r ON r.role_id = ur.role_id WHERE u.user_uuid = p_user_uuid;
    END;
    $$

CREATE OR REPLACE FUNCTION toggle_account_enabled (IN p_user_uuid VARCHAR(40))
    RETURNS TABLE(qr_code_image_uri TEXT, member_id VARCHAR, role VARCHAR, authorities TEXT, account_non_expired BOOLEAN, account_non_locked BOOLEAN, created_at TIMESTAMP WITH TIME ZONE, email VARCHAR, enabled BOOLEAN, first_name VARCHAR, user_id BIGINT, image_url VARCHAR, last_login TIMESTAMP WITH TIME ZONE, last_name VARCHAR, mfa BOOLEAN, updated_at TIMESTAMP WITH TIME ZONE, user_uuid VARCHAR, phone VARCHAR, bio VARCHAR, address VARCHAR)
    LANGUAGE PLPGSQL
    AS $$
    BEGIN
        UPDATE users SET enabled = NOT users.enabled WHERE users.user_uuid = p_user_uuid;
        RETURN QUERY SELECT u.qr_code_image_uri, u.member_id, r.name AS role, r.authority AS authorities, u.account_non_expired, u.account_non_locked, u.created_at, u.email, u.enabled, u.first_name, u.user_id, u.image_url, u.last_login, u.last_name, u.mfa, u.updated_at, u.user_uuid, u.phone, u.bio, u.address FROM users u JOIN user_roles ur ON ur.user_id = u.user_id JOIN roles r ON r.role_id = ur.role_id WHERE u.user_uuid = p_user_uuid;
    END;
    $$

CREATE OR REPLACE FUNCTION update_user_role (IN p_user_uuid VARCHAR(40), IN p_role VARCHAR(25))
    RETURNS TABLE(qr_code_image_uri TEXT, member_id VARCHAR, role VARCHAR, authorities TEXT, account_non_expired BOOLEAN, account_non_locked BOOLEAN, created_at TIMESTAMP WITH TIME ZONE, email VARCHAR, enabled BOOLEAN, first_name VARCHAR, user_id BIGINT, image_url VARCHAR, last_login TIMESTAMP WITH TIME ZONE, last_name VARCHAR, mfa BOOLEAN, updated_at TIMESTAMP WITH TIME ZONE, user_uuid VARCHAR, phone VARCHAR, bio VARCHAR, address VARCHAR)
    LANGUAGE PLPGSQL
    AS $$
    BEGIN
        UPDATE user_roles SET role_id = (SELECT r.role_id FROM roles r WHERE r.name = p_role) WHERE user_roles.user_id = (SELECT users.user_id FROM users WHERE users.user_uuid = p_user_uuid);
        RETURN QUERY SELECT u.qr_code_image_uri, u.member_id, r.name AS role, r.authority AS authorities, u.account_non_expired, u.account_non_locked, u.created_at, u.email, u.enabled, u.first_name, u.user_id, u.image_url, u.last_login, u.last_name, u.mfa, u.updated_at, u.user_uuid, u.phone, u.bio, u.address FROM users u JOIN user_roles ur ON ur.user_id = u.user_id JOIN roles r ON r.role_id = ur.role_id WHERE u.user_uuid = p_user_uuid;
    END;
    $$

CREATE OR REPLACE FUNCTION update_user_profile (IN p_user_uuid VARCHAR(40), IN p_first_name VARCHAR(25), IN p_last_name VARCHAR(25), IN p_email VARCHAR(40), IN p_phone VARCHAR(15),IN p_bio VARCHAR(100),IN p_address VARCHAR(100))
    RETURNS TABLE(qr_code_image_uri TEXT, member_id VARCHAR, role VARCHAR, authorities TEXT, account_non_expired BOOLEAN, account_non_locked BOOLEAN, created_at TIMESTAMP WITH TIME ZONE, email VARCHAR, enabled BOOLEAN, first_name VARCHAR, user_id BIGINT, image_url VARCHAR, last_login TIMESTAMP WITH TIME ZONE, last_name VARCHAR, mfa BOOLEAN, updated_at TIMESTAMP WITH TIME ZONE, user_uuid VARCHAR, phone VARCHAR, bio VARCHAR, address VARCHAR)
    LANGUAGE PLPGSQL
    AS $$
    BEGIN
        UPDATE users u SET 
            first_name = p_first_name, 
            last_name = p_last_name, 
            email = p_email, 
            phone = p_phone, 
            bio = p_bio, 
            address = p_address, 
            updated_at = NOW() 
        WHERE u.user_uuid = p_user_uuid;
        RETURN QUERY SELECT u.qr_code_image_uri, u.member_id, r.name AS role, r.authority AS authorities, u.account_non_expired, u.account_non_locked, u.created_at, u.email, u.enabled, u.first_name, u.user_id, u.image_url, u.last_login, u.last_name, u.mfa, u.updated_at, u.user_uuid, u.phone, u.bio, u.address FROM users u JOIN user_roles ur ON ur.user_id = u.user_id JOIN roles r ON r.role_id = ur.role_id WHERE u.user_uuid = p_user_uuid;
    END;
    $$


CREATE OR REPLACE FUNCTION create_grafanadashboard (
    IN p_grafanadashboard_uuid VARCHAR(40),
    IN p_name VARCHAR(100),
    IN p_description TEXT,
    IN p_url TEXT
)
RETURNS TABLE (
    grafanadashboard_id BIGINT,
    grafanadashboard_uuid VARCHAR,
    name VARCHAR,                 
    description TEXT,
    url TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
)
LANGUAGE PLPGSQL
AS $$
BEGIN
    RETURN QUERY
    INSERT INTO grafanadashboard (grafanadashboard_uuid, name, description, url)
    VALUES (p_grafanadashboard_uuid, p_name, p_description, p_url)
    RETURNING 
        grafanadashboard.grafanadashboard_id,
        grafanadashboard.grafanadashboard_uuid,
        grafanadashboard.name,
        grafanadashboard.description,
        grafanadashboard.url,
        grafanadashboard.created_at,
        grafanadashboard.updated_at;
END;
$$;

CREATE OR REPLACE FUNCTION update_grafanadashboard (
    IN p_grafanadashboard_uuid VARCHAR(40),
    IN p_name VARCHAR(100),
    IN p_description TEXT,
    IN p_url TEXT
)
RETURNS TABLE (
    o_grafanadashboard_id BIGINT,         
    o_grafanadashboard_uuid VARCHAR,     
    o_name VARCHAR,
    o_description TEXT,
    o_url TEXT,
    o_created_at TIMESTAMP WITH TIME ZONE,
    o_updated_at TIMESTAMP WITH TIME ZONE
)
LANGUAGE PLPGSQL
AS $$
BEGIN
    RETURN QUERY
    UPDATE grafanadashboard g 
    SET
        name = p_name,                  
        description = p_description,    
        url = p_url,                   
        updated_at = NOW()              
    WHERE
        g.grafanadashboard_uuid = p_grafanadashboard_uuid 
    RETURNING                          
        g.grafanadashboard_id,
        g.grafanadashboard_uuid,
        g.name,
        g.description,
        g.url,
        g.created_at,                  
        g.updated_at;                   

END;
$$;

CREATE OR REPLACE FUNCTION create_screenshot (
    IN p_screenshot_uuid VARCHAR(40),
    IN p_grafanadashboard_id BIGINT,
    IN p_url TEXT
)
RETURNS TABLE ( 
    o_screenshot_id BIGINT,
    o_screenshot_uuid VARCHAR,
    o_grafanadashboard_id BIGINT,
    o_url TEXT,
    o_created_at TIMESTAMP WITH TIME ZONE
)
LANGUAGE PLPGSQL
AS $$
BEGIN
    RETURN QUERY
    INSERT INTO screenshot (screenshot_uuid, grafanadashboard_id, url)
    VALUES (p_screenshot_uuid, p_grafanadashboard_id, p_url)
    RETURNING 
        screenshot.screenshot_id,
        screenshot.screenshot_uuid,
        screenshot.grafanadashboard_id,
        screenshot.url,
        screenshot.created_at;
END;
$$;