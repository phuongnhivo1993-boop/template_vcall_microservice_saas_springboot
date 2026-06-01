package com.vcall.customer.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.customer.dto.CustomerContactRequest;
import com.vcall.customer.entity.Customer;
import com.vcall.customer.entity.CustomerContact;
import com.vcall.customer.repository.CustomerContactRepository;
import com.vcall.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerContactService {

    private final CustomerRepository customerRepository;
    private final CustomerContactRepository customerContactRepository;

    public CustomerContact addContact(UUID customerId, CustomerContactRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        CustomerContact contact = new CustomerContact();
        contact.setCustomer(customer);
        contact.setContactType(CustomerContact.ContactType.valueOf(request.getContactType()));
        contact.setContactValue(request.getContactValue());
        contact.setPrimary(request.isPrimary());

        if (request.isPrimary()) {
            customer.getContacts().forEach(c -> c.setPrimary(false));
        }

        return customerContactRepository.save(contact);
    }

    public CustomerContact updateContact(Long contactId, CustomerContactRequest request) {
        CustomerContact contact = customerContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contactId));

        contact.setContactType(CustomerContact.ContactType.valueOf(request.getContactType()));
        contact.setContactValue(request.getContactValue());
        contact.setPrimary(request.isPrimary());

        if (request.isPrimary()) {
            contact.getCustomer().getContacts().forEach(c -> c.setPrimary(false));
            contact.setPrimary(true);
        }

        return customerContactRepository.save(contact);
    }

    public void removeContact(Long contactId) {
        CustomerContact contact = customerContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contactId));
        customerContactRepository.delete(contact);
    }

    public void setPrimary(Long contactId) {
        CustomerContact contact = customerContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contactId));
        contact.getCustomer().getContacts().forEach(c -> c.setPrimary(false));
        contact.setPrimary(true);
        customerContactRepository.save(contact);
    }
}
