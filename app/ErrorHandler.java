import com.fasterxml.jackson.databind.JsonMappingException;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import utils.Errors;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Http.Status.*;

/**
 * @author rishabh
 */
public class ErrorHandler extends DefaultHttpErrorHandler {

    private static final String UNEXPECTED_ERROR = "An unexpected error has occurred!";
    private static final String JSON_ERROR = "Please ensure the request has a valid JSON.";
    private static final String INVALID_REQUEST = "Unable to process your request. Please verify your request and try again.";

    private final Logger.ALogger log = Logger.of(ErrorHandler.class);

    @Inject
    public ErrorHandler(Configuration configuration, Environment environment,
                        OptionalSourceMapper sourceMapper, Provider<Router> routes) {
        super(configuration, environment, sourceMapper, routes);
    }

    @Override
    protected CompletionStage<Result> onDevServerError(Http.RequestHeader request, UsefulException exception) {
        log.warn(exception.getMessage(), exception);
        if (exception.getCause() instanceof JsonMappingException) {
            return CompletableFuture.completedFuture(Results.badRequest(Errors.toJson(BAD_REQUEST, JSON_ERROR)));
        } else {
            return CompletableFuture.completedFuture(
                    Results.internalServerError(Errors.toJson(INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR)));
        }
    }

    @Override
    protected CompletionStage<Result> onProdServerError(Http.RequestHeader request, UsefulException exception) {
        log.warn(exception.getMessage(), exception);
        if (exception.getCause() instanceof JsonMappingException) {
            return CompletableFuture.completedFuture(Results.badRequest(Errors.toJson(BAD_REQUEST, JSON_ERROR)));
        } else {
            return CompletableFuture.completedFuture(
                    Results.internalServerError(Errors.toJson(INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR)));
        }
    }

    @Override
    protected CompletionStage<Result> onOtherClientError(Http.RequestHeader request, int statusCode, String message) {
        log.warn("Status: {}, Message: {}", statusCode, message);
        return CompletableFuture.completedFuture(Results.status(statusCode, Errors.toJson(statusCode, INVALID_REQUEST)));
    }

    @Override
    protected CompletionStage<Result> onNotFound(Http.RequestHeader request, String message) {
        log.warn(message);
        log.warn("{} - Requested: '{} {} {} {}', From: '{}'", message, request.method(), request.host(),
                request.path(), request.contentType().get(), request.remoteAddress());
        return CompletableFuture.completedFuture(Results.status(NOT_FOUND, Errors.toJson(NOT_FOUND, message)));
    }

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        log.warn("Status: {}, Message: {}", statusCode, message);
        return CompletableFuture.completedFuture(Results.status(statusCode, Errors.toJson(statusCode, INVALID_REQUEST)));
    }

    @Override
    protected CompletionStage<Result> onBadRequest(Http.RequestHeader request, String message) {
        log.warn(message);
        log.warn("{} - Requested: '{} {} {} {}', From: '{}'", message, request.method(), request.host(),
                request.path(), request.contentType().get(), request.remoteAddress());
        return CompletableFuture.completedFuture(Results.status(BAD_REQUEST, Errors.toJson(BAD_REQUEST, message)));
    }

    @Override
    protected CompletionStage<Result> onForbidden(Http.RequestHeader request, String message) {
        log.warn(message);
        log.warn("{} - Requested: '{} {} {} {}', From: '{}'", message, request.method(), request.host(),
                request.path(), request.contentType().get(), request.remoteAddress());
        return CompletableFuture.completedFuture(Results.status(FORBIDDEN, Errors.toJson(FORBIDDEN, message)));
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        log.warn(exception.getMessage(), exception);
        if (exception.getCause() instanceof JsonMappingException) {
            return CompletableFuture.completedFuture(Results.badRequest(Errors.toJson(BAD_REQUEST, JSON_ERROR)));
        } else {
            return CompletableFuture.completedFuture(
                    Results.internalServerError(Errors.toJson(INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR)));
        }
    }

    @Override
    protected void logServerError(Http.RequestHeader request, UsefulException usefulException) {
        // TODO: Add code to log something useful here.
        super.logServerError(request, usefulException);
    }
}
