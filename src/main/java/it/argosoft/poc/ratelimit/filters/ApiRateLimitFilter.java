package it.argosoft.poc.ratelimit.filters;

import io.vertx.core.http.HttpServerRequest;
import it.argosoft.poc.ratelimit.exception.RateLimitExcededException;
import it.argosoft.poc.ratelimit.principal.UserPrincipal;
import it.argosoft.poc.ratelimit.service.TokenBucketRateLimitingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.time.Duration;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiRateLimitFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRateLimitFilter.class);

    @Context
    HttpServerRequest request;

    @Context
    SecurityContext securityContext;

    @Inject
    TokenBucketRateLimitingService rateLimitingService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        log.debug("Subject: {}", principal.getName());
        if (principal instanceof UserPrincipal) {
            log.debug("found UserPrincipal {} checking rate limit ...", principal.getName());
            boolean allow = rateLimitingService.shouldAllow(principal.getName());
            if (!allow) {
                log.debug("Deny request for user {} because rate limit exceded", principal.getName());
                throw new RateLimitExcededException();
            }
            log.debug("Allow request for user {}", principal.getName());
        } else {
            log.debug("ignore rate limiting on principal of type {}({})", principal.getClass(), principal.getName());
        }
    }
}
