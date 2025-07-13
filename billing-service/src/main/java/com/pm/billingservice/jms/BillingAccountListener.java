package com.pm.billingservice.jms;

import billing.BillingRequest;
import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class BillingAccountListener {

    private static final Logger log = LoggerFactory.getLogger(BillingAccountListener.class);

    @JmsListener(destination = "patient.account.create")
    public void onMessageCreate(Message message) {
        if (message instanceof BytesMessage bytesMessage) {
            try {
                byte[] data = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(data);

                BillingRequest request = BillingRequest.parseFrom(data);

                log.info("✅ Received BillingRequest via JMS: patientId={}, name={}, email={}",
                        request.getPatientId(), request.getName(), request.getEmail());

            } catch (Exception e) {
                log.error("❌ Failed to parse BillingRequest from JMS", e);
            }
        } else {
            log.warn("⚠️ Received unexpected message type: {}", message.getClass().getName());
        }
    }

    @JmsListener(destination = "patient.account.update")
    public void onMessageUpdate(Message message) {
        log.info("📩 Received Patient Update Account via JMS: {}", message.toString());
    }
}
