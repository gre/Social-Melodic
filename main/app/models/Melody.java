package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.helper.SqlQuery;
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
	
	public Boolean sterile = false; // détermine si la mélodie est une feuille
	
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
	
	public boolean sameAs(Melody m) {
		if(loopLength!=m.loopLength) return false;
		Vector<List<Integer>> notesArray = getNotesArray();
		Vector<List<Integer>> mNotesArray = m.getNotesArray();
		try {
			for(int i=0; i<loopLength; ++i) {
				List<Integer> notesPos = notesArray.get(i);
				List<Integer> mNotesPos = mNotesArray.get(i);
				if(!notesPos.equals(mNotesPos)) return false;
			}
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}
	
	public Melody searchSameChild(Melody melody) {
		for(Melody m : childrens)
			if(m.sameAs(melody))
				return m;
		return null;
	}
	
	/**
	 * @return null if no place has been found for this children (this time),
	 * else return the melody getted or created (if possible)
	 */
	public Melody getOrCreateRandomChild() {
		Integer position = indexOfQueueRandomNote();
		if(position>=0) {
			Note note = new Note(Math.random()<family.luckOfJustIntonation ? Note.randomJustIntonation(notesLength) : Note.randomNotJustIntonation(notesLength), position).save();
			Melody m = new Melody(this, note).applyMutation().save();
			Melody search = searchSameChild(m);
			if(search!=null) {
				m.delete();
				return search;
			}
			else {
				childrens.add(m);
				save();
				return m;
			}
		}
		else {
			if(childrens.size()==0) {
				sterile = true;
				save();
			}
		}
		return null;
	}

	public Melody getRandomChild(List<Melody> ignore) {
		List<Melody> melodies = new ArrayList<Melody>(childrens);
		if(ignore!=null) melodies.removeAll(ignore);
		return melodies.size()==0 ? null : melodies.get((int)Math.floor(melodies.size()*Math.random()));
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
		Integer step = escape1of2 ? (escape1of4 ? 4 : 2) : 1;
		if(Math.random()<family.luckToIgnoreSamePosition) {
			Integer highest = getHighestPosition();
			if(highest<0) return 0;
			return ((int)Math.floor((Math.random()*(double)highest) / (double)step))*step;
		}
		else {
			
			for(int pos = 0; pos<loopLength; pos += step )
				if(getNotesForPosition(pos).size()==0)
					return pos;
		}
		return -1;
	}
	
	public List<Note> getNotesForPosition(Integer i) {
		List<Note> l = new ArrayList<Note>();
		for(Note n : notes)
			if(n.pos.equals(i))
				l.add(n);
		return l;
	}
	
	public Integer getHighestPosition() {
		Integer highest = -1;
		for(Note n : notes)
			if(n.pos > highest)
				highest = n.pos;
		return highest;
	}
	
	public Vector<List<Integer>> getNotesArray() {
	    Vector< List<Integer> > array = new Vector<List<Integer>>(loopLength);
	    for(int i=0; i<loopLength; ++i)
	        array.add(new ArrayList<Integer>());
	    for(Note n : notes)
	        array.get(n.pos).add(n.pitch);
	    return array;
	}
	
	public boolean hasChildrens() {
	    return childrens.size()>0;
	}
	
	public void vote(boolean like) {
		if(like) ++ likes;
		++ total;
		save();
	}
}
