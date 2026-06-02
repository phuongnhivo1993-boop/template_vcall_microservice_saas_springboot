package com.vcall.customer.repository;

import com.vcall.customer.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTests {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        customer1 = new Customer();
        customer1.setId(UUID.randomUUID());
        customer1.setCustomerCode("CUS-001");
        customer1.setFullName("John Doe");
        customer1.setEmail("john@example.com");
        customer1.setPhone("1234567890");
        customer1.setGender("male");
        customer1.setCompany("Acme Corp");
        customer1.setNationality("US");
        customer1.setIsDeleted(false);
        customerRepository.save(customer1);

        customer2 = new Customer();
        customer2.setId(UUID.randomUUID());
        customer2.setCustomerCode("CUS-002");
        customer2.setFullName("Jane Smith");
        customer2.setEmail("jane@example.com");
        customer2.setPhone("9876543210");
        customer2.setGender("female");
        customer2.setCompany("Globex Inc");
        customer2.setNationality("UK");
        customer2.setIsDeleted(false);
        customerRepository.save(customer2);

        entityManager.flush();
    }

    @Test
    void testFindAll() {
        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(2);
    }

    @Test
    void testFindById() {
        Optional<Customer> found = customerRepository.findById(customer1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByEmail() {
        Optional<Customer> found = customerRepository.findByEmail("john@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByPhone() {
        Optional<Customer> found = customerRepository.findByPhone("1234567890");
        assertThat(found).isPresent();
    }

    @Test
    void testExistsByEmail() {
        assertThat(customerRepository.existsByEmail("john@example.com")).isTrue();
        assertThat(customerRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    void testExistsByPhone() {
        assertThat(customerRepository.existsByPhone("1234567890")).isTrue();
        assertThat(customerRepository.existsByPhone("0000000000")).isFalse();
    }

    @Test
    void testFindByCustomerCode() {
        Optional<Customer> found = customerRepository.findByCustomerCode("CUS-001");
        assertThat(found).isPresent();
    }

    @Test
    void testDeletionSetsIsDeleted() {
        Customer customer = customerRepository.findById(customer1.getId()).orElseThrow();
        customer.setIsDeleted(true);
        customerRepository.save(customer);
        entityManager.flush();

        Optional<Customer> found = customerRepository.findById(customer1.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testSpecificationWithKeyword() {
        Page<Customer> result = customerRepository.findAll((root, query, cb) -> {
            String pattern = "%john%";
            return cb.like(cb.lower(root.get("fullName")), pattern);
        }, PageRequest.of(0, 10, Sort.by("createdAt").descending()));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe");
    }

    @Test
    void testSpecificationWithGenderFilter() {
        Page<Customer> result = customerRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("gender"), "female"));
            return cb.and(predicates.toArray(new Predicate[0]));
        }, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getGender()).isEqualTo("female");
    }

    @Test
    void testSpecificationWithCompanyFilter() {
        Page<Customer> result = customerRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("company")), "%acme%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        }, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCompany()).isEqualTo("Acme Corp");
    }

    @Test
    void testPagination() {
        Page<Customer> page1 = customerRepository.findAll(PageRequest.of(0, 1, Sort.by("fullName").ascending()));
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testSorting() {
        Page<Customer> result = customerRepository.findAll(PageRequest.of(0, 10, Sort.by("fullName").descending()));
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe");
    }
}
