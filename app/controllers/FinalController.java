package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rishabh
 */
public class FinalController extends Controller {

    public Result index(String message) {
        return ok(views.html.last.render(message));
    }
}
