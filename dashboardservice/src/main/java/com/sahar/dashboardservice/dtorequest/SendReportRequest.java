package com.sahar.dashboardservice.dtorequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data // Or @Getter @Setter @ToString @EqualsAndHashCode @RequiredArgsConstructor
public class SendReportRequest {
    @NotNull(message = "Screenshot ID cannot be null")
    private Long screenshotId;
    @Email(message = "Custom email must be a valid email address")
    private String customRecipientEmail;
    private String selectedPredefinedEmailKey;
}
