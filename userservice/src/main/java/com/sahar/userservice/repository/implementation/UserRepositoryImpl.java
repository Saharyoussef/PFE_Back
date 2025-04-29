package com.sahar.userservice.repository.implementation;

import com.sahar.userservice.exception.ApiException;
import com.sahar.userservice.model.AccountToken;
import com.sahar.userservice.model.Credential;
import com.sahar.userservice.model.User;
import com.sahar.userservice.query.UserQuery;
import com.sahar.userservice.repository.UserRepository;
import com.sahar.userservice.utils.UserUtils;
import com.sahar.userservice.model.Device;
import com.sahar.userservice.model.PasswordToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.sql.Types.VARCHAR;
import static java.util.Map.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JdbcClient jdbc;

    @Override
    public User getUserByEmail(String email) {
        try {
            return jdbc.sql(UserQuery.SELECT_USER_BY_EMAIL_QUERY).param("email", email).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user email %s", email));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User getUserByUuid(String userUuid) {
        try {
            return jdbc.sql(UserQuery.SELECT_USER_BY_USER_UUID_QUERY).param("userUuid", userUuid).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user UUID %s", userUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User getUserById(Long userId) {
        try {
            return jdbc.sql(UserQuery.SELECT_USER_BY_USER_ID_QUERY).param("userId", userId).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user ID %s", userId));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User updateUser(String userUuid, String firstName, String lastName, String email, String phone, String bio, String address) {
        try {
            return jdbc.sql(UserQuery.UPDATE_USER_FUNCTION).paramSource(getParamSource(userUuid, firstName, lastName, email, phone, bio, address)).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user UUID %s", userUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public String createUser(String firstName, String lastName, String email, String username, String password) {
        try {
            var token = UserUtils.randomUUUID.get();
            jdbc.sql(UserQuery.CREATE_USER_STORED_PROCEDURE).paramSource(getParamSource(firstName, lastName, email, username, password, token)).update();
            return token;
        } catch (DuplicateKeyException exception) {
            log.error(exception.getMessage());
            throw new ApiException("Email/username already in use. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public AccountToken getAccountToken(String token) {
        try {
            return jdbc.sql(UserQuery.SELECT_ACCOUNT_TOKEN_QUERY).param("token", token).query(AccountToken.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("Invalid link. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyPasswordToken(String token) {
        return null;
    }

    @Override
    public User enableMfa(String userUuid) {
        try {
            return jdbc.sql(UserQuery.ENABLE_USER_MFA_FUNCTION).paramSource(getParamSource(userUuid, UserUtils.qrCodeSecret.get())).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User disableMfa(String userUuid) {
        try {
            return jdbc.sql(UserQuery.DISABLE_USER_MFA_FUNCTION).param("userUuid", userUuid).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User toggleAccountExpired(String userUuid) {
        try {
            return jdbc.sql(UserQuery.TOGGLE_ACCOUNT_EXPIRED_FUNCTION).param("userUuid", userUuid).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User toggleAccountLocked(String userUuid) {
        try {
            return jdbc.sql(UserQuery.TOGGLE_ACCOUNT_LOCKED_FUNCTION).param("userUuid", userUuid).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User toggleAccountEnabled(String userUuid) {
        try {
            return jdbc.sql(UserQuery.TOGGLE_ACCOUNT_ENABLED_FUNCTION).param("userUuid", userUuid).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User toggleCredentialsExpired(String userUuid) {
        return null;
    }

    @Override
    public void updatePassword(String userUuid, String encodedPassword) {
        try {
            jdbc.sql(UserQuery.UPDATE_USER_PASSWORD_QUERY).params(of("userUuid", userUuid, "password", encodedPassword)).update();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User updateRole(String userUuid, String role) {
        try {
            return jdbc.sql(UserQuery.UPDATE_USER_ROLE_FUNCTION).params(of("userUuid", userUuid, "role", role)).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void resetPassword(String email) {

    }

    @Override
    public void doResetPassword(String userUuid, String token, String password, String confirmPassword) {

    }

    @Override
    public List<User> getUsers() {
        try {
            return jdbc.sql(UserQuery.SELECT_USERS_QUERY).query(User.class).list();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("Users not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void deleteAccountToken(String token) {
        try {
            jdbc.sql(UserQuery.DELETE_ACCOUNT_TOKEN_QUERY).param("token", token).update();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("Token not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void deletePasswordToken(String token) {
        try {
            jdbc.sql("").param("token", token).update();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("Token not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void deletePasswordToken(Long userId) {
        try {
            jdbc.sql(UserQuery.DELETE_PASSWORD_TOKEN_QUERY).param("userId", userId).update();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("Token not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public String createPasswordToken(Long userId) {
        try {
            var token = UserUtils.randomUUUID.get();
            jdbc.sql(UserQuery.CREATE_PASSWORD_TOKEN_QUERY).params(of("userId", userId, "token", token)).update();
            return token;
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public String getPassword(String userUuid) {
        try {
            return jdbc.sql(UserQuery.SELECT_USER_PASSWORD_QUERY).param("userUuid", userUuid).query(String.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void updateImageUrl(String userUuid, String imageUrl) {
        try {
            jdbc.sql(UserQuery.UPDATE_USER_IMAGE_URL_QUERY).params(of("userUuid", userUuid, "imageUrl", imageUrl)).update();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public PasswordToken getPasswordToken(Long userId) {
        try {
            return jdbc.sql(UserQuery.SELECT_PASSWORD_TOKEN_BY_USER_ID_QUERY).params(of("userId", userId)).query(PasswordToken.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            //throw new ApiException("Invalid link. Please try again.");
            return null;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public PasswordToken getPasswordToken(String token) {
        try {
            return jdbc.sql(UserQuery.SELECT_PASSWORD_TOKEN_QUERY).param("token", token).query(PasswordToken.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("Invalid link. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void updateAccountSettings(Long userId) {
        try {
            jdbc.sql(UserQuery.UPDATE_ACCOUNT_SETTINGS_QUERY).param("userId", userId).update();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public Credential getCredential(String userUuid) {
        try {
            return jdbc.sql(UserQuery.SELECT_USER_CREDENTIAL_QUERY).param("userUuid", userUuid).query(Credential.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("Credential not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public List<Device> getDevices(String userUuid) {
        try {
            return jdbc.sql(UserQuery.SELECT_DEVICES_QUERY).param("userUuid", userUuid).query(Device.class).list();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("User not found. Please try again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private SqlParameterSource getParamSource(String userUuid, String qrCodeSecret) {
        return new MapSqlParameterSource()
                .addValue("userUuid", userUuid, VARCHAR)
                .addValue("qrCodeSecret", qrCodeSecret, VARCHAR)
                .addValue("qrCodeImageUri", UserUtils.qrCodeImageUri.apply(qrCodeSecret), VARCHAR);
    }

    private SqlParameterSource getParamSource(String userUuid, String firstName, String lastName, String email, String phone, String bio, String address) {
        return new MapSqlParameterSource()
                .addValue("userUuid", userUuid, VARCHAR)
                .addValue("firstName", firstName, VARCHAR)
                .addValue("lastName", lastName, VARCHAR)
                .addValue("email", email.trim().toLowerCase(), VARCHAR)
                .addValue("phone", phone, VARCHAR)
                .addValue("address", address, VARCHAR)
                .addValue("bio", bio, VARCHAR);
    }

    private SqlParameterSource getParamSource(String firstName, String lastName, String email, String username, String password, String token) {
        return new MapSqlParameterSource()
                .addValue("userUuid", UserUtils.randomUUUID.get(), VARCHAR)
                .addValue("firstName", firstName, VARCHAR)
                .addValue("lastName", lastName, VARCHAR)
                .addValue("email", email.trim().toLowerCase(), VARCHAR)
                .addValue("username", username.trim().toLowerCase(), VARCHAR)
                .addValue("password", password, VARCHAR)
                .addValue("token", token, VARCHAR)
                .addValue("credentialUuid", UserUtils.randomUUUID.get(), VARCHAR)
                .addValue("memberId", UserUtils.memberId.get(), VARCHAR);
    }
}