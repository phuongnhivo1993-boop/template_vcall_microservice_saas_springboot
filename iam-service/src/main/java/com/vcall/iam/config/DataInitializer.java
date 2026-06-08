package com.vcall.iam.config;

import com.vcall.iam.entity.Role;
import com.vcall.iam.entity.RoleName;
import com.vcall.iam.entity.User;
import com.vcall.iam.entity.UserRole;
import com.vcall.iam.entity.UserStatus;
import com.vcall.iam.repository.RoleRepository;
import com.vcall.iam.repository.UserRepository;
import com.vcall.iam.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-admin-password:Admin@12345}")
    private String defaultAdminPassword;

    @Value("${app.default-supervisor-password:Sup@12345}")
    private String defaultSupervisorPassword;

    @Value("${app.default-agent-password:Agent@12345}")
    private String defaultAgentPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedDefaultAccounts();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(roleName.name())
                        .build());
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    private void seedDefaultAccounts() {
        List<DefaultAccount> defaults = List.of(
                new DefaultAccount("admin", defaultAdminPassword, "Super Administrator", Set.of(RoleName.SUPER_ADMIN)),
                new DefaultAccount("supervisor", defaultSupervisorPassword, "Supervisor", Set.of(RoleName.SUPERVISOR)),
                new DefaultAccount("agent", defaultAgentPassword, "Agent", Set.of(RoleName.AGENT))
        );

        for (DefaultAccount acc : defaults) {
            User user = userRepository.findByUsername(acc.username).orElse(null);

            if (user == null) {
                user = User.builder()
                        .username(acc.username)
                        .password(passwordEncoder.encode(acc.password))
                        .fullName(acc.fullName)
                        .status(UserStatus.ACTIVE)
                        .build();
                user = userRepository.save(user);
                log.info("Created default user: {}", acc.username);
            }

            for (RoleName roleName : acc.roles) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                boolean alreadyAssigned = userRoleRepository.findByUserIdAndRoleId(user.getId(), role.getId())
                        .isPresent();
                if (!alreadyAssigned) {
                    UserRole userRole = UserRole.builder()
                            .user(user)
                            .role(role)
                            .build();
                    userRoleRepository.save(userRole);
                    log.info("Assigned role {} to user {}", roleName, acc.username);
                }
            }
        }

        log.info("Default accounts verified successfully");
    }

    private record DefaultAccount(String username, String password, String fullName, Set<RoleName> roles) {}
}
