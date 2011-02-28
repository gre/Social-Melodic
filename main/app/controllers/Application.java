package controllers;

import java.util.List;

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
        random();
    }
    
    public static void random() {
		if(Family.count()==0) {
		    new Family("Alpha").bootstrap(2, 16, 16).save();
		    new Family("Typhon").bootstrap(5, 24, 20).save();
		}
    	Melody melody = Melody.chooseRandom(getVoter());
    	if(melody==null) 
    	    noMoreMelodies();
    	melody(melody.id);
    }
    
    public static void noMoreMelodies() {
    	// TODO : inform user he reach all melodies vote, wait for next generation -> link to family tree
		render();
	}

    public static void melodyNotFound() {
    	render();
    }
    
	public static void melody(@Required Long id) {
    	Melody melody = Melody.findById(id);
    	if(melody==null) melodyNotFound();
    	render(melody);
    }
	
	public static void families() {
		List<Family> families = Family.list();
		render(families);
	}

	public static void family(@Required Long id) {
		Family family = Melody.findById(id);
		notFoundIfNull(family);
		render(family);
	}
	
	public static void vote(@Required Long id, @Required String act) {
		if(Validation.hasErrors()) notFound();
		Melody m = Melody.findById(id);
		notFoundIfNull(m);
		LogVoter voter = getVoter();
		if(LogVote.count("byMelodyAndVoter", m, voter)==0 && m.total < m.family.melodyMinVoteToFilter)
			voter.vote(m, act.equals("like"));
		random();
	}

    public static void adminLogin() {
        render();
    }
    public static void adminAuth(String login, String password) {
        if("admin".equals(login) && "aqwzsx".equals(password)) {
            session.put("admin", "true");
            Admin.index();
        }
        adminLogin();
    }
}
