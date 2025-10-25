package main;

import config.config;
import java.util.Scanner;

public class Register {
    public static void register() {
        config conf = new config();
        Scanner sc = new Scanner(System.in);
        String type = null;
        boolean isLooping = true;
        
        // ---REGISTRATION FORM---
        System.out.print("Full Name: ");
        String name = sc.nextLine();
        
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        
        // --- MODIFIED REGISTRATION LOGIC: Default to Staff/Admin, then force Staff ---
        while(isLooping){       
            // SELECTION OF THE TYPE OF USER
            System.out.println("1. Admin (Requires Admin creation or initial setup)");
            System.out.println("2. Staff (Default Registration Type)");
            System.out.print("Select Type: ");
            String answer = sc.nextLine();
            
            
            switch (answer){
                case "1":
                    System.out.println("Admin accounts must be created internally. Registering as Staff.");
                    type = "Staff"; // Enforcing initial registration to be Staff for security
                    isLooping = false;
                    break;
                case "2":
                    type = "Staff"; // Corrected type from "Patient" to "Staff"
                    isLooping = false;
                    break;
                default:
                    System.out.println("Invalid! Registering as Staff by default. Again.");
                    type = "Staff";
                    isLooping = false;
            }
            // Exit loop after setting type
            isLooping = false;
        }
        // New users are always set to Pending
        String sql = "INSERT INTO tbl_users (name, email, password, status, type) VALUES (?, ?, ?, ?, ?)";
        conf.addRecord(sql, name, email, password, "Pending", type);
        System.out.println("Registration complete. Please wait for an Admin to approve your account.");
    }
}