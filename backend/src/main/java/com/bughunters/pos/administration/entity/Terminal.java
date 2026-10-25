package com.bughunters.pos.administration.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "terminals", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "terminal_code"})
})
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "terminal_code", nullable = false, length = 50)
    private String terminalCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt = Instant.now();

    public Terminal() {}

    public Terminal(Tenant tenant, Branch branch, String terminalCode, String name) {
        this.tenant = tenant;
        this.branch = branch;
        this.terminalCode = terminalCode;
        this.name = name;
        this.active = true;
        this.registeredAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }
}
