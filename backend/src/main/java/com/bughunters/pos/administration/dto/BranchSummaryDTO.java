package com.bughunters.pos.administration.dto;

import com.bughunters.pos.administration.entity.Branch;
import com.bughunters.pos.administration.entity.BranchType;

public class BranchSummaryDTO {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private BranchType branchType;

    public BranchSummaryDTO() {}

    public BranchSummaryDTO(Branch branch) {
        if (branch != null) {
            this.id = branch.getId();
            this.code = branch.getCode();
            this.name = branch.getName();
            this.address = branch.getAddress();
            this.phone = branch.getPhone();
            this.branchType = branch.getBranchType();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public BranchType getBranchType() { return branchType; }
    public void setBranchType(BranchType branchType) { this.branchType = branchType; }
}
