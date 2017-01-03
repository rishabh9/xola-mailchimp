package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.AccessLoggingAction;
import utils.Constants;

/**
 * @author rishabh
 */
@With(AccessLoggingAction.class)
public class HomeController extends Controller {

    public Result index() {
        return ok(Constants.UP_AND_RUNNING);
    }
}
