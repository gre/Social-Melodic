package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

/**
 * A melody entity. It's forked from his parent melody and will create children melodies.
 */
@Entity
public class Melody extends Model {
	@ManyToOne
	public Melody parent = null;
	
	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
	public List<Melody> childrens = new ArrayList<Melody>();
	
	@ManyToOne
	public Family family;
	
	@OneToMany(cascade=CascadeType.ALL)
	public List<Note> notes = new ArrayList<Note>();
	
	public Integer loopLength;
	public Integer notesLength;
	
	public Integer likes = 0;
	public Integer total = 0;
	
	public Integer generation = 0;
	
	public Integer mutation = 0;
	
	public Boolean sterile = false;
	
	public Date date = new Date();

	@OneToMany(mappedBy="melody")
	List<LogVote> votes = new ArrayList<LogVote>();

	///	CONSTANTS : must be editable from backoffice
	
	public Melody(Integer loopLength, Integer notesLength) {
		this.loopLength = loopLength;
		this.notesLength = notesLength;
	}
	
	public Melody(Melody parent) {
		this(parent.loopLength, parent.notesLength);
		setParent(parent);
		for(Note note : parent.notes) {
			Note n = new Note(note).save();
			notes.add(n);
		}
	}
	
	public Melody(Melody parent, Note newNote) {
		this(parent);
		notes.add(newNote);
	}
	
	public Melody setParent(Melody parent) {
		this.parent = parent;
		if(parent!=null) {
			generation = parent.generation + 1;
			family = parent.family;
			mutation = parent.mutation;
		}
		return this;
	}
	
	public Melody setFamily(Family family) {
		this.family = family;
		return this;
	}
	
	public Melody createChildrens() {
		int total = (int) Math.round(notesLength * family.percentOfAllPossibilities);
		if(total<=0) return this;
		while(total-->0) {
			Integer position = indexOfQueueRandomNote();
			if(position>=0) {
				Note note = new Note(Math.random()<family.luckOfJustIntonation ? Note.randomJustIntonation(notesLength) : Note.randomNotJustIntonation(notesLength), position).save();
				Melody melody = new Melody(this, note).applyMutation().save();
				childrens.add(melody);
			}
		}
		save();
		if(generation+1 > family.depth) {
			family.depth = generation + 1;
			family.save();
		}
        if(!hasChildrens()) {
            sterile = true;
        }
		return this;
	}

	private Melody applyMutation() {
		while(Math.random() < family.luckOfMutation) {
			Note n = notes.get((int) Math.floor(Math.random()*notes.size()));
			n.pitch = (int) Math.floor(Math.random()*notesLength);
			n.save();
			mutation++;
		}
		return this;
	}
	
	public int indexOfQueueRandomNote() {
		boolean escape1of2 = Math.random() < family.luckToEscape1of2;
		boolean escape1of4 = Math.random() < family.luckToEscape1of4;
		boolean avoidSamePosition = Math.random() < family.luckToAvoidSamePosition;
		for(int pos = 0; pos<loopLength; pos += escape1of2 ? (escape1of4 ? 4 : 2) : 1 )
			if(avoidSamePosition || getNotesForPosition(pos).size()==0)
				return pos;
		return -1;
	}
	
	public List<Note> getNotesForPosition(Integer i) {
		List<Note> l = new ArrayList<Note>();
		for(Note n : notes)
			if(n.pos == i)
				l.add(n);
		return l;
	}
	
	public Vector<List<Integer>> getNotesArray() {
	    Vector< List<Integer> > array = new Vector<List<Integer>>(loopLength);
	    for(int i=0; i<loopLength; ++i)
	        array.add(new ArrayList<Integer>());
	    for(Note n : notes) {
	        List<Integer> notes = array.get(n.pos);
	        notes.add(n.pitch);
	    }
	    return array;
	}
	
	public boolean childrensReadyToFilter() {
		return count("parent=?1 and total < ?2", this, family.melodyMinVoteToFilter) == 0;
	}
	
	public boolean hasChildrens() {
	    return childrens.size()>0;
	}
	
	public void checkFilter() {
        if(!hasChildrens() && childrensReadyToFilter())
            filterChildrens();
	}
	
	public void filterChildrens() {
		Melody keep = find("parent=?1 order by likes desc", this, family.melodyMinVoteToFilter).first();
		for(Melody m : childrens) {
			if(!m.equals(keep))
				m.delete();
		}
		childrens = new ArrayList<Melody>();
		childrens.add(keep);
		save();
		keep.createChildrens();
	}
	
	public void vote(boolean like) {
		if(like) ++ likes;
		++ total;
		save();
		parent.checkFilter();
	}
	
	public static JPAQuery votables(LogVoter voter) {
	    return find("from Melody m where parent is not null and total < family.melodyMinVoteToFilter and ?1 not in (select voter from LogVote where melody=m) order by generation asc, total asc", voter);
	}
	
	public static Melody chooseRandom(LogVoter voter) {
		List<Melody> melodies = votables(voter).fetch(50);
		if(melodies.size()==0) {
		    Melody melodyWithNoChild = Melody.find("from Melody m where sterile = false and parent is not null and m not in (select parent from Melody) order by generation asc, likes desc").first();
		    if(melodyWithNoChild!=null) {
		        melodyWithNoChild.createChildrens();
		        melodies = votables(voter).fetch(50);
		    }
		}
		if(melodies.size()==0) return null;
		return melodies.get( (int)Math.floor(Math.random()*melodies.size()) );
	}
}
