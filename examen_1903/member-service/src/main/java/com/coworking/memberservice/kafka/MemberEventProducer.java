package com.coworking.memberservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemberEventProducer {

    private static final String MEMBER_DELETED_TOPIC = "member-deleted";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public MemberEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMemberDeleted(Long memberId) {
        kafkaTemplate.send(MEMBER_DELETED_TOPIC, memberId.toString());
    }
}
