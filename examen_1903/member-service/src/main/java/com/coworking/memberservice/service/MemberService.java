package com.coworking.memberservice.service;

import com.coworking.memberservice.kafka.MemberEventProducer;
import com.coworking.memberservice.model.Member;
import com.coworking.memberservice.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberEventProducer memberEventProducer;

    public MemberService(MemberRepository memberRepository, MemberEventProducer memberEventProducer) {
        this.memberRepository = memberRepository;
        this.memberEventProducer = memberEventProducer;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found with id: " + id));
    }

    public Member create(Member member) {
        return memberRepository.save(member);
    }

    public Member update(Long id, Member memberDetails) {
        Member member = findById(id);
        member.setFullName(memberDetails.getFullName());
        member.setEmail(memberDetails.getEmail());
        member.setSubscriptionType(memberDetails.getSubscriptionType());
        member.setSuspended(memberDetails.isSuspended());
        return memberRepository.save(member);
    }

    public void delete(Long id) {
        Member member = findById(id);
        memberRepository.delete(member);
        memberEventProducer.sendMemberDeleted(id);
    }

    public boolean checkSuspended(Long memberId) {
        Member member = findById(memberId);
        return member.isSuspended();
    }

    public void suspendMember(Long memberId) {
        Member member = findById(memberId);
        member.setSuspended(true);
        memberRepository.save(member);
    }

    public void unsuspendMember(Long memberId) {
        Member member = findById(memberId);
        member.setSuspended(false);
        memberRepository.save(member);
    }
}
