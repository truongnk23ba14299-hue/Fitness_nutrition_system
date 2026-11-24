package com.fitness.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private Sepay sepay = new Sepay();
    private int qrExpireMinutes = 10;
    private boolean devSimulateEnabled = true;

    @Data
    public static class Sepay {
        private String bankCode;
        private String bankName;
        private String accountNumber;
        private String accountName;
        private String webhookApiKey;
    }
}