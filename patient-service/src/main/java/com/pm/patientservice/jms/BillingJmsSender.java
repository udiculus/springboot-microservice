package com.pm.patientservice.jms;

import billing.BillingRequest;
import com.pm.patientservice.model.Patient;
import jakarta.jms.BytesMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BillingJmsSender {

    private final JmsTemplate jmsTemplate;

    public BillingJmsSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendCreateAccount(Patient patient) {
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .build();

        try {
            jmsTemplate.send("patient.account.create", session -> {
                BytesMessage message = session.createBytesMessage();
                message.writeBytes(request.toByteArray());
                return message;
            });
            log.info("✅ Sent create billing account to JMS with proto {}", patient.getId().toString());
        } catch (Exception e) {
            log.error("❌ Error sending BillingRequest to JMS", e);
        }
    }

    public void sendUpdateAccount(Patient patient) {
        try {
            jmsTemplate.convertAndSend("patient.account.update", patient);
            log.info("✅ Sent update billing account to JMS with POJO {}", patient.getId().toString());
        } catch (Exception e) {
            log.error("❌ Error sending BillingRequest to JMS", e);
        }
    }
}
