package it.argosoft.poc.ratelimit.principal;

import org.wildfly.common.Assert;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

public class ServicePrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 5337757875499015306L;
    private final String name;
    public ServicePrincipal(String subject) {
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
        ServicePrincipal that = (ServicePrincipal) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
