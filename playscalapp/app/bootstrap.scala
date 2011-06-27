import play.jobs._
import models._
import play.test._
    
@OnApplicationStart class BootStrap extends Job {
    
    override def doJob {
        
        // Import initial data if the database is empty
        if(Melody.count().single() == 0) {
            Yaml[List[Any]]("bootstrap.yml").foreach {
                _ match {
                    case m:Melody => Melody.create(m)
                }
            }
        }
        
    }
    
}
