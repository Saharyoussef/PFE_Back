package com.sahar.notificationservice.domain;

import com.sahar.notificationservice.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification implements Serializable {
    private Event payload; // The main content of the notification (the event that happened: type + data)
    private Map<String, String> headers; // Extra metadata or properties related to the notification (optional)

    // Override equals method: to compare two Notification objects properly
    public boolean equals(@Nullable Object other) {
        boolean var10000;
        if (this != other) {
            label28: {
                if (other instanceof Notification) {
                    var that = (Notification)other;
                    if (ObjectUtils.nullSafeEquals(this.payload, that.payload) && this.headers.equals(that.headers)) {
                        break label28;
                    }
                }
                var10000 = false;
                return var10000;
            }
        }
        var10000 = true;
        return var10000;
    }

    // Override hashCode method: needed whenever equals is overridden, ensures consistent behavior in collections
    public int hashCode() {
        return ObjectUtils.nullSafeHash(this.payload, this.headers);
    }

    // Override toString method: gives a human-readable version of the object
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append(" [payload=");
        Object var3 = this.payload;
        if (var3 instanceof byte[] bytes) {
            sb.append("byte[").append(bytes.length).append(']');
        } else {
            sb.append(this.payload);
        }
        sb.append(", headers=").append(this.headers).append(']');
        return sb.toString();
    }
}