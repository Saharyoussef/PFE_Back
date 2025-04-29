package com.sahar.userservice.repository;

import com.sahar.userservice.model.AccountToken;
import com.sahar.userservice.model.Credential;
import com.sahar.userservice.model.Device;
import com.sahar.userservice.model.PasswordToken;
import com.sahar.userservice.model.User;

import java.util.List;


public interface UserRepository {
    User getUserByEmail(String email);
    User getUserByUuid(String userUuid);
    User getUserById(Long userId);
    User updateUser(String userUuid, String firstName, String lastName, String email, String phone, String bio, String address);
    String createUser(String firstName, String lastName, String email, String username, String password);
    AccountToken getAccountToken(String token);
    User verifyPasswordToken(String token);
    User enableMfa(String userUuid);
    User disableMfa(String userUuid);
    User toggleAccountExpired(String userUuid);
    User toggleAccountLocked(String userUuid);
    User toggleAccountEnabled(String userUuid);
    User toggleCredentialsExpired(String userUuid);
    void updatePassword(String userUuid, String encodedPassword);
    User updateRole(String userUuid, String role);
    void resetPassword(String email);
    void doResetPassword(String userUuid, String token, String password, String confirmPassword);
    List<User> getUsers();
    Credential getCredential(String userUuid);
    List<Device> getDevices(String userUuid);
    void deleteAccountToken(String token);
    void updateAccountSettings(Long userId);
    PasswordToken getPasswordToken(String token);
    PasswordToken getPasswordToken(Long userId);
    void deletePasswordToken(String token);
    void deletePasswordToken(Long userId);
    void updateImageUrl(String userUuid, String imageUrl);
    String getPassword(String userUuid);
    String createPasswordToken(Long userId);
}