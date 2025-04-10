package utils;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

public class MyDB {
    private String url="jdbc:mysql://localhost:3306/healthlink15";
    private String user="root";
    private String password="";
    private Connection conn;
    private static MyDB instance;
    private MyDB() {
        try {
            conn= DriverManager.getConnection(url,user,password);
            System.out.println("Connected to database");
        }catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static MyDB getInstance(){
        if(instance==null){
            instance=new MyDB();
        }
        return instance;
    }


}
