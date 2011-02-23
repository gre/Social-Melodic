package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Melody extends Model {
	@ManyToOne
	public Melody parent = null;
	@OneToMany(mappedBy="parent")
	public List<Melody> childrens = new ArrayList<Melody>();
	@ManyToOne
	public Family family;
	
	@OneToMany
	public List<Note> notes = new ArrayList<Note>();
	
	public Integer loopLength;
	public Integer notesLength;
	
	public Integer likes = 0;
	public Integer total = 0;
	
	public Integer generation = 0;
	
	public Date date = new Date();

	@OneToMany(mappedBy="melody")
	List<LogVote> votes = new ArrayList<LogVote>();
	
	public Melody(Integer loopLength, Integer notesLength) {
		this.loopLength = loopLength;
		this.notesLength = notesLength;
	}
	
	public Melody setParent(Melody parent) {
		this.parent = parent;
		for(generation=0; parent!=null; generation++, parent=parent.parent);
		this.family = parent.family;
		return this;
	}
	
	public Melody createChildrens() {
		// TODO
		return this;
	}
	
	public Melody vote(boolean like) {
		if(like) ++ likes;
		++ total;
		return this;
	}
	
	public static Melody chooseRandom(LogVoter voter) {
		List<Melody> melodies = find("from Melody m where ?1 not in (select voter from LogVote where melody=m) order by total asc", voter).fetch(50);
		if(melodies==null || melodies.size()<=0) {
			return null;
		}
		return melodies.get( (int)Math.floor(Math.random()*melodies.size()) );
	}
	
}
