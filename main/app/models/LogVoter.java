package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

/**
 * Someone doing LogVote
 */
@Entity
public class LogVoter extends Model {
	public String uuid = UUID.randomUUID().toString();
	
	public LogVoter vote(Melody melody, boolean like) {
		new LogVote(this, melody, like).save();
		melody.vote(like);
		return this;
	}
	
	public List<Melody> findMelodies(Boolean like) {
		return Melody.find("from Melody m where m in (select melody from LogVote where voter = ?1 "+(like==null?"":"and vote="+like)+")", this).fetch();
		//return find("select melody from LogVote where voter = ?1", this).fetch();
	}
}
