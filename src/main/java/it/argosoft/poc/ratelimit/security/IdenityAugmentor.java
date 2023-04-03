package it.argosoft.poc.ratelimit.security;

import io.quarkus.elytron.security.oauth2.runtime.auth.ElytronOAuth2CallerPrincipal;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import it.argosoft.poc.ratelimit.principal.ServicePrincipal;
import it.argosoft.poc.ratelimit.principal.UserPrincipal;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class IdenityAugmentor implements SecurityIdentityAugmentor {

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext authenticationRequestContext) {

        // use a fixed principal just to provide a reproducer to github issue
        return Uni.createFrom().item(() -> {
            QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity
                    .builder()
                    // di default lo considero un ServicePrincipal "b2b"
                    .setPrincipal(new UserPrincipal("test-rate-limit"))
                    .addCredentials(identity.getCredentials());
            return builder.build();
        });

//        if (identity.isAnonymous()) {
//            return Uni.createFrom().item(identity);
//        }
//
//        return Uni.createFrom().item(() -> {
//            ElytronOAuth2CallerPrincipal principal = (ElytronOAuth2CallerPrincipal) identity.getPrincipal();
//            Map<String, Object> claims = principal.getClaims();
//
//            QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity
//                    .builder()
//                    // di default lo considero un ServicePrincipal "b2b"
//                    .setPrincipal(new ServicePrincipal((String) claims.get("sub")))
//                    .addCredentials(identity.getCredentials());
//
//            // se è presente il claim "ext", il token è stato rilasciato ad un utente argo
//            if (claims.get("ext") != null) {
//
//                builder
//                        .setPrincipal(new UserPrincipal((String) claims.get("sub")));
//
//                // TODO: è incompleta, bisogna settare ruoli scope ecc
//                // ma per la POC non sono necessari
//            }
//
//            return builder.build();
//        });
    }
}
