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

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author rishabh
 */
public class ErrorHandler extends DefaultHttpErrorHandler {

    private final Logger.ALogger log = Logger.of(ErrorHandler.class);

    @Inject
    public ErrorHandler(Configuration configuration, Environment environment,
                        OptionalSourceMapper sourceMapper, Provider<Router> routes) {
        super(configuration, environment, sourceMapper, routes);
    }

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(Results.badRequest(views.html.errors.render(message)));
    }

    @Override
    protected CompletionStage<Result> onBadRequest(Http.RequestHeader request, String message) {
        return CompletableFuture.completedFuture(Results.badRequest(views.html.errors.render(message)));
    }

    @Override
    protected CompletionStage<Result> onForbidden(Http.RequestHeader request, String message) {
        return CompletableFuture.completedFuture(Results.forbidden(views.html.errors.render(message)));
    }

    @Override
    protected CompletionStage<Result> onNotFound(Http.RequestHeader request, String message) {
        return CompletableFuture.completedFuture(Results.notFound(views.html.errors.render(message)));
    }

    @Override
    protected CompletionStage<Result> onOtherClientError(Http.RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(Results.badRequest(views.html.errors.render(message)));
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        return CompletableFuture.completedFuture(Results.internalServerError(views.html.errors.render(exception.getMessage())));
    }

    @Override
    protected void logServerError(Http.RequestHeader request, UsefulException usefulException) {
        super.logServerError(request, usefulException);
    }

    @Override
    protected CompletionStage<Result> onDevServerError(Http.RequestHeader request, UsefulException exception) {
        return super.onDevServerError(request, exception);
    }

    @Override
    protected CompletionStage<Result> onProdServerError(Http.RequestHeader request, UsefulException exception) {
        return CompletableFuture.completedFuture(Results.internalServerError(views.html.errors.render(exception.getMessage())));
    }
}
