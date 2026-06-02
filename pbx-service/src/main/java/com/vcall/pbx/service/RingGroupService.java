package com.vcall.pbx.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.pbx.dto.RingGroupRequest;
import com.vcall.pbx.dto.RingGroupResponse;
import com.vcall.pbx.entity.Extension;
import com.vcall.pbx.entity.RingGroup;
import com.vcall.pbx.entity.RingGroup.RingStrategy;
import com.vcall.pbx.entity.RingGroupMember;
import com.vcall.pbx.repository.ExtensionRepository;
import com.vcall.pbx.repository.RingGroupMemberRepository;
import com.vcall.pbx.repository.RingGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RingGroupService {

    private final RingGroupRepository ringGroupRepository;
    private final RingGroupMemberRepository ringGroupMemberRepository;
    private final ExtensionRepository extensionRepository;

    @Transactional
    public RingGroupResponse createRingGroup(RingGroupRequest request) {
        if (ringGroupRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Ring group already exists with name: " + request.getName());
        }

        RingGroup ringGroup = new RingGroup();
        ringGroup.setName(request.getName());
        ringGroup.setDescription(request.getDescription());
        ringGroup.setStrategy(RingStrategy.valueOf(request.getStrategy().toUpperCase()));
        ringGroup.setRingTimeout(request.getRingTimeout() != null ? request.getRingTimeout() : 30);
        ringGroup.setRingbackTone(request.getRingbackTone());
        ringGroup = ringGroupRepository.save(ringGroup);
        return toResponse(ringGroup);
    }

    @Transactional(readOnly = true)
    public RingGroupResponse getRingGroup(Long id) {
        RingGroup ringGroup = ringGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ring group not found with id: " + id));
        return toResponse(ringGroup);
    }

    @Transactional(readOnly = true)
    public Page<RingGroupResponse> getAllRingGroups(Pageable pageable) {
        return ringGroupRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public RingGroupResponse updateRingGroup(Long id, RingGroupRequest request) {
        RingGroup ringGroup = ringGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ring group not found with id: " + id));
        ringGroup.setName(request.getName());
        ringGroup.setDescription(request.getDescription());
        ringGroup.setStrategy(RingStrategy.valueOf(request.getStrategy().toUpperCase()));
        ringGroup.setRingTimeout(request.getRingTimeout());
        ringGroup.setRingbackTone(request.getRingbackTone());
        ringGroup = ringGroupRepository.save(ringGroup);
        return toResponse(ringGroup);
    }

    @Transactional
    public void deleteRingGroup(Long id) {
        RingGroup ringGroup = ringGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ring group not found with id: " + id));
        ringGroupMemberRepository.deleteAll(ringGroupMemberRepository.findByRingGroupIdOrderByPosition(id));
        ringGroup.setIsDeleted(true);
        ringGroupRepository.save(ringGroup);
    }

    @Transactional
    public void addMember(Long ringGroupId, Long extensionId, Integer position) {
        RingGroup ringGroup = ringGroupRepository.findById(ringGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Ring group not found with id: " + ringGroupId));
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found with id: " + extensionId));

        RingGroupMember member = new RingGroupMember();
        member.setRingGroup(ringGroup);
        member.setExtension(extension);
        member.setPosition(position != null ? position : 0);
        ringGroupMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long memberId) {
        RingGroupMember member = ringGroupMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Ring group member not found with id: " + memberId));
        ringGroupMemberRepository.delete(member);
    }

    @Transactional
    public void reorderMembers(Long ringGroupId, List<Long> memberIds) {
        List<RingGroupMember> members = ringGroupMemberRepository.findByRingGroupIdOrderByPosition(ringGroupId);
        for (int i = 0; i < memberIds.size() && i < members.size(); i++) {
            Long memberId = memberIds.get(i);
            members.stream()
                    .filter(m -> m.getId().equals(memberId))
                    .findFirst()
                    .ifPresent(m -> m.setPosition(i));
        }
        ringGroupMemberRepository.saveAll(members);
    }

    private RingGroupResponse toResponse(RingGroup ringGroup) {
        int memberCount = ringGroupMemberRepository.findByRingGroupIdOrderByPosition(ringGroup.getId()).size();
        return RingGroupResponse.builder()
                .id(ringGroup.getId())
                .name(ringGroup.getName())
                .description(ringGroup.getDescription())
                .strategy(ringGroup.getStrategy().name())
                .ringTimeout(ringGroup.getRingTimeout())
                .memberCount(memberCount)
                .build();
    }
}
