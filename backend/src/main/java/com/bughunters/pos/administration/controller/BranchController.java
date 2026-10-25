package com.bughunters.pos.administration.controller;

import com.bughunters.pos.administration.dto.BranchSummaryDTO;
import com.bughunters.pos.administration.service.BranchService;
import com.bughunters.pos.common.response.ApiResponse;
import com.bughunters.pos.config.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BRANCH_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<BranchSummaryDTO>>> getBranches(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<BranchSummaryDTO> branches = branchService.getBranchesForCurrentUser(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Branches retrieved successfully", branches));
    }
}
