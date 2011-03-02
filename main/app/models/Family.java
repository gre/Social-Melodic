package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

import controllers.CRUD.Hidden;

import play.data.validation.Match;
import play.data.validation.Required;
import play.db.helper.JpaHelper;
import play.db.helper.SqlQuery;
import play.db.jpa.Model;
import play.db.jpa.GenericModel.JPAQuery;

/**
 * Family of a Song. It's a tree of melody generations.
 */
@Entity
public class Family extends Model {

	@Required
	@Match(value="[a-z]{3,10}")
	public String name;
	
    @Enumerated(EnumType.STRING)
	public Status status = Status.GENERATION;
	
	@Hidden
	@ManyToOne
	public Melody root;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="family")
	public List<Melody> melodies = new ArrayList<Melody>();
	
	public Date created = new Date();
	
	// Properties

	public Double luckOfMutation = 0.1;
	public Double luckOfJustIntonation = 0.7;
	
	public Double luckToEscape1of2 = 0.85;
	public Double luckToEscape1of4 = 0.1;
	public Double luckToIgnoreSamePosition = 0.2;
	
	public enum Status {
		GENERATION, MATURATION, DEAD;
	}
	
	public Family(String name) {
		this.name = name;
	}
	
	public Family bootstrap(Integer nbMelodiesToBootstrap) {
		return bootstrap(nbMelodiesToBootstrap, 16, 20);
	}
	
	public Family bootstrap(Integer nbMelodiesToBootstrap, Integer loopLength, Integer notesLength) {
		Melody m = new Melody(loopLength, notesLength).save();
		save();
		setRoot(m);
		for(int i=0; i<nbMelodiesToBootstrap; ++i) {
		    Note n = new Note(Note.randomIntonation(m.notesLength), m.indexOfQueueRandomNote()).save();
		    m.notes.add(n);
		}
		m.save();
		return this;
	}
	
	public Family setRoot(Melody m) {
		m.setFamily(this).save();
		root = m;
		return this;
	}
	
	public static List<Family> list() {
		return find("order by date desc").fetch();
	}
	
	@Override
	public String toString() {
	    return name;
	}
	
	public static List<Family> getOpenFamilies() {
		Status[] types = {Status.GENERATION, Status.MATURATION};
		return find("status in "+SqlQuery.inlineParam(types)+" order by created desc").fetch();
	}
	
	public static Family getRandomFamily() {
		List<Family> families = getOpenFamilies();
		return families.size()==0 ? null : families.get((int)Math.floor(Math.random()*families.size()));
	}
	
	public Melody getRandomMelody(List<Melody> ignore) {
		Set<Long> ids = new HashSet<Long>();
		ids.add(0L); // hack to avoid empty set sql synthax error
		if(ignore!=null) for(Melody m : ignore) ids.add(m.id);
		List<Melody> melodies = Melody.find("family = ?1 and likes>0 and id not in "+SqlQuery.inlineParam(ids)+" order by generation desc, total asc", this).fetch();
		if(melodies.size()==0) melodies = Melody.find("family = ?1 and id not in "+SqlQuery.inlineParam(ids)+" order by generation desc, total asc", this).fetch();
		if(melodies.size()==0) return null;
		if(status == Status.GENERATION) { // linear luck
			return melodies.get((int)Math.floor(Math.random()*melodies.size()));
		}
		else if(status==Status.MATURATION) { // more luck to retrieve a high generation melody (random*random)
			return melodies.get((int) Math.floor(Math.random()*Math.random()*melodies.size()) );
		}
		return null;
	}
}
