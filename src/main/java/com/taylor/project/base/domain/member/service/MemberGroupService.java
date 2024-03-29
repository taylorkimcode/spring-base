package com.taylor.project.base.domain.member.service;

import com.taylor.project.base.common.exception.ApiException;
import com.taylor.project.base.common.exception.ApiExceptionCode;
import com.taylor.project.base.common.response.ApiPage;
import com.taylor.project.base.common.response.ApiPageRequest;
import com.taylor.project.base.domain.member.dto.MemberGroupRequest;
import com.taylor.project.base.domain.member.dto.MemberGroupResponse;
import com.taylor.project.base.domain.member.mapper.MemberGroupMapper;
import com.taylor.project.base.entity.Member;
import com.taylor.project.base.entity.MemberGroup;
import com.taylor.project.base.repository.MemberGroupRepository;
import com.taylor.project.base.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberGroupService {

    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;

    @Transactional
    public MemberGroupResponse create(MemberGroupRequest request) {
        if (isDuplicationMemberGroup(request.name())) {
            throw new ApiException(ApiExceptionCode.DUPLICATION_MEMBER_GROUP);
        }

        MemberGroup memberGroup = MemberGroupMapper.instance.toMemberGroupRequest(request);
        MemberGroup member = memberGroupRepository.save(memberGroup);

        return MemberGroupMapper.instance.toMemberGroupResponse(member);
    }

    @Transactional(readOnly = true)
    public MemberGroup getMemberGroupById(Long memberGroupId) {
        return memberGroupRepository.findById(memberGroupId)
            .orElseThrow(() -> new ApiException(ApiExceptionCode.NOT_FOUND_MEMBER_GROUP));
    }

    @Transactional(readOnly = true)
    public ApiPage<List<MemberGroupResponse>> getMemberGroupByName(String name,
        ApiPageRequest page) {
        Page<MemberGroup> memberGroups = memberGroupRepository
            .findByNameLikeAndDeletedYn(name, false, page.of());

        List<MemberGroupResponse> contents = memberGroups.getContent().stream()
            .map(MemberGroupMapper.instance::toMemberGroupResponse)
            .toList();

        return ApiPage.<List<MemberGroupResponse>>builder()
            .content(contents)
            .page(memberGroups)
            .build();
    }

    @Transactional(readOnly = true)
    public MemberGroupResponse getMemberGroupByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ApiException(ApiExceptionCode.NOT_FOUND_MEMBER));
        return MemberGroupMapper.instance.toMemberGroupResponse(member.getMemberGroup());
    }

    private Boolean isDuplicationMemberGroup(String name) {
        return !memberGroupRepository.findByNameAndDeletedYnFalse(name).isEmpty();
    }

}
