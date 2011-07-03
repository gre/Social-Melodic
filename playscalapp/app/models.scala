package models

import play._
import play.mvc._

import play.db.anorm._
import play.db.anorm.SqlParser._
import play.db.anorm.defaults._

import java.util.{Date}

object Status extends Enumeration {
    type Status = Value
    val GENERATION = Value("GENERATION")
    val MATURATION = Value("MATURATION")
    val DEAD = Value("DEAD")
}

case class Family (
    id: Pk[Long],
    name: String,
    status: String = Status.GENERATION.toString,
    root_id: Option[Long],
    created: Date = new Date(),
    luckOfMutation : Double = 0.1,
    luckOfJustIntonation : Double = 0.7,
    luckToEscape1of2 : Double = 0.85,
    luckToEscape1of4 : Double = 0.1,
    luckToIgnoreSamePosition : Double = 0.2
) {
    override def toString = name
    def root = root_id match { case Some(id) => Melody.find("id = {id}").on("id" -> id).first() }
}

object Family extends Magic[Family] {
    def apply(name:String):Family = Family(NotAssigned, name, Status.GENERATION.toString, None, new Date(), 0.1, 0.7, 0.85, 0.1, 0.2)
    def bootstrap(name:String, nbMelodies:Int):Family = bootstrap(name, nbMelodies, 16, 20)
    def bootstrap(name:String, nbMelodies:Int, loopLength:Int, notesLength:Int):Family = {
        val family:Family = create(apply(name)).get;
        val m:Melody = Melody.create(family, loopLength, notesLength).get;
        val f = family.copy(root_id=m.id.get)
        update(f);
        for(i <- 0 until nbMelodies) {
            m.id.get match {
                case Some(id) => Note.create(Note(id, i*2, (Math.random*notesLength).toInt ))
            }
        }
        f
    }
}

case class Melody (
  id: Pk[Long],
  parent_id: Option[Long],
  family_id: Long,
  loopLength: Int,
  notesLength: Int,
  likes: Int = 0,
  total: Int = 0,
  generation: Int = 0,
  mutation: Int = 0,
  sterile: Boolean = false,
  created: Date = new Date()
) {

    def setFamily(f:Family) = {
        //family_id = f.id
        this
    }

    def notes() = {
        Note.find("melody_id = {melody_id} order by pos").on("melody_id" -> id.apply()).list()
    }

    def notesArray() = {
    }

    // TODO
    // def getNotesArray() = "[[14, 16], [13], [23], [], [20], [], [7], [], [22], [], [23], [], [8], [], [20], [], [22], [], [22], []]"
}

object Melody extends Magic[Melody] {
  def apply(family_id:Long, loopLength:Int, notesLength:Int):Melody = Melody(NotAssigned, None, family_id, 16, 20, 0, 0, 0, 0, false, new Date());
  def getFirst() = {
    Melody.find().first().get
  }

  def create(family:Family, loopLength:Int, notesLength:Int):Option[Melody] = {
    family.id.get match {
        case Some(id) => Some(create(Melody(id, loopLength, notesLength)).get)
        case None => None
    }
  }
}


case class Note (
    id: Pk[Long],
    pos: Int,
    pitch: Int,
    melody_id: Long
) {
    def sameAs(n : Note) = (pitch==n.pitch && pos==n.pos)
}

object Note extends Magic[Note] {
    def apply(melody_id:Long, pos:Int, pitch:Int):Note = Note(NotAssigned, pos, pitch, melody_id)
    final val intonationMax = 12;
    final val justIntonations = List(1,3,5,6,8,10);
}
