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
        
        // ----------------------------------------------------
        // STEP 1: Hash the password entered by the user
        // ----------------------------------------------------
        String enteredPasswordHash = config.hashPassword(Upass);
        
        if (enteredPasswordHash == null) {
            System.out.println("Login failed due to system error. Try again.");
            return null;
        }
        
        String sql = "SELECT * FROM tbl_users WHERE email =?";
        
        List<Map<String, Object>> result = conf.fetchRecords(sql, Uemail);
        
        if (result.isEmpty()){
            // User not found in database based on email
            System.out.println("INVALID INPUT. (Email or Password incorrect)");
            return null;
        }
        
        Map<String, Object> userData = result.get(0);
        String storedPasswordHash = userData.get("password").toString();
        
        
        
        if (enteredPasswordHash.equals(storedPasswordHash)) {
            
            String name = userData.get("name").toString();
            String type = userData.get("type").toString();
            String status = userData.get("status").toString();
            
            if ("Approved".equals(status)) {
                
                //  Safely extract and convert the 'id' (robust casting)
                Object idObj = userData.get("id");

                if (idObj instanceof Long) {
                    userData.put("id", ((Long) idObj).intValue());
                } else if (idObj instanceof Integer) {
                    userData.put("id", (Integer) idObj);
                } else {
                    System.out.println("Internal Error: User ID format unknown. Login aborted.");
                    return null;
                }
                
                // 5. Check the must_change_pass flag SAFELY (PREVENTS ClassCastException)
                Object mustChangePassObj = userData.get("must_change_pass");
                int resetFlag = 0; // Default to 0 (false) if NULL or type is unknown

                if (mustChangePassObj != null) {
                    // Prioritize the Integer check, then the Long check.
                    if (mustChangePassObj instanceof Integer) {
                        resetFlag = (Integer) mustChangePassObj;
                    } else if (mustChangePassObj instanceof Long) {
                        // Safely convert Long to int if it's the returned type
                        resetFlag = ((Long) mustChangePassObj).intValue();
                    }
                }

                if (resetFlag == 1) {
                    System.out.println("\n\n‼ SECURITY ALERT: You are using a temporary password.");
                    System.out.println("You MUST change your password before accessing the dashboard.");
                    
                    // Signal main.java to intercept and force a password change
                    userData.put("force_change", true); 
                    
                    // Login is technically successful; return data to trigger the change routine
                    return userData; 
                }
                userData.put("type", type);
                
                System.out.println("\n\nWelcome " + name + " [" + type + "]\n");
                return userData; // Login successful and approved, return data
            } else {
                System.out.println("Your account is not approved. Status: " + status);
                return null; // Login successful but status is Pending
            }
        } else { // Failed Hash Check
            System.out.println("INVALID INPUT.");
            return null; // Login failed (bad email/password)
        }         
    }
}