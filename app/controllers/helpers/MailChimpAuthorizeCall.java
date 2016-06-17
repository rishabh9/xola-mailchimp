package controllers.helpers;

import play.Configuration;
import play.mvc.Call;

import javax.inject.Inject;

/**
 * @author rishabh
 */
public class MailChimpAuthorizeCall extends Call {

    private static final String AUTHORIZE_URL = "mailchimp.authorize.url";
    private static final String RESPONSE_TYPE = "mailchimp.response.type";
    private static final String CLIENT_ID = "mailchimp.client.id";
    private static final String REDIRECT_URI = "mailchimp.redirect.uri";

    private final Configuration configuration;

    private String id;

    public void setId(final String id) {
        this.id = id;
    }

    @Inject
    public MailChimpAuthorizeCall(final Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public String url() {
        final StringBuilder url = new StringBuilder(300);
        url.append(configuration.getString(AUTHORIZE_URL));
        url.append('?').append("response_type=").append(configuration.getString(RESPONSE_TYPE));
        url.append('&').append("client_id=").append(configuration.getString(CLIENT_ID));
        url.append('&').append("redirect_uri=").append(configuration.getString(REDIRECT_URI));
        url.append('?').append("confirm=").append(id);
        return url.toString();
//https://login.mailchimp.com/oauth2/authorize?response_type=code&client_id=889667269727&redirect_uri=http://requestb.in/z2bqmdz2
    }

    @Override
    public String method() {
        return "POST";
    }

    @Override
    public String fragment() {
        return "";
    }

}
