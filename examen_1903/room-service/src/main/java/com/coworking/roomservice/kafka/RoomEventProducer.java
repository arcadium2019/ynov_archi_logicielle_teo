package com.coworking.roomservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RoomEventProducer {

    private static final String ROOM_DELETED_TOPIC = "room-deleted";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public RoomEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRoomDeleted(Long roomId) {
        kafkaTemplate.send(ROOM_DELETED_TOPIC, roomId.toString());
    }
}
