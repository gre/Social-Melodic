package controllers;

import play.mvc.Before;
import play.mvc.Controller;

public class Admin extends Controller {
	@Before
	static void globals() {
		// Permissions ...
	}
	
	public static void dashboard() {
		render();
	}
}
