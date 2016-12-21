package utils;

import play.libs.Json;
import play.mvc.Call;
import play.mvc.Http;

import static play.test.Helpers.fakeRequest;

/**
 * @author rishabh
 */
public class TestHelper {

    /**
     * @param action The action to be called.
     * @param input  The request body
     * @param <T>    Any type that is convertible to JSON
     * @return RequestBuilder
     */
    public static <T> Http.RequestBuilder fakeRequestWithBody(Call action, T input) {
        return fakeRequest(action).bodyJson(Json.toJson(input));
    }

    /**
     * @param action          The action to be called.
     * @param queryParameters Query parameters starting with "?"
     * @return RequestBuilder
     */
    public static Http.RequestBuilder fakeRequestWithoutBody(Call action, String queryParameters) {
        return fakeRequest(action).uri(action.url() + queryParameters);
    }

    /**
     * @param action The action to be called.
     * @return RequestBuilder
     */
    public static Http.RequestBuilder fakeRequestWithoutBody(Call action) {
        return fakeRequest(action);
    }
}
