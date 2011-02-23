package controllers;

import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.*;


import models.*;

public class Application extends Controller {

	static LogVoter getVoter() {
		String uuid = session.get("uuid");
		LogVoter voter = null;
		if(uuid!=null) {
			voter = LogVoter.find("byUuid", uuid).first();
		}
		if(voter==null) {
			voter = new LogVoter();
			session.put("uuid", voter.uuid);
			voter.save();
		}
		return voter;
	}
	
    public static void index() {
        render();
    }
    
    public static void random() {
    	Melody melody = Melody.chooseRandom(getVoter());
    	if(melody==null) noMoreMelodies();
    	melody(melody.id);
    }
    
    public static void noMoreMelodies() {
    	// TODO
		render();
	}

	public static void melody(@Required Long id) {
    	Melody melody = Melody.findById(id);
    	notFoundIfNull(melody);
    	render(melody);
    }

	public static void vote(@Required Long id, @Required String act) {
		if(Validation.hasErrors()) notFound();
		Melody m = Melody.findById(id);
		notFoundIfNull(m);
		getVoter().vote(m, act.equals("like"));
		random();
	}
}