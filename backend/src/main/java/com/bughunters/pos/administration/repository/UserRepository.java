package com.bughunters.pos.administration.repository;

import com.bughunters.pos.administration.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmailWithRolesAndPermissions(@Param("identifier") String identifier);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Tenant-isolated queries
    Page<User> findByTenantId(Long tenantId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchByTenantId(@Param("tenantId") Long tenantId, @Param("query") String query, Pageable pageable);

    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    // PIN lookup scoped to branch and tenant
    @Query("SELECT u FROM User u JOIN u.branches b WHERE u.tenant.id = :tenantId AND b.id = :branchId AND u.active = true")
    List<User> findActiveUsersByTenantAndBranch(@Param("tenantId") Long tenantId, @Param("branchId") Long branchId);
}
