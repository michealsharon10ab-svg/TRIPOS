package com.bughunters.pos.administration.service;

import com.bughunters.pos.administration.dto.BranchSummaryDTO;
import com.bughunters.pos.administration.entity.Branch;
import com.bughunters.pos.administration.entity.Tenant;
import com.bughunters.pos.administration.repository.BranchRepository;
import com.bughunters.pos.administration.repository.TenantRepository;
import com.bughunters.pos.common.exception.ResourceNotFoundException;
import com.bughunters.pos.config.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;

    public BranchService(BranchRepository branchRepository, TenantRepository tenantRepository) {
        this.branchRepository = branchRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public List<BranchSummaryDTO> getBranchesForCurrentUser(UserPrincipal currentUser) {
        if (currentUser.isSuperAdmin() && currentUser.getTenantId() == null) {
            return branchRepository.findAll().stream()
                    .map(BranchSummaryDTO::new)
                    .collect(Collectors.toList());
        }

        Long tenantId = currentUser.getTenantId();
        return branchRepository.findByTenantIdAndActiveTrue(tenantId).stream()
                .map(BranchSummaryDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BranchSummaryDTO> getBranchesByTenantCode(String tenantCode) {
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with code: " + tenantCode));

        return branchRepository.findByTenantIdAndActiveTrue(tenant.getId()).stream()
                .map(BranchSummaryDTO::new)
                .collect(Collectors.toList());
    }
}
