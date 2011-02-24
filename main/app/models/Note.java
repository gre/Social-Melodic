package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * One note in a melody
 */
@Entity
public class Note extends Model {
	public Integer pitch; // value of the note (1 to notesLength)
	public Integer pos; // loop position (1 to loopLength)

	public Note(Integer pitch, Integer position) {
		this.pitch = pitch;
		this.pos = position;
	}

	public Note(Note n) {
		this.pitch = n.pitch;
		this.pos = n.pos;
	}
	
	static final Integer intonationMax = 12;
	static final Integer[] justIntonations = {1,3,5,6,8,10};
	static final Integer[] notJustIntonations = {0,2,4,7,9,11};
	public static Integer randomJustIntonation(int max) {
		Integer pitch = justIntonations[(int) Math.floor(Math.random()*justIntonations.length)];
		Integer octave = (int)Math.floor(max/intonationMax);
		return 12*octave + pitch;
	}
	public static Integer randomNotJustIntonation(int max) {
		Integer pitch = notJustIntonations[(int) Math.floor(Math.random()*justIntonations.length)];
		Integer octave = (int)Math.floor(max/intonationMax);
		return 12*octave + pitch;
	}
	public static Integer randomIntonation(int max) {
	    return (int) Math.floor(Math.random()*max);
	}
}
