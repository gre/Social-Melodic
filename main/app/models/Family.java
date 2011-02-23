package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Family extends Model {
	public String name;
	
	@ManyToOne
	public Melody root;
	
	public Family(String name) {
		this.name = name;
	}
	
	public Family setRoot(Melody m) {
		m.family = this;
		root = m;
		return this;
	}
}
