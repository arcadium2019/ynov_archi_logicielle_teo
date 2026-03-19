package com.coworking.memberservice.kafka;

import com.coworking.memberservice.service.MemberService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MemberEventConsumer {

    private final MemberService memberService;

    public MemberEventConsumer(MemberService memberService) {
        this.memberService = memberService;
    }

    @KafkaListener(topics = "member-suspend", groupId = "member-group")
    public void handleMemberSuspend(String message) {
        Long memberId = Long.parseLong(message.trim());
        memberService.suspendMember(memberId);
    }

    @KafkaListener(topics = "member-unsuspend", groupId = "member-group")
    public void handleMemberUnsuspend(String message) {
        Long memberId = Long.parseLong(message.trim());
        memberService.unsuspendMember(memberId);
    }
}
