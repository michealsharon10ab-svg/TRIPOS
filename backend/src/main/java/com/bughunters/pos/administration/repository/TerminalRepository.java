package com.bughunters.pos.administration.repository;

import com.bughunters.pos.administration.entity.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {
    Optional<Terminal> findByTenantCodeAndTerminalCode(String tenantCode, String terminalCode);
    Optional<Terminal> findByTerminalCodeAndTenantId(String terminalCode, Long tenantId);
}
