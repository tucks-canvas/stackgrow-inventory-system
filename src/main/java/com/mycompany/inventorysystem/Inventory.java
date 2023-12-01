/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

package com.mycompany.inventorysystem;

import java.awt.CardLayout;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import java.util.Comparator;
import javax.swing.table.TableRowSorter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.*;
import java.io.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.table.TableColumn;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 *
 * @author remya
 */
public class Inventory extends javax.swing.JPanel {
    
    /**
     * Creates new form Inventory
     */
    List<Product> productList = new ArrayList<>();
    String projectPath = System.getProperty("user.dir");
    String jsonFilePath = projectPath + "/Databases/product.json";
    String excelFilePath = projectPath + "/Databases/Database.xlsx";

    public Inventory() {
        initComponents();
        tb_load();
        addTableMouseListener(); 
        jTabbedPane1.addChangeListener(new ChangeListener() {
             @Override
                public void stateChanged(ChangeEvent e) {
                    if (jTabbedPane1.getSelectedIndex() == 1 ) {
                    displayDailySalesCards();
                    
                    }else if( jTabbedPane1.getSelectedIndex() == 2){
                    displayWeeklySalesCards();
                }
                }
        });

    }
    
    private void tb_load() {
    try {
        
         productList = retrieveProductData();

        DefaultTableModel model = new DefaultTableModel()
         {
             @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells non-editable
                return false;
            }
         
            @Override
                public Class<?> getColumnClass(int column) {
                switch (column) {
                case 0:
                    return Integer.class;
                case 1:
                case 2:
                    return String.class;
                case 3:
                    return Float.class;
                case 4:
                    return Integer.class;
                default:
                    return Object.class;
                }
            }
         };


        model.setRowCount(0); // Clear existing rows
        model.setColumnCount(0);

        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Type");
        model.addColumn("Price");
        model.addColumn("Stock");

        for (Product product : productList) {
            Vector<Object> row = new Vector<>();
            row.add((int)(product.getId())); 
            row.add(product.getName());
            row.add(product.getType());
            row.add( product.getRetailCost());
            row.add(product.getStock());
            model.addRow(row);
        }

        jTable2.setModel(model); // Set the updated model

        jTable2.setEnabled(true);
        jTable2.setFocusable(true);
        jTable2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        // Enable sorting with custom comparator for the numeric columns
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        jTable2.setRowSorter(sorter);
        sorter.setComparator(0, Comparator.<Integer, Integer>comparing(Integer::valueOf));
        
        TableColumn idColumn = jTable2.getColumnModel().getColumn(0);
        TableColumn Column_2 = jTable2.getColumnModel().getColumn(1);
        TableColumn Column_3 = jTable2.getColumnModel().getColumn(2);
        TableColumn Column_4 = jTable2.getColumnModel().getColumn(3);
        TableColumn Column_5 = jTable2.getColumnModel().getColumn(4);

        
        idColumn.setPreferredWidth(2);
        Column_2.setPreferredWidth(2);
        Column_3.setPreferredWidth(2);
        Column_4.setPreferredWidth(2);
        Column_5.setPreferredWidth(2);

        
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
                    int idColumnIndex = 0;

                    // Check if the column index is valid
                    if (jTable2.getColumnCount() > idColumnIndex) {
                        int id = Integer.parseInt(jTable2.getValueAt(selectedRow, idColumnIndex).toString());

                        // Retrieve the selected product from the product list
                        Product selectedProduct = getProductById(id);
                        int index = productList.indexOf(selectedProduct);
                        System.out.println(index);

                        // Open a new detail form with the selected data
                        ProductView editWindow = new ProductView( index,selectedProduct, Inventory.this);
                        editWindow.setVisible(true);
                    }
                }
            }
        });
    }

    private Product getProductById(int id) {
        
        for (Product product : productList) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null; 
    }
    
    public static boolean doesFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }
    
     

    

    



    private List<Product> retrieveProductData() {
    List<Product> productList = new ArrayList<>();  


    if (doesFileExist(jsonFilePath)) {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            productList = new ArrayList<>(Arrays.asList(objectMapper.readValue(reader, Product[].class)));
         } catch (IOException e) {
            e.printStackTrace();
        }
    } else {
        System.out.println("The JSON file does not exist.");

        // Code to read data from Excel file
       

        productList.addAll(readFromExcel());
    }

    return productList;
}

public List<Product> readFromExcel(String excelFilePath) {
    List<Product> productList = new ArrayList<>();

    try (FileInputStream file = new FileInputStream(excelFilePath)) {
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        int startRow = 1; // represents the 2nd row
        Row row = sheet.getRow(0);
        String[] expectedColumnNames = {"ID" ,   "Name"  ,  "Type" ,   "Description",    "Weight (kg)",    "Brand",    "Stock" ,   "Retail Cost",    "Wholesale Cost" ,"Supplier"};
            for (int i = 0; i < expectedColumnNames.length; i++) {
                Cell cell = row.getCell(i);
                if (cell == null || !cell.getStringCellValue().trim().equals(expectedColumnNames[i])) {

                    JOptionPane.showMessageDialog(this, "Columns must have headings in the format of \n (ID    Name    Type    Description    Weight (kg)    Brand    Stock    Retail Cost  Wholesale Cost  Supplier)");
                    return(readFromExcel());
                }
            }
        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            row = sheet.getRow(rowIndex);
            // Assuming your Excel file has columns in the order: ID, Name, Type, Description, Weight, Brand, Quantity, Price, Wholesale, Supplier
            Cell idCell = row.getCell(0);
            Cell nameCell = row.getCell(1);
            Cell typeCell = row.getCell(2);
            Cell descriptionCell = row.getCell(3);
            Cell weightCell = row.getCell(4);
            Cell brandCell = row.getCell(5);
            Cell quantityCell = row.getCell(6);
            Cell priceCell = row.getCell(7);
            Cell wholeCell = row.getCell(8);
            Cell suppCell = row.getCell(9);

            if (!(getCellValueAsString(nameCell) == null && getCellValueAsString(typeCell) == null && getCellValueAsString(descriptionCell) == null && getCellValueAsString(weightCell) == null && getCellValueAsString(brandCell) == null && getCellValueAsString(suppCell) == null)) {
    int id = (int) idCell.getNumericCellValue();
    String name = getCellValueAsString(nameCell);
    String type = getCellValueAsString(typeCell);
    String description = getCellValueAsString(descriptionCell);
    String weight = getCellValueAsString(weightCell);
    String brand = getCellValueAsString(brandCell);
    int quantity = (int) quantityCell.getNumericCellValue();
    float price = (float) priceCell.getNumericCellValue();
    float wholesale = (float) wholeCell.getNumericCellValue();
    String supplier = getCellValueAsString(suppCell);

    Product product = new Product(id, name, type, description, weight, brand, quantity, price, wholesale, supplier);
    boolean foundExistingProduct = false;

    for (Product existingProduct : productList) {
        System.out.println(existingProduct.getName());
        if (existingProduct.getName().equals(product.getName())) {
            foundExistingProduct = true;

            Object[] options = {"YES", "NO"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Would you like to update the entry: "+ existingProduct.getName()+"?", "Product with the same attributes already exists.",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                existingProduct.setType(product.getType());
                existingProduct.setDescription(product.getDescription());
                existingProduct.setWeight(product.getWeight());
                existingProduct.setBrand(product.getBrand());
                existingProduct.setStock(product.getStock());
                existingProduct.setRetailCost(product.getRetailCost());
                existingProduct.setWholesaleCost(product.getWholesaleCost());
                existingProduct.setSupplier(product.getSupplier());
            } else {
            }

            break;
        }
    }

    if (!foundExistingProduct) {
        productList.add(product);
    }
}
        }

        workbook.close();
        JOptionPane.showMessageDialog(this, "Products imported successfully!");
    } catch (IOException e) {
        e.printStackTrace();
    }

    return productList;
}
    public List<Product> readFromExcel() {
        List<Product> productList = new ArrayList<>();
        try (FileInputStream file = new FileInputStream(excelFilePath)) {
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 1; // represents the 2nd row
            for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                // Assuming your Excel file has columns in the order: ID, Name, Type, Description, Weight, Brand, Quantity, Price, Wholesale, Supplier
                Cell idCell = row.getCell(0);
                Cell nameCell = row.getCell(1);
                Cell typeCell = row.getCell(2);
                Cell descriptionCell = row.getCell(3);
                Cell weightCell = row.getCell(4);
                Cell brandCell = row.getCell(5);
                Cell quantityCell = row.getCell(6);
                Cell priceCell = row.getCell(7);
                Cell wholeCell = row.getCell(8);
                Cell suppCell = row.getCell(9);

                int id = (int) idCell.getNumericCellValue();
                System.out.println(id);
                String name = getCellValueAsString(nameCell);
                System.out.println(name);
                String type = getCellValueAsString(typeCell);
                System.out.println(type);
                String description = getCellValueAsString(descriptionCell);
                System.out.println(description);
                String weight = getCellValueAsString(weightCell);
                String brand = getCellValueAsString(brandCell);
                int quantity = (int) quantityCell.getNumericCellValue();
                float price = (float) priceCell.getNumericCellValue();
                float wholesale = (float) wholeCell.getNumericCellValue();
                String supplier = getCellValueAsString(suppCell);

                Product product = new Product(id, name, type, description, weight, brand, quantity, price, wholesale, supplier);
                productList.add(product);
            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return productList;
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
    public void searchProducts(String searchTerm) {
    
    productList = retrieveProductData();
    List<Product> searchResults = new ArrayList<>();

    for (Product product : productList) {
            // Check if any attribute contains the search term (case-insensitive)
            if (containsIgnoreCase(product.getName(), searchTerm) ||
            containsIgnoreCase(product.getType(), searchTerm) ||
            containsIgnoreCase(product.getDescription(), searchTerm) ||
            containsIgnoreCase(product.getWeight(), searchTerm) ||
            containsIgnoreCase(product.getBrand(), searchTerm)) {
            searchResults.add(product);
        }

     updateTable(searchResults);
    }
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
    public void updateTable(List<Product> productList) {
    DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
    model.setRowCount(0); // Clear the existing rows

    if (productList != null) {
        for (Product product : productList) {
            Vector<Object> row = new Vector<>();
            row.add((int)(product.getId())); 
            row.add(product.getName());
            row.add(product.getType());
            row.add( product.getRetailCost());
            row.add(product.getStock());
            model.addRow(row);
        }
    }
}
    public void Editor (int index, Product product){
        System.out.println(index);
        productList.set(index, product);
        writeToJson(productList);
        writeToExcel(productList);
        updateTable(productList);
        displayDailySalesCards();
        displayWeeklySalesCards();
        System.out.println("This is the Sale: " + product.calculateDailySale());
        System.out.println("this is the " + product.getStockChangeHistory());
        
    }
    public void Deleter (int index){
        System.out.println(index);
        productList.remove(index);
        writeToJson(productList);
        writeToExcel(productList);
        updateTable(productList);
        
        
    }
    public List<Product> getProductList(){
        return productList;
}
    
    private void writeToExcel(List<Product> productList) {
        String projectPath = System.getProperty("user.dir");
        String excelFilePath = projectPath + "/Databases/Database.xlsx";
        try (FileInputStream fileIn = new FileInputStream(excelFilePath);
             Workbook workbook = WorkbookFactory.create(fileIn);
             FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Start row index for writing data (assuming your Excel file has headers in the first row)
            int startRow = 1;

            // Iterate through the productList and write to the Excel sheet
            for (int i = 0; i < productList.size(); i++) {
                Product product = productList.get(i);
                Row row = sheet.createRow(startRow + i);

                // Set cell values based on your product properties
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getType());
                row.createCell(3).setCellValue(product.getDescription());
                row.createCell(4).setCellValue(product.getWeight());
                row.createCell(5).setCellValue(product.getBrand());
                row.createCell(6).setCellValue(product.getStock());
                row.createCell(7).setCellValue(product.getRetailCost());
                row.createCell(8).setCellValue(product.getWholesaleCost());
                row.createCell(9).setCellValue(product.getSupplier());

            
            }

            // Write the updated workbook back to the file
            workbook.write(fileOut);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToExcel(List<Product> productList, String excelFilePath){
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
                sheet = workbook.createSheet("Product");
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                Row productHeader = sheet.createRow(0);
                productHeader.createCell(0).setCellValue("ID");
                productHeader.createCell(1).setCellValue("Name");
                productHeader.createCell(2).setCellValue("Type");
                productHeader.createCell(3).setCellValue("Description");
                productHeader.createCell(4).setCellValue("Weight (kg)");
                productHeader.createCell(5).setCellValue("Brand");
                productHeader.createCell(6).setCellValue("Stock");
                productHeader.createCell(7).setCellValue("Retail Cost");
                productHeader.createCell(8).setCellValue("Wholesale Cost");
                productHeader.createCell(9).setCellValue("Supplier");

                int startRow = 1;

            // Iterate through the productList and write to the Excel sheet
            for (int i = 0; i < productList.size(); i++) {
                Product product = productList.get(i);
                Row row = sheet.createRow(startRow + i);

                // Set cell values based on your product properties
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getType());
                row.createCell(3).setCellValue(product.getDescription());
                row.createCell(4).setCellValue(product.getWeight());
                row.createCell(5).setCellValue(product.getBrand());
                row.createCell(6).setCellValue(product.getStock());
                row.createCell(7).setCellValue(product.getRetailCost());
                row.createCell(8).setCellValue(product.getWholesaleCost());
                row.createCell(9).setCellValue(product.getSupplier());


            }

            // Write the updated workbook back to the file
            workbook.write(fileOut);
            JOptionPane.showMessageDialog(this, "Products exported successfully!");


            } catch (IOException e) {
            e.printStackTrace();
        }
    }
     private void writeToJson(List<Product> productList) {
        // Write the updated productList to the JSON file
        // Include error handling as needed
        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(writer, productList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void displayDailySalesCards() {
    jPanel1.removeAll(); // Remove all existing cards
     

    GridLayout gridLayout = new GridLayout(0, 1); // One column, variable rows
    jPanel1.setLayout(gridLayout);
    gridLayout.setHgap(5); // Set horizontal gap to 5 pixels
    gridLayout.setVgap(5); // Set vertical gap to 5 pixels
    for (Product product : productList) {
        System.out.println("THIS IS SALE " +product.calculateDailySale() );
        if (product.calculateDailySale() > 0) {
            JPanel card = new JPanel();
            card.setLayout(new GridLayout(3, 2)); // Adjust the layout as needed
            gridLayout.setVgap(2);
            card.add(new JLabel("Product Name:"));
            card.add(new JLabel(product.getName()));
            card.add(new JLabel("Quantity Sold:"));
            card.add(new JLabel(String.valueOf(product.calculateDailySale())));
            card.add(new JLabel("Total Sales:"));
            card.add(new JLabel(String.valueOf(product.calculateDailySale() * product.getRetailCost())));

            // Add the card to the card panel
            jPanel1.add(card);
        }
    }

    // Refresh the panel to reflect the changes
    jPanel1.revalidate();
    jPanel1.repaint();
}
    
   private void displayWeeklySalesCards() {
    jPanel3.removeAll(); // Remove all existing cards
   
    

    GridLayout gridLayout = new GridLayout(0, 1); // One column, variable rows
    gridLayout.setHgap(5); // Set horizontal gap to 5 pixels
    gridLayout.setVgap(5); // Set vertical gap to 5 pixels
    jPanel3.setLayout(gridLayout);
    

    for (Product product : productList) {
        if (product.calculateWeeklySales() > 0) {
            JPanel card = new JPanel();
            card.setLayout(new GridLayout(3, 2)); // Adjust the layout as needed
             gridLayout.setVgap(2);
            card.add(new JLabel("Product Name:"));
            card.add(new JLabel(product.getName()));
            card.add(new JLabel("Quantity Sold:"));
            card.add(new JLabel(String.valueOf(product.calculateWeeklySales())));
            card.add(new JLabel("Total Sales:"));
            card.add(new JLabel(String.valueOf(product.calculateWeeklySales() * product.getRetailCost())));

            // Add the card to the card panel
            jPanel3.add(card);
        }
    }

    // Refresh the panel to reflect the changes
    jPanel3.revalidate();
    jPanel3.repaint();
}
    public void addProduct (Product product){
        productList.add(product);
        updateTable(productList);
        writeToExcel(productList);
        writeToJson(productList);


    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton4.setText("Reset");
        buttonGroup1.add(jButton4);
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

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton3.setText("Save");
        buttonGroup1.add(jButton3);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton1.setText("Add");
        buttonGroup1.add(jButton1);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addGap(18, 18, 18)
                .addComponent(jButton4)
                .addGap(62, 62, 62)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3)
                    .addComponent(jButton1))
                .addGap(0, 10, Short.MAX_VALUE))
        );

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 748, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 229, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 663, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Inventory Database", jPanel5);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 977, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 675, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Daily Sales Report", jPanel1);

        jPanel3.setLayout(new java.awt.CardLayout());
        jTabbedPane1.addTab("Weekly Sales Report", jPanel3);

        jScrollPane3.setViewportView(jTabbedPane1);
        jTabbedPane1.getAccessibleContext().setAccessibleName("Inventory Database");
        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton2.setText("Search");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(153, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(jButton2)
                .addGap(148, 148, 148))
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 766, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(9, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        int ID = productList.size() + 1;
        ProductEntry Add = new ProductEntry(ID,Inventory.this);
        Add.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
          String text = jTextField1.getText();
        if (!text.isEmpty()) {
            System.out.println ("is it" + !text.isEmpty());
            searchProducts(text);
        } else {
            resetTable();        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        System.out.println(productList);
        writeToJson(productList);
        writeToExcel(productList);
        
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        tb_load();
    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
