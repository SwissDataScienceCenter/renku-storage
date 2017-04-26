package modules;

import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.http.client.direct.ParameterClient;

/**
  * Created by jeberle on 26.04.17.
  */
class TokenParameterClient extends ParameterClient {

    TokenParameterClient(final String parameterName, final Authenticator tokenAuthenticator){
        super(parameterName, tokenAuthenticator);
    }
}
