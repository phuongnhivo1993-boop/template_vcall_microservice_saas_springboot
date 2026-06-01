package com.vcall.customer.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.customer.dto.CustomerAddressRequest;
import com.vcall.customer.entity.Customer;
import com.vcall.customer.entity.CustomerAddress;
import com.vcall.customer.repository.CustomerAddressRepository;
import com.vcall.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerAddressService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    public CustomerAddress addAddress(UUID customerId, CustomerAddressRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setAddressType(CustomerAddress.AddressType.valueOf(request.getAddressType()));
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setPrimary(request.isPrimary());

        if (request.isPrimary()) {
            customer.getAddresses().forEach(a -> a.setPrimary(false));
        }

        return customerAddressRepository.save(address);
    }

    public CustomerAddress updateAddress(Long addressId, CustomerAddressRequest request) {
        CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        address.setAddressType(CustomerAddress.AddressType.valueOf(request.getAddressType()));
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setPrimary(request.isPrimary());

        if (request.isPrimary()) {
            address.getCustomer().getAddresses().forEach(a -> a.setPrimary(false));
            address.setPrimary(true);
        }

        return customerAddressRepository.save(address);
    }

    public void removeAddress(Long addressId) {
        CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        customerAddressRepository.delete(address);
    }
}
