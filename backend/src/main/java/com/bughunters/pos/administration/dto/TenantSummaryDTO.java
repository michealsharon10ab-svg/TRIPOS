package com.bughunters.pos.administration.dto;

import com.bughunters.pos.administration.entity.Tenant;

public class TenantSummaryDTO {
    private Long id;
    private String code;
    private String name;
    private String description;

    public TenantSummaryDTO() {}

    public TenantSummaryDTO(Tenant tenant) {
        if (tenant != null) {
            this.id = tenant.getId();
            this.code = tenant.getCode();
            this.name = tenant.getName();
            this.description = tenant.getDescription();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
