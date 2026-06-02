package com.vcall.customer.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.customer.dto.CustomerTagRequest;
import com.vcall.customer.dto.CustomerTagResponse;
import com.vcall.customer.entity.CustomerTag;
import com.vcall.customer.repository.CustomerTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerTagService {

    private final CustomerTagRepository customerTagRepository;

    @Transactional(readOnly = true)
    public List<CustomerTagResponse> findAll() {
        return customerTagRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerTagResponse findById(Long id) {
        CustomerTag tag = customerTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        return toResponse(tag);
    }

    public CustomerTagResponse create(CustomerTagRequest request) {
        CustomerTag tag = new CustomerTag();
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        return toResponse(customerTagRepository.save(tag));
    }

    public CustomerTagResponse update(Long id, CustomerTagRequest request) {
        CustomerTag tag = customerTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        return toResponse(customerTagRepository.save(tag));
    }

    public void delete(Long id) {
        CustomerTag tag = customerTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        tag.setIsDeleted(true);
        customerTagRepository.save(tag);
    }

    private CustomerTagResponse toResponse(CustomerTag tag) {
        return CustomerTagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
}
