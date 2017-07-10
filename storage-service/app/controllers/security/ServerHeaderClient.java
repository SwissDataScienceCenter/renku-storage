package controllers.security;

import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.http.client.direct.HeaderClient;

public class ServerHeaderClient extends HeaderClient {
    public ServerHeaderClient(final String headerName, final String prefixHeader,
                        final Authenticator tokenAuthenticator) {
        super(headerName, prefixHeader, tokenAuthenticator);
    }
}
