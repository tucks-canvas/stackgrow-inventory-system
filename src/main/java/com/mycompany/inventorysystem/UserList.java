/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.inventorysystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author remya
 */
public class UserList extends javax.swing.JPanel {

    /**
     * Creates new form Inventory
     */
    List<User> userList = new ArrayList<>();
    String projectPath = System.getProperty("user.dir");
    String jsonFilePath = projectPath + "/Databases/user.json";
    String excelFilePath = projectPath + "/Databases/User.xlsx";

    public UserList() {
        initComponents();
        tb_load();
        addTableMouseListener();

    }

     private void tb_load() {
        try {
            userList = new ArrayList<>();

            userList = retrieveUserData();
            for (User i:userList){
                System.out.println("Already there:" + i.getUsername());
            }

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // Make all cells non-editable
                    return false;
                }
            };

            model.setRowCount(0); // Clear existing rows
            model.setColumnCount(0);
            
            model.addColumn("ID");
            model.addColumn("Username");
            model.addColumn("Password");
            model.addColumn("Type");

            for (User user : userList) {
                Vector<Object> row = new Vector<>();
                row.add(user.getId());
                row.add(user.getUsername());
                row.add(user.getPassword());
                row.add(user.getType());
                model.addRow(row);
            }

            jTable2.setModel(model); // Set the updated model

            jTable2.setEnabled(true);
            jTable2.setFocusable(true);
            jTable2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Enable sorting with custom comparator for the numeric columns
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            jTable2.setRowSorter(sorter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTableMouseListener() {
        jTable2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = jTable2.getSelectedRow();
                System.out.println("THIS IS" + selectedRow);

                // Check if a row is selected
                if (selectedRow != -1 && jTable2.getColumnCount() > 0) {
                    int usernameColumnIndex = 1;

                    // Check if the column index is valid
                    if (jTable2.getColumnCount() > usernameColumnIndex) {
                        String username = jTable2.getValueAt(selectedRow, usernameColumnIndex).toString();

                        // Retrieve the selected user from the user list
                        User selectedUser = getUserByUsername(username);
                        int index = userList.indexOf(selectedUser);
                        System.out.println(index);

                        // Open a new detail form with the selected data
                        UserView editWindow = new UserView(index, selectedUser, UserList.this);
                        editWindow.setVisible(true);
                    }
                }
            }
        });
    }

    private User getUserByUsername(String username) {
        for (User user : userList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static boolean doesFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }








private List<User> retrieveUserData() {
    List<User> nuserList = new ArrayList<>();
    if (doesFileExist(jsonFilePath)) {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            nuserList = new ArrayList<>(Arrays.asList(objectMapper.readValue(reader, User[].class)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    } else {
        System.out.println("The JSON file does not exist.");

        // Code to read data from Excel file
        nuserList.addAll(readFromExcel());
        for (User i:userList){
            System.out.println("From readFromExcel:" + i.getUsername());
        }
    }
    return nuserList;
}


    public List<User> readFromExcel() {
    try (FileInputStream file = new FileInputStream(excelFilePath)) {
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        int startRow = 1; // represents the 2nd row
        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            // Assuming your Excel file has columns in the order: Username, Password, ID, Type
            Cell usernameCell = row.getCell(1);
            Cell passwordCell = row.getCell(2);
            Cell idCell = row.getCell(0);
            Cell typeCell = row.getCell(3);

            if (!(getCellValueAsString(usernameCell)==null && getCellValueAsString(passwordCell)==null && getCellValueAsString(idCell)==null && getCellValueAsString(typeCell)==null)) {
                String username = getCellValueAsString(usernameCell);
                String password = getCellValueAsString(passwordCell);
                int id = (int)Double.parseDouble(getCellValueAsString(idCell));
                String type = getCellValueAsString(typeCell);
                User user = new User(id, type, username, password);
                userList.add(user);
                
                System.out.println("From Spreadsheet: " + user.getUsername());
            }
        }

        workbook.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

    return userList;
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                return String.valueOf(cell.getNumericCellValue());
            }
        }
        return null;
    }
    public void searchUsers(String searchTerm) {
    List<User> searchResults = new ArrayList<>();

    for (User user : userList) {
        // Check if any attribute contains the search term (case-insensitive)
        if (containsIgnoreCase(user.getUsername(), searchTerm) ||
                containsIgnoreCase(user.getPassword(), searchTerm) ||
                containsIgnoreCase(String.valueOf(user.getId()), searchTerm) ||
                containsIgnoreCase(user.getType(), searchTerm)) {
            searchResults.add(user);
        }
    }

    updateTable(searchResults);
    }

    private static boolean containsIgnoreCase(String source, String target) {
        if (source != null && target != null) {
            return source.toLowerCase().contains(target.toLowerCase());
        }
        return false;


    }
    private void resetTable() {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0); // Clear the existing rows
        tb_load(); // Load the original data
    }

    public void updateTable(List<User> userList) {
    DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
    model.setRowCount(0); // Clear the existing rows

    if (userList != null) {
        for (User user : userList) {
            Vector<Object> row = new Vector<>();
            row.add(user.getId());
            row.add(user.getUsername());
            row.add(user.getPassword());
            row.add(user.getType());
            model.addRow(row);
        }
    }
}


    public void Editor (int index, User user){
        System.out.println(index);
        userList.set(index, user);
        writeToJson(userList);
        writeToExcel(userList);
        updateTable(userList);
    }
//    public void writeToExcel(String excelFilePath,List<Supplier> supplierList) {
//        Workbook workbook;
//        Sheet sheet;
//
//        File file = new File(excelFilePath);
//        if (file.exists()) {
//            // If file exists, open it
//            try (FileInputStream fileIn = new FileInputStream(excelFilePath)) {
//                workbook = WorkbookFactory.create(fileIn);
//                sheet = workbook.getSheetAt(0);
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//        } else {
//            // If file does not exist, create a new one
//            workbook = new XSSFWorkbook();
//            sheet = workbook.createSheet("Suppliers");
//        }
//
//        try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
//            Row supplierHeader = sheet.createRow(0);  // Create the first row
//            supplierHeader.createCell(0).setCellValue("Name");
//            supplierHeader.createCell(1).setCellValue("Address");
//            supplierHeader.createCell(2).setCellValue("Email");
//            supplierHeader.createCell(3).setCellValue("Number");
//
//            int startRow = 1;
//
//            // Iterate through the supplierList and write to the Excel sheet
//            for (int i = 0; i < supplierList.size(); i++) {
//                Supplier supplier = supplierList.get(i);
//                Row row = sheet.createRow(startRow + i);
//
//                // Set cell values based on your supplier properties
//                row.createCell(0).setCellValue(supplier.getName());
//                row.createCell(1).setCellValue(supplier.getAddress());
//                row.createCell(2).setCellValue(supplier.getEmail());
//                row.createCell(3).setCellValue(supplier.getNumber());
//            }
//
//            // Write the updated workbook back to the file
//            workbook.write(fileOut);
//            JOptionPane.showMessageDialog(this, "Suppliers exported successfully!");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
  public List<User> getUserList(){
    return userList;
}

    private void writeToExcel(List<User> userList) {
    String projectPath = System.getProperty("user.dir");
    String jsonFilePath = projectPath + "/Databases/user.json";
    String excelFilePath = projectPath + "/Databases/User.xlsx";

    Workbook workbook;
    try (FileInputStream fileIn = new FileInputStream(excelFilePath)) {
        workbook = WorkbookFactory.create(fileIn);
    } catch (IOException e) {
        e.printStackTrace();
        return; // Exit the method if there's an error reading the file
    }

    Sheet sheet = workbook.getSheetAt(0);

    // Clear the Excel sheet below column headers
    int lastRowNum = sheet.getLastRowNum();
    for (int i = 1; i <= lastRowNum; i++) {
        Row row = sheet.getRow(i);
        if (row != null) {
            sheet.removeRow(row);
        }
    }

    // Start row index for writing data (assuming your Excel file has headers in the first row)
    int startRow = 1;

    // Iterate through the userList and write to the Excel sheet
    for (int i = 0; i < userList.size(); i++) {
        User user = userList.get(i);
        Row row = sheet.createRow(startRow + i);

        // Set cell values based on your user properties
        row.createCell(0).setCellValue(user.getUsername());
        row.createCell(1).setCellValue(user.getPassword());
        row.createCell(2).setCellValue(user.getId());
        row.createCell(3).setCellValue(user.getType());
    }

    // Write the updated workbook back to the file
    try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
        workbook.write(fileOut);
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    private void writeToJson(List<User> user) {
        // Write the updated userList to the JSON file
        // Include error handling as needed

        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(writer, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public void addUser(User user){
        userList.add(user);
        updateTable(userList);
        writeToExcel(userList);
        writeToJson(userList);
    }

    public void Deleter(int index){
        System.out.println(index);
        userList.remove(index);
        writeToJson(userList);
        writeToExcel(userList);
        updateTable(userList);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton4.setText("Reset");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton2.setText("Search");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton4)
                .addGap(39, 39, 39)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(55, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4)
                    .addComponent(jButton2))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Name", "Address ", "Email", "Number"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jTabbedPane1.addTab("Employee Database", jScrollPane2);

        jScrollPane3.setViewportView(jTabbedPane1);

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton1.setText("Add");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton3.setText("Save");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(286, 286, 286))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 927, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1)
                        .addComponent(jButton3))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 957, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 420, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
        String text = jTextField1.getText();
        if (!text.isEmpty()) {
             searchUsers(text);
        } else {
            resetTable();
        }
        
        
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
            String text = jTextField1.getText();
        if (!text.isEmpty()) {
            System.out.println ("is it" + !text.isEmpty());
            searchUsers(text);
        } else {
            resetTable();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:    
        int ID = userList.size();
        UserEntry Add = new UserEntry(ID,UserList.this);
        Add.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        writeToExcel(userList);
        writeToJson(userList);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        tb_load();
    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
