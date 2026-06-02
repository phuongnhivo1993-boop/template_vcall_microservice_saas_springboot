package com.vcall.customer.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.customer.dto.CustomerTagRequest;
import com.vcall.customer.dto.CustomerTagResponse;
import com.vcall.customer.entity.CustomerTag;
import com.vcall.customer.repository.CustomerTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerTagService {

    private final CustomerTagRepository customerTagRepository;

    @Transactional(readOnly = true)
    public Page<CustomerTagResponse> findAll(Pageable pageable) {
        return customerTagRepository.findAll(pageable)
                .map(this::toResponse);
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
