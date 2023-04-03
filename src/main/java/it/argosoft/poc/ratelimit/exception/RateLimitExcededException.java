package it.argosoft.poc.ratelimit.exception;

import java.io.IOException;

public class RateLimitExcededException extends IOException {
    public RateLimitExcededException() {
    }

    public RateLimitExcededException(String message) {
        super(message);
    }
}
