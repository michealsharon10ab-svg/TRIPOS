package com.bughunters.pos.config;

import com.bughunters.pos.administration.entity.*;
import com.bughunters.pos.administration.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final TenantRepository tenantRepository;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final TerminalRepository terminalRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            TenantRepository tenantRepository,
            BranchRepository branchRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            TerminalRepository terminalRepository,
            PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.branchRepository = branchRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.terminalRepository = terminalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (tenantRepository.count() > 0) {
            logger.info("Database already seeded. Skipping initial seeding.");
            return;
        }

        logger.info("Seeding initial Bug Hunters Unified POS enterprise records...");

        // 1. Seed Tenant
        Tenant tenant = new Tenant("LLG", "Lanka Leisure Group", "Hospitality, Restaurant, and Retail Enterprise");
        tenant = tenantRepository.save(tenant);

        // 2. Seed Branches
        Branch branchHotel = new Branch(tenant, "COLOMBO-CITY", "Colombo City Hotel", "Galle Road, Colombo 03", "+94 11 234 5678", BranchType.HOTEL);
        Branch branchDine = new Branch(tenant, "ROYAL-DINE", "Royal Ocean Dine", "Marine Drive, Bambalapitiya", "+94 11 234 5679", BranchType.RESTAURANT);
        Branch branchMart = new Branch(tenant, "CENTRAL-MART", "Lanka Central Retail Mart", "Main Street, Pettah", "+94 11 234 5680", BranchType.RETAIL);

        branchHotel = branchRepository.save(branchHotel);
        branchDine = branchRepository.save(branchDine);
        branchMart = branchRepository.save(branchMart);

        // 3. Seed Permissions
        List<Permission> permissions = List.of(
                new Permission("USER_READ", "ADMINISTRATION", "View staff accounts and details"),
                new Permission("USER_CREATE", "ADMINISTRATION", "Create new staff accounts"),
                new Permission("USER_UPDATE", "ADMINISTRATION", "Modify staff profiles and role assignments"),
                new Permission("USER_DISABLE", "ADMINISTRATION", "Activate or deactivate staff accounts"),
                new Permission("ROLE_MANAGE", "ADMINISTRATION", "Manage security roles and permissions"),
                new Permission("BRANCH_READ", "BRANCH", "Access branch profiles and statuses"),
                new Permission("SALES_CREATE", "SALES", "Create sales orders and process checkout payments"),
                new Permission("SALES_READ", "SALES", "View sales records and daily receipts"),
                new Permission("INVENTORY_READ", "INVENTORY", "View stock levels and catalog items"),
                new Permission("INVENTORY_MANAGE", "INVENTORY", "Adjust stock, receive purchase orders, and edit products"),
                new Permission("RESTAURANT_ORDER_CREATE", "RESTAURANT", "Take table orders and dispatch kitchen order tickets"),
                new Permission("KITCHEN_ORDER_UPDATE", "RESTAURANT", "Update kitchen ticket status on KDS screen"),
                new Permission("HOTEL_RESERVATION_MANAGE", "HOTEL", "Create and modify hotel room bookings and check-in folios"),
                new Permission("REPORT_VIEW", "REPORTING", "Access executive, sales, and inventory analytics reports")
        );
        permissionRepository.saveAll(permissions);

        Set<Permission> allPerms = new HashSet<>(permissions);

        // 4. Seed Roles
        Role superAdminRole = new Role("ROLE_SUPER_ADMIN", "Platform Super Administrator with system-wide access");
        superAdminRole.setPermissions(allPerms);

        Role tenantAdminRole = new Role("ROLE_TENANT_ADMIN", "Tenant Enterprise Administrator");
        Set<Permission> tenantPerms = new HashSet<>(allPerms);
        tenantPerms.removeIf(p -> "ROLE_MANAGE".equals(p.getName()));
        tenantAdminRole.setPermissions(tenantPerms);

        Role branchManagerRole = new Role("ROLE_BRANCH_MANAGER", "Branch Operations Manager");
        Set<Permission> managerPerms = new HashSet<>();
        allPerms.forEach(p -> {
            if (!"USER_CREATE".equals(p.getName()) && !"USER_DISABLE".equals(p.getName()) && !"ROLE_MANAGE".equals(p.getName())) {
                managerPerms.add(p);
            }
        });
        branchManagerRole.setPermissions(managerPerms);

        Role cashierRole = new Role("ROLE_CASHIER", "Front Counter POS Cashier");
        Set<Permission> cashierPerms = new HashSet<>();
        allPerms.forEach(p -> {
            if ("SALES_CREATE".equals(p.getName()) || "SALES_READ".equals(p.getName()) || "INVENTORY_READ".equals(p.getName()) || "BRANCH_READ".equals(p.getName())) {
                cashierPerms.add(p);
            }
        });
        cashierRole.setPermissions(cashierPerms);

        roleRepository.saveAll(List.of(superAdminRole, tenantAdminRole, branchManagerRole, cashierRole));

        // 5. Seed Users
        // A) Super Admin
        User superAdmin = new User();
        superAdmin.setUsername("superadmin");
        superAdmin.setEmail("admin@bughunters.pos");
        superAdmin.setFullName("Platform Super Administrator");
        superAdmin.setPassword(passwordEncoder.encode("Admin@12345"));
        superAdmin.setActive(true);
        superAdmin.setRoles(Set.of(superAdminRole));
        superAdmin.setBranches(Set.of(branchHotel, branchDine, branchMart));
        userRepository.save(superAdmin);

        // B) Tenant Admin (matching screenshot email: michealsharon.10ab@gmail.com)
        User tenantAdmin = new User();
        tenantAdmin.setUsername("tenantadmin");
        tenantAdmin.setEmail("michealsharon.10ab@gmail.com");
        tenantAdmin.setFullName("Micheal Sharon");
        tenantAdmin.setPhone("+94 77 200 0002");
        tenantAdmin.setPassword(passwordEncoder.encode("Tenant@12345"));
        tenantAdmin.setActive(true);
        tenantAdmin.setTenant(tenant);
        tenantAdmin.setRoles(Set.of(tenantAdminRole));
        tenantAdmin.setBranches(Set.of(branchHotel, branchDine, branchMart));
        userRepository.save(tenantAdmin);

        // C) Branch Manager
        User manager = new User();
        manager.setUsername("manager01");
        manager.setEmail("manager@lankaleisure.com");
        manager.setFullName("Branch Manager - Colombo");
        manager.setPhone("+94 77 300 0003");
        manager.setPassword(passwordEncoder.encode("Manager@12345"));
        manager.setPin(passwordEncoder.encode("1234"));
        manager.setActive(true);
        manager.setTenant(tenant);
        manager.setRoles(Set.of(branchManagerRole));
        manager.setBranches(Set.of(branchHotel));
        userRepository.save(manager);

        // D) POS Cashier
        User cashier = new User();
        cashier.setUsername("cashier01");
        cashier.setEmail("cashier01@lankaleisure.com");
        cashier.setFullName("Countertop Cashier 01");
        cashier.setPhone("+94 77 400 0004");
        cashier.setPassword(passwordEncoder.encode("Cashier@12345"));
        cashier.setPin(passwordEncoder.encode("5678"));
        cashier.setActive(true);
        cashier.setTenant(tenant);
        cashier.setRoles(Set.of(cashierRole));
        cashier.setBranches(Set.of(branchHotel));
        userRepository.save(cashier);

        // 6. Seed POS Terminal (matching screenshot: Terminal 2 - Colombo City Hotel)
        Terminal terminal1 = new Terminal(tenant, branchHotel, "TERM-001", "Terminal 1 - Front Counter");
        Terminal terminal2 = new Terminal(tenant, branchHotel, "TERM-002", "Terminal 2 - Colombo City Hotel");
        terminalRepository.saveAll(List.of(terminal1, terminal2));

        logger.info("Successfully seeded default users, roles, permissions, branches, and terminal.");
    }
}
