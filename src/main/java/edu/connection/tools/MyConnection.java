package edu.connection.tools;

import edu.connection.entities.Personne;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private String url="jdbc:mysql://localhost:3306/defx";
    private String login="root";
    private String pwd="";
    private Connection cnx;
    private static MyConnection instance;

    private MyConnection()  {
        try {
            cnx = DriverManager.getConnection(url , login , pwd);
            System.out.println("Connection Established...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }
    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }
}
