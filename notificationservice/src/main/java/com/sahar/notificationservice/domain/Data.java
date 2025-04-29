package com.sahar.notificationservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//The Data class represents the structured information needed to personalize and send a notification,
//such as email content, and user info.
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Data {
    private String name;
    private String email;
    private String token;
    private String date;
}