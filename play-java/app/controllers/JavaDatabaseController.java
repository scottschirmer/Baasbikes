package controllers;

import play.mvc.*;

import views.html.*;
import javax.inject.Inject;
import java.sql.Connection;

import play.db.*;

class JavaDatabaseController extends Controller {

    private Database db;

    @Inject
    public JavaDatabaseController(Database db) {
        this.db = db;
    }
    Connection connection = db.getConnection();

    
}