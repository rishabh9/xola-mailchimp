package utils;

import play.Logger;
import play.api.http.MediaRange;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * <p>Play Action Wrapper for Acces Logging.</p>
 * <p>Add {@link play.mvc.With} annotation to every controller you want access logging enabled. Example:</p>
 * <p>
 * {@code @With(AccessLoggingAction.class)} <br/>
 * {@code public class CustomController extends Controller {}}
 * </p>
 *
 * @author rishabh
 */
public class AccessLoggingAction extends Action.Simple {

    private Logger.ALogger accessLogger = Logger.of("access");

    public CompletionStage<Result> call(Http.Context ctx) {
        final Request request = new Request(ctx.request());

        if (accessLogger.isInfoEnabled()) {
            accessLogger.info("{}", Json.toJson(request));
        }
        return delegate.call(ctx);
    }
}

class Request {

    private String method;
    private String uri;
    private String remoteAddress;
    private String body;
    private String path;
    private List<String> acceptedTypes;
    private List<String> acceptLanguages;
    private String charset;
    private String contentType;
    private List<Cookie> cookies;
    private Map<String, String[]> headers;
    private String host;
    private Map<String, String[]> queryString;
    private boolean secure;
    private Map<String, String> tags;
    private String version;
    private String username;

    Request(Http.Request request) {
        method = request.method();
        uri = request.uri();
        remoteAddress = request.remoteAddress();
        body = request.body().asText();
        path = request.path();
        acceptedTypes = acceptedTypes(request.acceptedTypes());
        acceptLanguages = acceptLanguages(request.acceptLanguages());
        charset = request.charset().isPresent() ? request.charset().get() : "";
        contentType = request.contentType().isPresent() ? request.contentType().get() : "";
        cookies = cookies(request.cookies());
        headers = request.headers();
        host = request.host();
        queryString = request.queryString();
        secure = request.secure();
        tags = request.tags();
        version = request.version();
        username = request.username();
    }

    private List<String> acceptedTypes(List<MediaRange> acceptedTypes) {
        List<String> media = new ArrayList<>(acceptedTypes.size());
        acceptedTypes.forEach(mediaRange -> media.add(mediaRange.mediaType()));
        return media;
    }

    private List<String> acceptLanguages(List<Lang> acceptLanguages) {
        List<String> langs = new ArrayList<>(acceptLanguages.size());
        acceptLanguages.forEach(lang -> langs.add(lang.toLocale().getDisplayName()));
        return langs;
    }

    private List<Cookie> cookies(Http.Cookies cookies) {
        List<Cookie> cookieStrs = new ArrayList<>();
        cookies.forEach(cookie -> cookieStrs.add(Cookie.newBuilder().maxAge(cookie.maxAge()).domain(cookie.domain())
                .httpOnly(cookie.httpOnly()).name(cookie.name()).path(cookie.path()).secure(cookie.secure())
                .value(cookie.value())
                .build()));
        return cookieStrs;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    public List<String> getAcceptedTypes() {
        return acceptedTypes;
    }

    public List<String> getAcceptLanguages() {
        return acceptLanguages;
    }

    public String getCharset() {
        return charset;
    }

    public String getContentType() {
        return contentType;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public Map<String, String[]> getHeaders() {
        return headers;
    }

    public String getHost() {
        return host;
    }

    public Map<String, String[]> getQueryString() {
        return queryString;
    }

    public boolean isSecure() {
        return secure;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getVersion() {
        return version;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return secure == request.secure &&
                Objects.equals(method, request.method) &&
                Objects.equals(uri, request.uri) &&
                Objects.equals(remoteAddress, request.remoteAddress) &&
                Objects.equals(body, request.body) &&
                Objects.equals(path, request.path) &&
                Objects.equals(acceptedTypes, request.acceptedTypes) &&
                Objects.equals(acceptLanguages, request.acceptLanguages) &&
                Objects.equals(charset, request.charset) &&
                Objects.equals(contentType, request.contentType) &&
                Objects.equals(cookies, request.cookies) &&
                Objects.equals(headers, request.headers) &&
                Objects.equals(host, request.host) &&
                Objects.equals(queryString, request.queryString) &&
                Objects.equals(tags, request.tags) &&
                Objects.equals(version, request.version) &&
                Objects.equals(username, request.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, uri, remoteAddress, body, path, acceptedTypes, acceptLanguages, charset,
                contentType, cookies, headers, host, queryString, secure, tags, version, username);
    }
}

class Cookie {
    private int maxAge;
    private String domain;
    private boolean httpOnly;
    private String name;
    private String path;
    private boolean secure;
    private String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cookie cookie = (Cookie) o;
        return maxAge == cookie.maxAge &&
                httpOnly == cookie.httpOnly &&
                secure == cookie.secure &&
                Objects.equals(domain, cookie.domain) &&
                Objects.equals(name, cookie.name) &&
                Objects.equals(path, cookie.path) &&
                Objects.equals(value, cookie.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxAge, domain, httpOnly, name, path, secure, value);
    }

    private Cookie(Builder builder) {
        maxAge = builder.maxAge;
        domain = builder.domain;
        httpOnly = builder.httpOnly;
        name = builder.name;
        path = builder.path;
        secure = builder.secure;
        value = builder.value;
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static final class Builder {
        private int maxAge;
        private String domain;
        private boolean httpOnly;
        private String name;
        private String path;
        private boolean secure;
        private String value;

        private Builder() {
        }

        Builder maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        Builder httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        Builder name(String name) {
            this.name = name;
            return this;
        }

        Builder path(String path) {
            this.path = path;
            return this;
        }

        Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        Builder value(String value) {
            this.value = value;
            return this;
        }

        Cookie build() {
            return new Cookie(this);
        }
    }
}
