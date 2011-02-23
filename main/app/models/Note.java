package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Note extends Model {
	public Integer pitch;
	public Integer pos; // loop position
}
