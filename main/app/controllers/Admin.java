package controllers;

import play.mvc.Before;
import play.mvc.Controller;

public class Admin extends Controller {
	
	@Before
	static void globals() {
		// Permissions ...
	    if(!session.contains("admin")) 
	        Application.adminLogin();
	}
	
	public static void logout() {
	    session.remove("admin");
	    redirect("/");
	}
	
	public static void index() {
		Families.index();
	}
}
