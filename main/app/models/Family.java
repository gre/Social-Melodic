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
	
	public static Family bootstrapRandom(String familyName) {
		Family family = new Family(familyName);
		Melody m = new Melody(16, 20);
		m.family = family;
		for(int i=0; i<3; ++i)
			m.queueRandomNote();
		m.save();
		family.setRoot(m);
		return family;
	}
}
