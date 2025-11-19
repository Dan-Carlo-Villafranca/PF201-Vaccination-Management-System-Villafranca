package main;

import config.config;
import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class main {
    
    // --- NEW STATIC FIELDS FOR RBAC ---
    public static String userType = null;
    public static int currentUserId = -1;
    private static Scanner sc = new Scanner(System.in); 
    // --- END NEW STATIC FIELDS ---
    
    // --- STAFF AND ADMIN ---
    private static void viewUsers() {
        String votersQuery = "SELECT * FROM tbl_residents";
        String[] votersHeaders = {"ID", "Name", "Age", "Gender", "Address", "Contact"};
        String[] votersColumns = {"r_id", "r_name", "r_age", "r_gender", "r_address", "r_contact"};
        
        config conf = new config();
        conf.viewRecords(votersQuery, votersHeaders, votersColumns);
    }
    
    // --- VACCINES AND INVENTORY ---
    private static void viewVaccines(config conf) {
        String query = "SELECT v_id, v_name, v_type, v_manufacturer, v_stock FROM tbl_vaccine";
        String[] headers = {"ID", "Name", "Type", "Manufacturer", "Stock"};
        String[] columns = {"v_id", "v_name", "v_type", "v_manufacturer", "v_stock"}; 
        conf.viewRecords(query, headers, columns);
    }
    
    // --- RESIDENTS CRUD HELPER (UNCHANGED) ---
    private static void manageResidents(Scanner sc, config conf) {
        boolean inResidentMenu = true;
        while(inResidentMenu) {
            System.out.println("\n--- RESIDENT USER MANAGEMENT (CRUD only) ---");
            System.out.println("1. Add Resident Information");
            System.out.println("2. Update Resident Information (Requires ID, view via Vaccination Records menu)");
            System.out.println("3. Delete Resident Information (Requires ID, view via Vaccination Records menu)");
            System.out.println("4. Back to Dashboard");
            System.out.print("Enter Choice: ");
            
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); 
                continue;
            }
            int action = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (action) {
                case 1:
                    System.out.print("Enter name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter age: ");
                    String age = sc.nextLine();
                    System.out.print("Enter gender(M/F): ");
                    String gender = sc.nextLine();
                    System.out.print("Enter address: ");
                    String address = sc.nextLine();
                    System.out.print("Enter contact: ");
                    String contact = sc.nextLine();

                    String sql = "INSERT INTO tbl_residents (r_name, r_age, r_gender, r_address, r_contact) VALUES (?, ?, ?, ?, ?)";
                    conf.addRecord(sql, name, age, gender, address, contact);
                    break;
                case 2: // Update
                    System.out.println("\n*** Please view the resident IDs via the 'Vaccination Records' menu (Option 4/3) first. ***");
                    System.out.print("Enter id to update: ");
                    int idToUpdate;
                    if (sc.hasNextInt()) {
                        idToUpdate = sc.nextInt();
                        sc.nextLine();
                    } else {
                        System.out.println("Invalid ID format.");
                        sc.nextLine();
                        break;
                    }
                    
                    System.out.print("Enter new name: ");
                    name = sc.nextLine();
                    System.out.print("Enter new age: ");
                    age = sc.nextLine();
                    System.out.print("Enter new gender: ");
                    gender = sc.nextLine();
                    System.out.print("Enter new address: ");
                    address = sc.nextLine();
                    System.out.print("Enter new contact: ");
                    contact = sc.nextLine();

                    sql = "UPDATE tbl_residents SET r_name = ?, r_age = ?, r_gender = ?, r_address = ?, r_contact = ? WHERE r_id = ?";
                    conf.updateRecord(sql, name, age, gender, address, contact, idToUpdate);
                    break;
                case 3: // Delete
                    System.out.println("\n*** Please view the resident IDs via the 'Vaccination Records' menu (Option 4/3) first. ***");
                    System.out.print("Enter Information ID to delete: ");
                    int idToDelete;
                    if (sc.hasNextInt()) {
                        idToDelete = sc.nextInt();
                        sc.nextLine();
                    } else {
                        System.out.println("Invalid ID format.");
                        sc.nextLine();
                        break;
                    }

                    String sqlDelete = "DELETE FROM tbl_residents WHERE r_id = ?";
                    conf.deleteRecord(sqlDelete, idToDelete);
                    break;
                case 4:
                    inResidentMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    // --- RESET PASSWORD THEN TEMPORARY PASS ---
    private static void adminResetUserPassword(Scanner sc, config conf) {
        System.out.println("\n--- ADMIN: RESET USER PASSWORD ---");

        // 1. View users to get the ID/Context
        String query = "SELECT id, name, email, type, status FROM tbl_users";
        String[] headers = {"ID", "Name", "Email", "Role", "Status"};
        String[] columns = {"id", "name", "email", "type", "status"};
        conf.viewRecords(query, headers, columns);

        System.out.print("Enter User ID whose password needs resetting: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid ID format.");
            sc.nextLine();
            return;
        }
        int userId = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter NEW TEMPORARY PASSWORD for user " + userId + ": ");
        String newPassword = sc.nextLine();

        // 2. CRUCIAL STEP: Hash the new password using the existing config method
        String newHashedPassword = config.hashPassword(newPassword);

        if (newHashedPassword == null) {
            System.out.println("Reset failed: System error during password hashing.");
            return;
        }

        // 3. Update the database with the new hash
        String sql = "UPDATE tbl_users SET password = ?, must_change_pass = 1 WHERE id = ?";

        // updateRecord safely handles the update
        conf.updateRecord(sql, newHashedPassword, userId);

        System.out.println("\n✅ Password reset successfully for User ID " + userId + ".");
        System.out.println("‼NOTE: Inform the user that their temporary password is: " + newPassword);
}

    // ----------------------------------------------------------------------------------
    // NEW SECURITY METHOD: FORCE PASSWORD CHANGE 🚨
    // ----------------------------------------------------------------------------------
    private static void forcePasswordChange(Scanner sc, config conf, int userId) {
        System.out.println("\n--- REQUIRED PASSWORD CHANGE ---");
        System.out.print("Enter your new, strong password: ");
        String newPass = sc.nextLine();
        
        // Hash the new password
        String newHashedPass = config.hashPassword(newPass);

        if (newHashedPass == null) {
            System.out.println("Error: Failed to hash new password. Please restart.");
            return;
        }

        // Update the record: new password hash and reset the must_change_pass flag to 0
        String sql = "UPDATE tbl_users SET password = ?, must_change_pass = 0 WHERE id = ?";
        
        conf.updateRecord(sql, newHashedPass, userId);
        
        System.out.println("\n Password updated successfully! You may now access the dashboard.");
    }
    
    // --- ADMIN USER MANAGEMENT (UNCHANGED) ---
    private static void adminManageUsers(Scanner sc, config conf) {
        // This check prevents potential backdoor access if the menu fails to hide the option
        if (!"Admin".equalsIgnoreCase(userType)) {
            System.out.println("Access Denied. Only Administrators can manage system users.");
            return;
        }

        boolean inAdminMenu = true;
        while(inAdminMenu) {
            System.out.println("\n--- ADMIN: SYSTEM USER MANAGEMENT ---");
            System.out.println("1. Add New User (Staff/Admin)");
            System.out.println("2. View All System Users");
            System.out.println("3. Approve/Change User Status");
            System.out.println("4. Reset User Password");
            System.out.println("5. Back to Dashboard");
            System.out.print("Enter Choice: ");
            
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); 
                continue;
            }
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline
            
            
            switch(choice) {
                case 1:
                    System.out.println("\n--- ADD NEW SYSTEM USER ---");
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Email: ");
                    String email = sc.nextLine();

                    System.out.print("Enter Temporary Password: ");
                    String password = sc.nextLine();

                    // Hash the password immediately for security
                    String hashedPassword = config.hashPassword(password);

                    if (hashedPassword == null) {
                        System.out.println("❌ User creation failed: Hashing error.");
                        break;
                    }

                    System.out.print("Enter Role (Admin or Staff): ");
                    String newType = sc.nextLine();

                    // Basic validation for type (allows only 'Admin' or 'Staff')
                    if (!"Admin".equalsIgnoreCase(newType) && !"Staff".equalsIgnoreCase(newType)) {
                        System.out.println("Invalid role entered. Defaulting to 'Staff'.");
                        newType = "Staff";
                    }

                    // New users created by Admin are automatically 'Approved'
                    String status = "Approved"; 

                    String query = "INSERT INTO tbl_users (name, email, password, status, type) VALUES (?, ?, ?, ?, ?)";
                    conf.addRecord(query, name, email, hashedPassword, status, newType);
                    System.out.println("✅ User '" + name + "' (" + newType + ") added and automatically approved.");
                    break;
                case 2:
                    query = "SELECT id, name, email, type, status FROM tbl_users";
                    String[] headers = {"ID", "Name", "Email", "Role", "Status"};
                    String[] columns = {"id", "name", "email", "type", "status"};
                    conf.viewRecords(query, headers, columns);
                    break;
                case 3:
                    // View users first
                    query = "SELECT id, name, email, type, status FROM tbl_users";
                    headers = new String[]{"ID", "Name", "Email", "Role", "Status"};
                    columns = new String[]{"id", "name", "email", "type", "status"};
                    conf.viewRecords(query, headers, columns);
                    
                    System.out.print("Enter User ID to modify status: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid ID format.");
                        sc.nextLine();
                        break;
                    }
                    int id = sc.nextInt();
                    sc.nextLine();
                    
                    System.out.print("Enter new status (Approved/Pending): ");
                    String newStatus = sc.nextLine();
                    
                    String sql = "UPDATE tbl_users SET status = ? WHERE id = ?";
                    conf.updateRecord(sql, newStatus, id);
                    break;
                case 4:
                    adminResetUserPassword(sc, conf); // Reset Password Method
                    break;
                case 5:
                    inAdminMenu = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    
    // --- VACCINE INVENTORY MANAGEMENT (UNCHANGED) ---
    private static void manageVaccines(Scanner sc, config conf) {
        boolean inVaccineMenu = true;
        while(inVaccineMenu) {
            System.out.println("\n--- VACCINE MANAGEMENT (INVENTORY) ---");
            System.out.println("1. Add New Vaccine");
            System.out.println("2. View All Vaccines (View-only)");
            System.out.println("3. Update Vaccine Stock/Details");
            System.out.println("4. Delete Vaccine");
            System.out.println("5. Back to Dashboard");
            System.out.print("Enter Choice: ");
            
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
                continue;
            }
            int choice = sc.nextInt();
            sc.nextLine();
            
            switch(choice) {
                case 1:
                    System.out.print("Enter Vaccine Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Type of Vaccine (ex. Tipdas, Tetanus. etc.): ");
                    String type = sc.nextLine();
                    System.out.print("Enter Manufacturer: ");
                    String manufacturer = sc.nextLine();
                    System.out.print("Enter Stock: ");
                    String stock = sc.nextLine();
                    
                    String sqlAdd = "INSERT INTO tbl_vaccine (v_name, v_type, v_manufacturer, v_stock) VALUES (?, ?, ?, ?)";
                    conf.addRecord(sqlAdd, name, type, manufacturer, stock); 
                    break;
                case 2:
                    viewVaccines(conf);
                    break;
                case 3:
                    viewVaccines(conf);
                    System.out.print("Enter Vaccine ID to update: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid ID format.");
                        sc.nextLine();
                        break;
                    }
                    int id = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter new Vaccine Name: ");
                    name = sc.nextLine();

                    System.out.print("Enter new Type of Vaccine: ");
                    type = sc.nextLine();

                    System.out.print("Enter new Stock level: ");
                    String stockStr = sc.nextLine(); // String Input

                    // **CRITICAL VALIDATION STEP**
                    try {
                        int stockInt = Integer.parseInt(stockStr); // convert string to integer

                        String sqlUpdate = "UPDATE tbl_vaccine SET v_name = ?, v_type = ?, v_stock = ? WHERE v_id = ?";
                        conf.updateRecord(sqlUpdate, name, type, stockInt, id); 

                    } catch (NumberFormatException e) {
                        // If conversion fails (user entered text instead of a number)
                        System.out.println("❌ Error: Stock level must be a valid whole number. Update failed.");
                    }
                    break;
                case 4:
                    viewVaccines(conf);
                    System.out.print("Enter Vaccine ID to delete: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid ID format.");
                        sc.nextLine();
                        break;
                    }
                    id = sc.nextInt();
                    sc.nextLine();
                    String sqlDelete = "DELETE FROM tbl_vaccine WHERE v_id = ?";
                    conf.deleteRecord(sqlDelete, id);
                    break;
                case 5:
                    inVaccineMenu = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    
    // --- VACCINATION RECORDS (UNCHANGED) ---
    private static void manageVaccinationRecords(Scanner sc, config conf) {
        boolean inRecordMenu = true;
        
        while(inRecordMenu) {
            System.out.println("\n--- VACCINATION RECORDS AND VIEW OPTIONS ---");
            System.out.println("1. Add New Vaccination Record");
            System.out.println("2. View All Vaccination Records (Full Report)");
            System.out.println("3. View All Residents (for ID look-up)"); // Added Residents view here
            System.out.println("4. Back to Dashboard");
            System.out.print("Enter Choice: ");
            
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
                continue;
            }
            int choice = sc.nextInt();
            sc.nextLine();
            
            switch(choice) {
                case 1:
                    // 1. Get Resident ID
                    viewUsers(); // Displays residents for ID selection
                    System.out.print("Enter Resident ID to vaccinate: ");
                    int residentId;
                    if (sc.hasNextInt()) {
                        residentId = sc.nextInt();
                        sc.nextLine();
                    } else {
                        System.out.println("Invalid ID format.");
                        sc.nextLine();
                        break;
                    }
                    
                    // 2. Get Vaccine ID
                    viewVaccines(conf); 
                    System.out.print("Enter Vaccine ID used: ");
                    int vaccineId;
                    if (sc.hasNextInt()) {
                        vaccineId = sc.nextInt();
                        sc.nextLine();
                    } else {
                        System.out.println("Invalid ID format.");
                        sc.nextLine();
                        break;
                    }
                    // ----------------------------------------------------------------------
                    // ADDED PART 1: STOCK CHECK 🚨
                    // ----------------------------------------------------------------------
                    int currentStock = conf.getVaccineStock(vaccineId); 

                    if (currentStock <= 0) {
                        System.out.println("Error: Vaccine ID " + vaccineId + " is **OUT OF STOCK** (" + currentStock + "). Cannot proceed.");
                        break; // Stop execution of Case 1
                    }

                    // 3. Get Dose Details and Date
                    System.out.print("Enter Dose Number (e.g., 1st, 2nd, Booster): ");
                    String doseNumber = sc.nextLine();
                    System.out.print("Enter Date Administered (YYYY-MM-DD): ");
                    String dateAdministered = sc.nextLine();
                    
                    // 4. Get Vaccinator ID (Logged-in Staff/Admin)
                    int vaccinatorId = currentUserId; 
                    
                    // 5. Insert Record and Update Stock
                    // Corrected SQL: Uses 'date_administered' to match your tbl_records schema
                    String sqlRecord = "INSERT INTO tbl_records (resident_id, vaccine_id, date_administered, dose_number, vaccinator_id) VALUES (?, ?, ?, ?, ?)";
                    conf.addRecord(sqlRecord, residentId, vaccineId, dateAdministered, doseNumber, vaccinatorId);
                    
                    // Decrease vaccine stock (Optional but recommended)
                    String updateStockSql = "UPDATE tbl_vaccine SET v_stock = v_stock - 1 WHERE v_id = ? AND v_stock > 0";
                    conf.updateRecord(updateStockSql, vaccineId); 
                    break;
                    
                case 2:
                    // View Vaccination Records using JOINs
                    String query = "SELECT r.record_id, res.r_name AS Resident, v.v_name AS Vaccine, " +
                                    "r.dose_number, r.date_administered, u.name AS Vaccinator " + 
                                    "FROM tbl_records r " +
                                    "JOIN tbl_residents res ON r.resident_id = res.r_id " +
                                    "JOIN tbl_vaccine v ON r.vaccine_id = v.v_id " +
                                    "JOIN tbl_users u ON r.vaccinator_id = u.id";
                                    
                    String[] headers = {"Rec. ID", "Resident", "Vaccine", "Dose No.", "Date", "Vaccinator"};
                    String[] columns = {"record_id", "Resident", "Vaccine", "dose_number", "date_administered", "Vaccinator"};
                    conf.viewRecords(query, headers, columns);
                    break;
                    
                case 3:
                    // View Residents (Moved from manageResidents)
                    viewUsers(); 
                    break;
                    
                case 4:
                    inRecordMenu = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }


    // Main Method Fixed for Seamless Loop
    public static void main(String[] args) {
        config conf = new config();
        int choice;
        // Start isLoggedIn as false. Login sets it to true, Logout sets it to false.
        boolean isLoggedIn = false; 

        // --- MASTER LOOP: Keeps the program running until 'Exit' is chosen ---
        while (true) { 
            
            // --- 1. LOGIN/REGISTER PHASE (Runs as long as the user is NOT logged in) ---
            while (!isLoggedIn) {
                System.out.println("\n\n--- Welcome to the Vaccination System ---");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Enter Choice: ");

                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine();

                    switch (choice) {
                        case 1:
                            System.out.println("\n--- LOGIN ---");
                            Map<String, Object> userData = Login.login();
                            if(userData != null){
                                isLoggedIn = true; // SUCCESS: Exit this loop and proceed to dashboard
                                userType = userData.get("type").toString();
                                currentUserId = (int)userData.get("id");
                            // CRITICAL FIX: CHECK FORCED CHANGE FLAG 
                                if (userData.containsKey("force_change") && (boolean)userData.get("force_change")) {

                                    // If forced, execute the password change routine. 
                                    // This method handles the input and updates must_change_pass back to 0.
                                    System.out.println("Initiating forced password change...");
                                    forcePasswordChange(sc, conf, currentUserId);

                                    // The execution continues below
                                }

                                // 2. Set isLoggedIn = true ONLY AFTER all security checks are handled.
                                isLoggedIn = true; // SUCCESS: Exit this loop and proceed to dashboard
                            }
                            break;
                        case 2:
                            System.out.println("\n--- REGISTRATION ---");
                            Register.register();
                            break;
                        case 3:
                            System.out.println("Exiting Program. Goodbye!");
                            sc.close();
                            return; // Terminates the program
                        default:
                            System.out.println("Invalid option. Please choose 1, 2, or 3.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    sc.nextLine(); // Clear the invalid input from the buffer
                }
            } // End of while (!isLoggedIn) - The user is now logged in.

            // --- 2. DASHBOARD PHASE (Runs as long as the user IS logged in) ---
            // This loop immediately runs because isLoggedIn is true from the successful login.
            while (isLoggedIn) {
                System.out.println("\n--- Vaccination Dashboard [" + userType + "] ---");
                
                // ADMIN-ONLY FEATURES
                if ("Admin".equalsIgnoreCase(userType)) {
                    System.out.println("1. User Management (Approve Staff & View All System Users)");
                    System.out.println("2. Residents (Add/Update/Delete only)");
                    System.out.println("3. Vaccines");
                    System.out.println("4. Vaccination Records (Add/View All Records/View Residents)");
                    System.out.println("5. Logout");
                    System.out.println("6. Exit Program");
                } 
                // STAFF/PATIENT FEATURES
                else if ("Staff".equalsIgnoreCase(userType)) {
                    System.out.println("1. Residents (Add/Update/Delete only)");
                    System.out.println("2. Vaccines");
                    System.out.println("3. Vaccination Records (Add/View All Records/View Residents)");
                    System.out.println("4. Logout");
                    System.out.println("5. Exit Program");
                } else {
                    System.out.println("Error: Unknown User Type. Logging out.");
                    userType = null;
                    currentUserId = -1;
                    isLoggedIn = false; // Force logout and restart login loop
                    continue; // Skip the rest of this iteration
                }
                
                System.out.print("Enter Choice: ");
                
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine();
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    sc.nextLine();  
                    continue;
                }

                if ("Admin".equalsIgnoreCase(userType)) {
                    switch (choice) {
                        case 1: 
                            adminManageUsers(sc, conf); 
                            break;
                        case 2: 
                            manageResidents(sc, conf); 
                            break;
                        case 3: 
                            manageVaccines(sc, conf); 
                            break;
                        case 4: 
                            manageVaccinationRecords(sc, conf); 
                            break;
                        case 5:
                            System.out.println("Logging out...");
                            userType = null;
                            currentUserId = -1;
                            isLoggedIn = false; // Logout: **Exits this inner dashboard loop**
                            break;
                        case 6:
                            System.out.println("Exiting Program. Goodbye! 👋");
                            sc.close();
                            return;
                        default:
                            System.out.println("Invalid option. Please try again.");
                    }
                } else if ("Staff".equalsIgnoreCase(userType)) {
                    switch (choice) {
                        case 1: 
                            manageResidents(sc, conf); 
                            break;
                        case 2: 
                            manageVaccines(sc, conf); 
                            break;
                        case 3: 
                            manageVaccinationRecords(sc, conf); 
                            break;
                        case 4:
                            System.out.println("Logging out...");
                            userType = null;
                            currentUserId = -1;
                            isLoggedIn = false; // Logout: **Exits this inner dashboard loop**
                            break;
                        case 5:
                            System.out.println("Exiting Program. Goodbye! 👋");
                            sc.close();
                            return;
                        default:
                            System.out.println("Invalid option. Please try again.");
                    }
                }
            } // End of while (isLoggedIn) - Dashboard loop
            
            // When the inner Dashboard loop breaks (via Logout or Error), the Master Loop (while(true)) 
            // restarts, which immediately hits the 'while (!isLoggedIn)' condition, bringing the user 
            // back to the Welcome/Login menu.
        } // End of while (true) - Master Loop
    }
} //public main class close