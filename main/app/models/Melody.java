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
	
	public Integer mutation = 0;
	
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
		family = parent.family;
		mutation = parent.mutation;
		return this;
	}

	static final Double percentOfMutation = 0.05;
	static final Double percentOfAllPossibilities = 0.8;
	static final Double luckOfJustIntonation = 0.7;
	
	public Melody createChildrens() {
		int total = (int) Math.round(notesLength * percentOfAllPossibilities);
		if(total<=0) return this;
		int totalJustIntonation = (int) Math.round(total * luckOfJustIntonation);
		int totalNotJustIntonation = total - totalJustIntonation;
		int totalMutation = (int) Math.round(percentOfMutation * total);
		while(total-->0) {
			Note note = null;
			Melody melody = new Melody(loopLength, notesLength);
			Integer position = indexOfQueueRandomNote();
			if(position>=0) {
				if(Math.random()>0.5 && totalJustIntonation-->0)
					note = new Note(Note.randomJustIntonation(notesLength), position);
				else if(totalNotJustIntonation-->0)
					note = new Note(Note.randomNotJustIntonation(notesLength), position);
			}
			
			if(note!=null) {
				melody.setParent(this);
				for(Note thisNote : notes) {
					Note n = new Note(thisNote);
					n.save();
					melody.notes.add(n);
				}
				note.save();
				melody.notes.add(note);
				if(totalMutation-->0)
					melody.applyMutation();
				melody.save();
				childrens.add(melody);
			}
		}
		save();
		return this;
	}

	private Melody applyMutation() {
		do {
			Note n = notes.get((int) Math.floor(Math.random()*notes.size()));
			n.pitch = (int) Math.floor(Math.random()*notesLength);
			n.save();
		} while(Math.random() > percentOfMutation);
		return this;
	}

	static final Double luckToEscape1of2 = 0.9;
	static final Double luckToEscape1of4 = 0.1;
	static final Double luckToAvoidSamePosition = 0.9;
	
	public int indexOfQueueRandomNote() {
		boolean escape1of2 = Math.random() > luckToEscape1of2;
		boolean escape1of4 = Math.random() > luckToEscape1of4;
		boolean avoidSamePosition = Math.random() > luckToAvoidSamePosition;
		boolean found = false;
		for(int pos = 0; pos<loopLength && !found; pos += escape1of2 ? (escape1of4 ? 4 : 2) : 1 )
			if(avoidSamePosition || getNotesForPosition(pos).size()==0)
				return pos;
		return -1;
	}
	
	/**
	 * @return note if place has been found, else null
	 */
	public Note queueRandomNote() {
		Integer pitch = (int) Math.floor(Math.random()*notesLength);
		boolean alreadyIn = false;
		int pos = indexOfQueueRandomNote();
		if(pos==-1) return null;
		for(Note n : notes) {
			if(n.pitch==pitch && n.pos==pos)
				alreadyIn = true;
		}
		if(!alreadyIn) {
			Note n = new Note(pitch, pos);
			n.save();
			notes.add(n);
			return n;
		}
		return null;
	}
	
	public List<Note> getNotesForPosition(Integer i) {
		List<Note> l = new ArrayList<Note>();
		for(Note n : notes)
			if(n.pos == i)
				l.add(n);
		return l;
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
