package Main;

import Config.Config;
import java.util.Scanner;


public class Main {
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        Config db = new Config();
        
        db. connectDB(); 
        
        System.out.print("Enter Users ID: ");
        int id = sc.nextInt();
        System.out.print("Enter Users Name: ");
        String name = sc.next();
        System.out.print("Enter Users Contact: ");
        String contact = sc.next();
        System.out.print("Enter Users Email: ");
        String email = sc.next();
        
        String sql = "INSERT INTO tbl_users (u_id, u_name, u_contact, u_email)VALUES ( ?, ?, ?, ?)";    
        
        db.addRecord(sql, id, name, contact, email);
    }
}
