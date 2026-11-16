package com.bughunters.pos.security;

/**
 * Test-only implementation of {@link SecurityContext}.
 *
 * <p><strong>NOT for production use.</strong> Use in unit and integration
 * tests to inject a known userId / branchId without a real JWT:
 *
 * <pre>{@code
 *   SecurityContextHolder.set(new TestSecurityContext(1L, 10L));
 *   // ... call service ...
 *   SecurityContextHolder.clear();
 * }</pre>
 *
 * <p>PLACEHOLDER — will be superseded by Member 1 (Sharon)'s JWT-backed
 * implementation once the Auth module is wired.
 */
public class TestSecurityContext implements SecurityContext {

    private final Long userId;
    private final Long branchId;

    public TestSecurityContext(Long userId, Long branchId) {
        this.userId   = userId;
        this.branchId = branchId;
    }

    @Override
    public Long getCurrentUserId() {
        return userId;
    }

    @Override
    public Long getCurrentBranchId() {
        return branchId;
    }
}
