package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;

/**
 * @author rishabh
 */
public class HomeController extends Controller {

    public Result index() {
        return ok(Constants.UP_AND_RUNNING);
    }
}
