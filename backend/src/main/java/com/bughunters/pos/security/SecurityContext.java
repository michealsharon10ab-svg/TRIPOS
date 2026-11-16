package com.bughunters.pos.security;

/**
 * Placeholder interface representing the current authenticated principal's
 * identity in the context of the POS system.
 *
 * <p><strong>PLACEHOLDER — TO BE REPLACED BY MEMBER 1 (SHARON).</strong>
 * Once the JWT filter chain is wired, a real implementation backed by the
 * JWT claims will replace the test stub in production code.
 *
 * <p>All hotel-module services depend on this interface (not the stub), so
 * they will work without change once the real implementation is registered
 * as a Spring bean.
 */
public interface SecurityContext {

    /**
     * Returns the ID of the currently authenticated staff member.
     *
     * @return user / staff ID; never {@code null} in a secured context
     */
    Long getCurrentUserId();

    /**
     * Returns the branch ID that the current user is acting under.
     * <p>Every query in the hotel module is scoped by this value — users
     * must never see another branch's data (Business Rule 6).
     *
     * @return branch ID; never {@code null} in a secured context
     */
    Long getCurrentBranchId();
}
