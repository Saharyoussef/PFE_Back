package com.sahar.dashboardservice.domain;

import com.sahar.dashboardservice.event.Event;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
@Getter
@Setter
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private Event payload;
    private Map<String, String> headers;
    private static final String ID = "id";
    private static final String TIMESTAMP = "timestamp";

    public Notification() {}
    public Notification(Event payload) {
        this(payload, Map.of(
                ID, UUID.randomUUID().toString(),
                TIMESTAMP, LocalDateTime.now().toString()
        ));
    }
    public Notification(@NotNull Event payload, @NotNull Map<String, String> headers) {
        this.payload = payload;
        this.headers = headers;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Notification that = (Notification) other;
        return ObjectUtils.nullSafeEquals(payload, that.payload) &&
                ObjectUtils.nullSafeEquals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHash(payload, headers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append(" [payload=");
        Object payloadContent = this.payload;
        if (payloadContent instanceof byte[] bytes) {
            sb.append("byte[").append(bytes.length).append(']');
        } else {
            sb.append(payloadContent);
        }
        sb.append(", headers=").append(this.headers).append(']');
        return sb.toString();
    }
}