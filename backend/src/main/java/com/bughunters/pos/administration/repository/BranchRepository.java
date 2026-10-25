package com.bughunters.pos.administration.repository;

import com.bughunters.pos.administration.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByTenantIdAndActiveTrue(Long tenantId);
    List<Branch> findByTenantId(Long tenantId);
    Optional<Branch> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Branch> findByCodeAndTenantId(String code, Long tenantId);
    boolean existsByCodeAndTenantId(String code, Long tenantId);
}
