import play.jobs._
import models._
import play.test._
    
@OnApplicationStart class BootStrap extends Job {
    
    override def doJob {
        if(Family.count().single() == 0) {
            Family.bootstrap("Alpha", 1, 20, 16);
            Family.bootstrap("Typhon", 2, 32, 20);
        }
    }
    
}
