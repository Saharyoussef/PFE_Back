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
public class Device {
    private Long deviceId;
    private Long userId;
    private String device;
    private String client;
    private String ipAddress;
    private String createdAt;
    private String updatedAt;
}