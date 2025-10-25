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
    private static Scanner sc = new Scanner(System.in); // Make Scanner static for easier access
    // --- END NEW STATIC FIELDS ---
    
    private static void viewUsers() {
        String votersQuery = "SELECT * FROM tbl_residents";
        String[] votersHeaders = {"ID", "Name", "Age", "Gender", "Address", "Contact"};
        String[] votersColumns = {"r_id", "r_name", "r_age", "r_gender", "r_address", "r_contact"};
        
        config conf = new config();
        conf.viewRecords(votersQuery, votersHeaders, votersColumns);
    }
    
    // --- NEW FEATURE: VACCINE VIEW HELPER ---
    private static void viewVaccines(config conf) {
        String query = "SELECT * FROM tbl_vaccine";
        String[] headers = {"ID", "Name", "Manufacturer", "Stock"};
        String[] columns = {"v_id", "v_name", "v_manufacturer", "v_stock"};
        conf.viewRecords(query, headers, columns);
    }
    
    // --- NEW FEATURE: RESIDENTS CRUD HELPER (Extracted original logic) ---
    private static void manageResidents(Scanner sc, config conf) {
        boolean inResidentMenu = true;
        while(inResidentMenu) {
            System.out.println("\n--- RESIDENT USER MANAGEMENT ---");
            System.out.println("1. Add Resident Information");
            System.out.println("2. View All Residents");
            System.out.println("3. Update Resident Information");
            System.out.println("4. Delete Resident Information");
            System.out.println("5. Back to Dashboard");
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
                case 2:
                    viewUsers(); // Your existing method
                    break; 
                case 3:
                    viewUsers();
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
                case 4:
                    viewUsers();
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
                case 5:
                    inResidentMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // --- NEW FEATURE: ADMIN USER MANAGEMENT ---
    private static void adminManageUsers(Scanner sc, config conf) {
        boolean inAdminMenu = true;
        while(inAdminMenu) {
            System.out.println("\n--- ADMIN: USER MANAGEMENT ---");
            System.out.println("1. View All System Users");
            System.out.println("2. Approve/Change User Status");
            System.out.println("3. Back to Dashboard");
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
                    String query = "SELECT id, name, email, type, status FROM tbl_users";
                    String[] headers = {"ID", "Name", "Email", "Role", "Status"};
                    String[] columns = {"id", "name", "email", "type", "status"};
                    conf.viewRecords(query, headers, columns);
                    break;
                case 2:
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
                    
                    System.out.print("Enter new status (Approved/Pending/Suspended): ");
                    String newStatus = sc.nextLine();
                    
                    String sql = "UPDATE tbl_users SET status = ? WHERE id = ?";
                    conf.updateRecord(sql, newStatus, id);
                    break;
                case 3:
                    inAdminMenu = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    
    // --- NEW FEATURE: VACCINE INVENTORY MANAGEMENT ---
    private static void manageVaccines(Scanner sc, config conf) {
        boolean inVaccineMenu = true;
        while(inVaccineMenu) {
            System.out.println("\n--- VACCINE MANAGEMENT (INVENTORY) ---");
            System.out.println("1. Add New Vaccine");
            System.out.println("2. View All Vaccines");
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
                    System.out.print("Enter Manufacturer: ");
                    String manufacturer = sc.nextLine();

                    String sqlAdd = "INSERT INTO tbl_vaccine (v_name, v_manufacturer) VALUES (?, ?, ?)";
                    conf.addRecord(sqlAdd, name, manufacturer);
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
                    System.out.print("Enter new Manufacturer: ");
                    manufacturer = sc.nextLine();
                    
                    String sqlUpdate = "UPDATE tbl_vaccine SET v_name = ?, v_manufacturer = ? WHERE v_id = ?";
                    conf.updateRecord(sqlUpdate, name, manufacturer, id);
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
    
    // --- NEW FEATURE: VACCINATION RECORDS ---
    private static void manageVaccinationRecords(Scanner sc, config conf) {
        boolean inRecordMenu = true;
        while(inRecordMenu) {
            System.out.println("\n--- VACCINATION RECORDS ---");
            System.out.println("1. Add New Vaccination Record");
            System.out.println("2. View All Vaccination Records");
            System.out.println("3. Back to Dashboard");
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
                    viewUsers();
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
                    
                    // 3. Get Dose Details and Date
                    System.out.print("Enter Dose Number (e.g., 1st, 2nd, Booster): ");
                    String doseNumber = sc.nextLine();
                    System.out.print("Enter Date Administered (YYYY-MM-DD): ");
                    String dateAdministered = sc.nextLine();
                    
                    // 4. Get Vaccinator ID (Logged-in Staff/Admin)
                    int vaccinatorId = currentUserId; 
                    
                    // 5. Insert Record and Update Stock
                    String sqlRecord = "INSERT INTO tbl_records (resident_id, vaccine_id, date_administered, dose_number, vaccinator_id) VALUES (?, ?, ?, ?, ?)";
                    conf.addRecord(sqlRecord, residentId, vaccineId, dateAdministered, doseNumber, vaccinatorId);
                    
                    // Decrease vaccine stock (Optional but recommended)
                    String updateStockSql = "UPDATE tbl_vaccine SET v_stock = v_stock - 1 WHERE v_id = ? AND v_stock > 0";
                    conf.updateRecord(updateStockSql, vaccineId); 
                    break;
                    
                case 2:
                    // View Records using JOINs
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
                    inRecordMenu = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }


    // Main Method Here
    public static void main(String[] args) {
        // Scanner sc = new Scanner(System.in); // Removed local Scanner, now using static sc
        config conf = new config();
        int choice;
        boolean isLoggedIn = true; // for Verification (Initial Menu Loop control)

        // Initial Menu Loop: Login, Register, Exit
        while (isLoggedIn) {
            System.out.println("\n\n--- Welcome to the Vaccination System ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter Choice: ");

            // Input validation
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        System.out.println("\n--- LOGIN ---");
                        // MODIFIED: Capture user data from Login.login()
                        Map<String, Object> userData = Login.login();
                        if(userData != null){
                            isLoggedIn = false; // Assume successful login for flow control
                            userType = userData.get("type").toString();
                            currentUserId = (int)userData.get("id");
                        }
                        break;
                    case 2:
                        System.out.println("\n--- REGISTRATION ---");
                        Register.register();
                        break;
                    case 3:
                        System.out.println("Exiting Program. Goodbye! 👋");
                        sc.close();
                        return; // Terminates the program
                    default:
                        System.out.println("Invalid option. Please choose 1, 2, or 3.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); // Clear the invalid input from the buffer
            }
        }
        
        // Vaccination Main Dashboard (Controlled by userType)
        boolean isOkay = false; // Used to control the main dashboard loop
        while (!isOkay){
            System.out.println("\n--- Vaccination Dashboard [" + userType + "] ---");
            
            // ADMIN-ONLY FEATURES
            if ("Admin".equalsIgnoreCase(userType)) {
                System.out.println("1. User Management (Approve Staff)");
                System.out.println("2. Residents (CRUD)");
                System.out.println("3. Vaccines (CRUD)");
                System.out.println("4. Vaccination Records (Add/View)");
                System.out.println("5. Logout");
                System.out.println("6. Exit Program");
            } 
            // STAFF/PATIENT FEATURES (Restricted menu)
            else if ("Staff".equalsIgnoreCase(userType) || "Patient".equalsIgnoreCase(userType)) {
                System.out.println("1. Residents (CRUD)");
                System.out.println("2. Vaccines (CRUD)");
                System.out.println("3. Vaccination Records (Add/View)");
                System.out.println("4. Logout");
                System.out.println("5. Exit Program");
            } else {
                // Should not happen, but log out if userType is bad
                System.out.println("Error: Unknown User Type. Logging out.");
                userType = null;
                currentUserId = -1;
                isOkay = true; // Go back to the initial loop
                continue;
            }
            
            System.out.print("Enter Choice: ");
            
            // Input validation
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine();
            } else {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); // Clear the invalid input from the buffer
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
                        isLoggedIn = true; // Return to the initial login loop
                        isOkay = true;
                        break;
                    case 6:
                        System.out.println("Exiting Program. Goodbye! 👋");
                        sc.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } else if ("Staff".equalsIgnoreCase(userType) || "Patient".equalsIgnoreCase(userType)) {
                // Adjusting the case numbers due to the removed Admin option
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
                        isLoggedIn = true; // Return to the initial login loop
                        isOkay = true;
                        break;
                    case 5:
                        System.out.println("Exiting Program. Goodbye! 👋");
                        sc.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } 
    } 
}// public class close