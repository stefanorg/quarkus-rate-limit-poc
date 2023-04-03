package it.argosoft.poc.ratelimit.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RateLimitExcededExceptionMapper implements ExceptionMapper<RateLimitExcededException> {
    @Override
    public Response toResponse(RateLimitExcededException exception) {
        return Response
                .status(Response.Status.TOO_MANY_REQUESTS)
                .build();
    }
}
