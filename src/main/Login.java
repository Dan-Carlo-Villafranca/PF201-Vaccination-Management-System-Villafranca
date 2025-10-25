package main;

import config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap; // Added import for Map/HashMap

public class Login {
    // MODIFIED: Method now returns Map of user data or null
    public static Map<String, Object> login(){
        config conf = new config();
        Scanner sc = new Scanner(System.in);
        
        System.out.print("Enter Email: ");
        String Uemail = sc.nextLine();
        
        System.out.print("Enter Password: ");
        String Upass = sc.nextLine();
        
        String sql = "SELECT * FROM tbl_users WHERE email =? AND password = ?";
        
        List<Map<String, Object>> result = conf.fetchRecords(sql, Uemail, Upass);
        
        if (!result.isEmpty()){
            
            Map<String, Object> userData = result.get(0);
            String name = userData.get("name").toString();
            String type = userData.get("type").toString();
            String status = userData.get("status").toString();
            
            if ("Approved".equals(status)) {
                // Ensure the Map contains u_id and type for main
                userData.put("id", (int)userData.get("id")); // Assuming u_id is an integer
                userData.put("type", type);
                
                System.out.println("\n\nWelcome " + name + " [" + type + "]\n\n");
                return userData; // Login successful and approved, return data
            } else {
                System.out.println("Your account is not approved. Status: " + status);
                return null; // Login successful but status is Pending
            }
        } else {
            System.out.println("INVALID INPUT.");
            return null; // Login failed (bad email/password)
        }       
    }
}