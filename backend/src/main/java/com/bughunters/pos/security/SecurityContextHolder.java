package com.bughunters.pos.security;

/**
 * ThreadLocal-based holder that binds a {@link SecurityContext} to the current
 * request thread.
 *
 * <p><strong>PLACEHOLDER — TO BE REPLACED BY MEMBER 1 (SHARON).</strong>
 * In production, Sharon's JWT filter will call {@link #set(SecurityContext)}
 * at the beginning of every request and {@link #clear()} in an
 * {@code OncePerRequestFilter} finally-block.
 *
 * <p>In unit tests, call {@link #set(SecurityContext)} with a
 * {@link com.bughunters.pos.security.TestSecurityContext} to inject any
 * userId / branchId without spinning up Spring Security.
 */
public class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> HOLDER = new ThreadLocal<>();

    private SecurityContextHolder() {
        // utility class
    }

    /** Binds {@code ctx} to the current thread. */
    public static void set(SecurityContext ctx) {
        HOLDER.set(ctx);
    }

    /**
     * Returns the current context.
     *
     * @throws IllegalStateException if no context has been set (unauthenticated thread)
     */
    public static SecurityContext get() {
        SecurityContext ctx = HOLDER.get();
        if (ctx == null) {
            throw new IllegalStateException(
                "No SecurityContext bound to the current thread. "
                + "Ensure the JWT filter (or test stub) has called SecurityContextHolder.set().");
        }
        return ctx;
    }

    /** Removes the context from the current thread (call in filter finally-block). */
    public static void clear() {
        HOLDER.remove();
    }
}
