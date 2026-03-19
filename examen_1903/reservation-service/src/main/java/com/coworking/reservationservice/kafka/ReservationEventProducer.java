package com.coworking.reservationservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventProducer {

    private static final String MEMBER_SUSPEND_TOPIC = "member-suspend";
    private static final String MEMBER_UNSUSPEND_TOPIC = "member-unsuspend";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ReservationEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMemberSuspend(Long memberId) {
        kafkaTemplate.send(MEMBER_SUSPEND_TOPIC, memberId.toString());
    }

    public void sendMemberUnsuspend(Long memberId) {
        kafkaTemplate.send(MEMBER_UNSUSPEND_TOPIC, memberId.toString());
    }
}
