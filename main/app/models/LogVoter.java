package models;

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
		new LogVote(this, melody).save();
		melody.vote(like);
		return this;
	}
}
