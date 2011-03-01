package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * Log a vote to not revote for an already voted melody
 */
@Entity
public class LogVote extends Model {
	@ManyToOne
	public LogVoter voter;
	@ManyToOne
	public Melody melody;
	public Date date = new Date();
	
	public boolean vote;
	
	public LogVote(LogVoter v, Melody m, boolean like) {
		voter = v;
		melody = m;
		vote = like;
	}
	
}
