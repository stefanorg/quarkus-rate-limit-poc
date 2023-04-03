package it.argosoft.poc.ratelimit.principal;

import org.wildfly.common.Assert;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

public class UserPrincipal implements Principal, Serializable {

    private static final long serialVersionUID = -2186968772188120847L;
    private final String name;
    public UserPrincipal(String subject) {
        Assert.checkNotNullParam("subject", subject);
        this.name = subject;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
