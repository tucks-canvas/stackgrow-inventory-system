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
public class ContactList extends javax.swing.JPanel {

    /**
     * Creates new form Inventory
     */
    List<Supplier> supplierList = new ArrayList<>();
    String projectPath = System.getProperty("user.dir");
    String jsonFilePath = projectPath + "/Databases/supplier.json";
    String excelFilePath = projectPath + "/Databases/Database.xlsx";

    public ContactList() {
        initComponents();
        tb_load();
        addTableMouseListener();

    }

    private void tb_load() {
        try {
            for (Supplier i:supplierList){
                System.out.println("Already there:" + i.getName());
            }
            supplierList = new ArrayList<>();

            supplierList = retrieveSupplierData();
            for (Supplier i:supplierList){
                System.out.println("Already there:" + i.getName());
            }


            DefaultTableModel model = new DefaultTableModel()
            {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // Make all cells non-editable
                    return false;
                }
            };


            model.setRowCount(0); // Clear existing rows
            model.setColumnCount(0);

            model.addColumn("Name");
            model.addColumn("Address");
            model.addColumn("Email");
            model.addColumn("Number");

            for (Supplier supplier : supplierList) {
                Vector<Object> row = new Vector<>();
                row.add(supplier.getName());
                row.add(supplier.getAddress());
                row.add(supplier.getEmail());
                row.add(supplier.getNumber());
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
                    int nameColumnIndex = 0;

                    // Check if the column index is valid
                    if (jTable2.getColumnCount() > nameColumnIndex) {
                        String name = jTable2.getValueAt(selectedRow, nameColumnIndex).toString();

                        // Retrieve the selected supplier from the supplier list
                        Supplier selectedSupplier = getSupplierByName(name);
                        int index = supplierList.indexOf(selectedSupplier);
                        System.out.println(index);

                        // Open a new detail form with the selected data
                        SuppliersView editWindow = new SuppliersView(index, selectedSupplier, ContactList.this);
                        editWindow.setVisible(true);
                    }
                }
            }
        });
    }

    private Supplier getSupplierByName(String name) {
        for (Supplier supplier : supplierList) {
            if (supplier.getName().equals(name)) {
                return supplier;
            }
        }
        return null;
    }

    public static boolean doesFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }









    private List<Supplier> retrieveSupplierData() {


        List <Supplier> nsupplierList = new ArrayList<>();
        if (doesFileExist(jsonFilePath)) {
            try (FileReader reader = new FileReader(jsonFilePath)) {
                ObjectMapper objectMapper = new ObjectMapper();
                nsupplierList = new ArrayList<> (Arrays.asList(objectMapper.readValue(reader, Supplier[].class)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("The JSON file does not exist.");

            // Code to read data from Excel file

            nsupplierList.addAll(readFromExcel());
            for (Supplier i:supplierList){
                System.out.println("From readFromExcel:" + i.getName());
            }

        }

        return nsupplierList;
    }


    public List<Supplier> readFromExcel(String excelFilePath) {


        try (FileInputStream file = new FileInputStream(excelFilePath)) {
            Workbook workbook = new XSSFWorkbook(file);

            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(0);
            String[] expectedColumnNames = {"Name", "Address", "Email", "Number"};
            for (int i = 0; i < expectedColumnNames.length; i++) {
                Cell cell = row.getCell(i);
                if (cell == null || !cell.getStringCellValue().trim().equals(expectedColumnNames[i])) {

                    JOptionPane.showMessageDialog(this, "Columns must have headings in the format of \n (Name    Address    Email    Number)");
                    return(readFromExcel());
                }
            }
            int startRow = 1; // represents the 2nd row
            for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                row = sheet.getRow(rowIndex);

                Cell nameCell = row.getCell(0);
                Cell addressCell = row.getCell(1);
                Cell emailCell = row.getCell(2);
                Cell numberCell = row.getCell(3);

                if (!(getCellValueAsString(nameCell)==null && getCellValueAsString(addressCell)==null && getCellValueAsString(emailCell)==null && getCellValueAsString(numberCell)==null)){
                    String name = getCellValueAsString(nameCell);
                    String address = getCellValueAsString(addressCell);
                    String email = getCellValueAsString(emailCell);
                    String number = getCellValueAsString(numberCell);

                    Supplier supplier = new Supplier(name, address, email, number);
                    boolean foundExistingSupplier = false;

                    for (Supplier existingSupplier : supplierList) {
                        System.out.println(existingSupplier.getName());
                        if (existingSupplier.getName().equals(supplier.getName())) {
                            foundExistingSupplier = true;

                            Object[] options = {"YES", "NO"};
                            int choice = JOptionPane.showOptionDialog(this,
                                    "Would you like to update the entry: "+ existingSupplier.getName()+"?", "Supplier with the same attributes already exists.",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                    null, options, options[0]);

                            if (choice == JOptionPane.YES_OPTION) {
                                existingSupplier.setAddress(supplier.getAddress());
                                existingSupplier.setEmail(supplier.getEmail());
                                existingSupplier.setNumber(supplier.getNumber());
                            }

                            break;
                        }

                    }

                    if (!foundExistingSupplier) {
                        supplierList.add(supplier);
                    }

                }


            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, "Suppliers imported successfully!");

        return supplierList;
    }
    public List<Supplier> readFromExcel() {


        try (FileInputStream file = new FileInputStream(excelFilePath)) {
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(1);
            int startRow = 1; // represents the 2nd row
            for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                // Assuming your Excel file has columns in the order: Name, Address, Email, Number
                Cell nameCell = row.getCell(0);
                Cell addressCell = row.getCell(1);
                Cell emailCell = row.getCell(2);
                Cell numberCell = row.getCell(3);

                if (!(getCellValueAsString(nameCell)==null && getCellValueAsString(addressCell)==null && getCellValueAsString(emailCell)==null && getCellValueAsString(numberCell)==null)) {
                String name = getCellValueAsString(nameCell);
                String address = getCellValueAsString(addressCell);
                String email = getCellValueAsString(emailCell);
                String number = getCellValueAsString(numberCell);
                Supplier supplier = new Supplier(name, address, email, number);
                supplierList.add(supplier);
                
                System.out.println("From Spreadsheet: " + supplier.getName());
            


                }


            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return supplierList;
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
    public void searchSuppliers(String searchTerm) {

        List<Supplier> searchResults = new ArrayList<>();

        for (Supplier supplier : supplierList) {
            // Check if any attribute contains the search term (case-insensitive)
            if (containsIgnoreCase(supplier.getName(), searchTerm) ||
                    containsIgnoreCase(supplier.getAddress(), searchTerm) ||
                    containsIgnoreCase(supplier.getEmail(), searchTerm) ||
                    containsIgnoreCase(supplier.getNumber(), searchTerm)) {
                searchResults.add(supplier);
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

    public void updateTable(List<Supplier> supplierList) {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0); // Clear the existing rows

        if (supplierList != null) {
            for (Supplier supplier : supplierList) {
                Vector<Object> row = new Vector<>();
                row.add(supplier.getName());
                row.add(supplier.getAddress());
                row.add(supplier.getEmail());
                row.add(supplier.getNumber());
                model.addRow(row);
            }
        }
    }

    public void Editor (int index, Supplier supplier){
        System.out.println(index);
        supplierList.set(index, supplier);
        writeToJson(supplierList);
        writeToExcel(supplierList);
        updateTable(supplierList);
    }
    public void setSupplierList(List<Supplier> nsupplyList){
        supplierList = nsupplyList;
    }
    public void writeToExcel(String excelFilePath,List<Supplier> supplierList) {
        Workbook workbook;
        Sheet sheet;

        File file = new File(excelFilePath);
        if (file.exists()) {
            // If file exists, open it
            try (FileInputStream fileIn = new FileInputStream(excelFilePath)) {
                workbook = WorkbookFactory.create(fileIn);
                sheet = workbook.getSheetAt(0);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            // If file does not exist, create a new one
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Suppliers");
        }

        try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
            Row supplierHeader = sheet.createRow(0);  // Create the first row
            supplierHeader.createCell(0).setCellValue("Name");
            supplierHeader.createCell(1).setCellValue("Address");
            supplierHeader.createCell(2).setCellValue("Email");
            supplierHeader.createCell(3).setCellValue("Number");

            int startRow = 1;

            // Iterate through the supplierList and write to the Excel sheet
            for (int i = 0; i < supplierList.size(); i++) {
                Supplier supplier = supplierList.get(i);
                Row row = sheet.createRow(startRow + i);

                // Set cell values based on your supplier properties
                row.createCell(0).setCellValue(supplier.getName());
                row.createCell(1).setCellValue(supplier.getAddress());
                row.createCell(2).setCellValue(supplier.getEmail());
                row.createCell(3).setCellValue(supplier.getNumber());
            }

            // Write the updated workbook back to the file
            workbook.write(fileOut);
            JOptionPane.showMessageDialog(this, "Suppliers exported successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   public List<Supplier> getSupplyList(){
        return supplierList;
    }


    private void writeToExcel(List<Supplier> supplierList) {
        String projectPath = System.getProperty("user.dir");
        String excelFilePath = projectPath + "/Databases/Database.xlsx";
        
        try (FileInputStream fileIn = new FileInputStream(excelFilePath);
             Workbook workbook = WorkbookFactory.create(fileIn);
             FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {

            Sheet sheet = workbook.getSheetAt(1);

            // Start row index for writing data (assuming your Excel file has headers in the first row)
            int startRow = 1;
            //Clears Excel Sheet below column headers
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    sheet.removeRow(row);
                }
            }

            // Iterate through the supplierList and write to the Excel sheet
            for (int i = 0; i < supplierList.size(); i++) {
                Supplier supplier = supplierList.get(i);
                Row row = sheet.createRow(startRow + i);

                // Set cell values based on your supplier properties
                row.createCell(0).setCellValue(supplier.getName());
                row.createCell(1).setCellValue(supplier.getAddress());
                row.createCell(2).setCellValue(supplier.getEmail());
                row.createCell(3).setCellValue(supplier.getNumber());
            }

            // Write the updated workbook back to the file
            workbook.write(fileOut);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToJson(List<Supplier> supplier) {
        // Write the updated supplierList to the JSON file
        // Include error handling as needed

        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(writer, supplier);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSupplier (Supplier supplier){
        supplierList.add(supplier);
        updateTable(supplierList);
        writeToExcel(supplierList);
        writeToJson(supplierList);

    }
    public void Deleter (int index){
        System.out.println(index);
        supplierList.remove(index);
        writeToJson(supplierList);
        writeToExcel(supplierList);
        updateTable(supplierList);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
        });
        jPanel2.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 760, 30));

        jTabbedPane1.setForeground(new java.awt.Color(0, 102, 51));
        jTabbedPane1.setFont(new java.awt.Font("Tw Cen MT", 1, 14)); // NOI18N
        jTabbedPane1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTabbedPane1KeyPressed(evt);
            }
        });

        jTable2.setFont(new java.awt.Font("Tw Cen MT", 0, 12)); // NOI18N
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
        jTable2.setGridColor(new java.awt.Color(255, 255, 255));
        jTable2.setSelectionBackground(new java.awt.Color(0, 102, 51));
        jTable2.setSelectionForeground(new java.awt.Color(255, 255, 255));
        jScrollPane2.setViewportView(jTable2);

        jTabbedPane1.addTab("Supplier Database", jScrollPane2);

        jScrollPane3.setViewportView(jTabbedPane1);

        jPanel2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 760, 310));

        jButton1.setFont(new java.awt.Font("Tw Cen MT", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(0, 102, 51));
        jButton1.setText("Add");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 380, 160, 40));

        jButton3.setFont(new java.awt.Font("Tw Cen MT", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(0, 102, 51));
        jButton3.setText("Save");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 380, 160, 40));

        jButton4.setFont(new java.awt.Font("Tw Cen MT", 1, 12)); // NOI18N
        jButton4.setForeground(new java.awt.Color(0, 102, 51));
        jButton4.setText("Reset");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 380, 160, 40));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
        String text = jTextField1.getText();
        if (!text.isEmpty()) {
             searchSuppliers(text);
        } else {
            resetTable();
        }
        
        
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:    
        SupplierEntry Add = new SupplierEntry( ContactList.this);
        Add.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        writeToExcel(supplierList);
        writeToJson(supplierList);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        tb_load();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTabbedPane1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTabbedPane1KeyPressed
        // TODO add your handling code here:
       
        
    }//GEN-LAST:event_jTabbedPane1KeyPressed

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        // TODO add your handling code here:
        
        String text = jTextField1.getText();
        
        if (!text.isEmpty()) {
             searchSuppliers(text);
        } else {
            resetTable();
        }
        
    }//GEN-LAST:event_jTextField1KeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
