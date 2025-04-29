package com.sahar.notificationservice.event;

import com.sahar.notificationservice.enumeration.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private EventType eventType;
    private Map<String, ?> data;
    // A flexible key-value map holding event-specific data (can hold any type of values)
}