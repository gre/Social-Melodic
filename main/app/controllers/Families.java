package controllers;

import play.*;
import play.mvc.*;
import models.Family;

@With(Admin.class)
@CRUD.For(Family.class)
public class Families extends CRUD {
    
}
