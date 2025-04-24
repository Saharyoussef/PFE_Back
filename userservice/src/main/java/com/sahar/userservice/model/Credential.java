package com.sahar.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Credential {
    private Long credentialId;
    private String credentialUuid;
    private String password;
    private String createdAt;
    private String updatedAt;
}
