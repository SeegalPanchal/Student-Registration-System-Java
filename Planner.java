/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import univ.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import static javax.script.ScriptEngine.FILENAME;
import univ.Course;
import database.*;
/**
 *
 * @author seega
 */
public class Planner extends javax.swing.JFrame implements Serializable {

    Student current;
    CourseCatalog catalog = new CourseCatalog();
    HashMap<String,ArrayList<Course>> degrees = new HashMap<>();
    MyConnection c = new MyConnection();
    
    /**
     * Creates new form Planner
     */
    public Planner() {
        initComponents();
        setup();
    }

    private void setup() {
        
        BCG bcg = new BCG();
        CS cs = new CS();
        SEng seng = new SEng();
        //catalog.initializeCatalog("src\\courselistA2.txt");
        catalog.initializeCatalog();
        System.out.println("Seperator");
        //loadRequired(JOptionPane.showInputDialog("Enter filename with degree courses: ")); // load the required courses for the degrees
        loadRequired("requiredCourses.txt"); // hard-coded, REMOVE LATER!!!!
        outputArea.setText(catalog.toString());
              
        studentOptions.setVisible(false);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        
        file.addSeparator();
        JMenu admin = new JMenu("Administrator");
        file.add(admin);
        
        JMenuItem adminCourse = new JMenuItem("Edit Courses");
        
        adminCourse.addActionListener((event) -> {
            setVisible(false);
            AdminFrame.setSize(755, 400);
            AdminFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            AdminFrame.setVisible(true);
            // set everything to disabled until they select a mode
            subjectBox1.setEnabled(false);
            subjectBox2.setEnabled(false);
            codeField1.setEnabled(false);
            codeField2.setEnabled(false);
            titleField.setEnabled(false);
            creditWeightBox.setEnabled(false);
            semesterOfferedBox.setEnabled(false);
            updateButton.setEnabled(false);
            updateButton1.setEnabled(false);
            subjectBox3.setEnabled(false);
            codeField3.setEnabled(false);
            
            
            
            liveUpdateAdmin();
            
        });
        admin.add(adminCourse);
        
        degrees.put("BCG", bcg.getRequiredCourses());
        degrees.put("CS", cs.getRequiredCourses());
        degrees.put("SEng", seng.getRequiredCourses());
        JMenuItem adminDegree = new JMenuItem("Edit Degrees");
        adminDegree.addActionListener((event)-> {
            // code on event here
            DegreeFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            DegreeFrame.setVisible(true);
            DegreeFrame.setSize(815, 250);
            setVisible(false);
            viewDegrees();
        });
        admin.add(adminDegree);
                
        file.addSeparator();
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener((event)->savePerformed());
        file.add(save);
        
        JMenuItem open = new JMenuItem("Load");
        open.addActionListener((event)->loadStudent());
        file.add(open);
        
        file.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener((event) -> System.exit(0));
        file.add(exit);
        
        menuBar.add(file);
        setJMenuBar(menuBar);
    }
    
    private void loadRequired(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        String line[] = null;
        try
        {
            // declare and instantiate the readers
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);

            // the first line from the CSV file
            String temp = br.readLine();
            while (temp != null) {
                // this splits the strings on commas, 
                // and if there is nothing there puts 
                // a blank string in the array (or null)
                // instead of ignoring it
                line = temp.trim().split(",", -1);

                // trim the leading whitespace
                for (int i = 0; i < line.length; i ++) {
                    line [i] = line[i].trim();
                }
                
                if (line[0].equals("BCG")) {
                    ArrayList<String> courses = new ArrayList<>();
                    for (int i = 1; i < line.length; i ++) {
                        courses.add(line[i]);
                    }
                    writeDegree("bcgcourses.ser", courses);
                }
                else if (line[0].equals("CS")) {
                    ArrayList<String> courses = new ArrayList<>();
                    for (int i = 1; i < line.length; i ++) {
                        courses.add(line[i]);
                    }
                    writeDegree("cscourses.ser", courses);
                } else if (line[0].equals("SEng")) {
                    ArrayList<String> courses = new ArrayList<>();
                    for (int i = 1; i < line.length; i ++) {
                        courses.add(line[i]);
                    }
                    writeDegree("sengcourses.ser", courses);
                }
                // continue reading
                temp = br.readLine();
            }

            // close the readers
            br.close();
            fr.close();
        }
        // input output exception, such as File Not Found exception
        catch(IOException e) 
        {
            // print the thrown error
            System.out.println("Error. IOException: " + e);
        }
    }
    
    private void loadDegree() {
        String deg = degreeBox.getSelectedItem().toString();
        switch (deg) {
            case "Bachelor of Computing (General)":
                current.setDegree(new BCG());
                break;
            case "Computer Science (Honours)":
                current.setDegree(new CS());
                break;
            case "Software Engineering (Honours)":
                current.setDegree(new SEng());
                break;
        }
    }
    
    private void writeDegree(String fileName, ArrayList<String> courses)
    {
        try {
            FileOutputStream fout = new FileOutputStream(fileName); 
            ObjectOutputStream out = new ObjectOutputStream(fout);
            
            // create an arrayList of Courses
            ArrayList<Course> temp = new ArrayList<>();
            for (String code: courses) {
                Course c = catalog.findCourse(code);
                if (c != null) {
                    temp.add(c);
                }
            }
            
            // write the courses to a ser file
            out.writeObject(temp);
            out.close();
            fout.close();
        }
        // catch IO excetions, such as File Not Found Exception
        catch (IOException e)
        {
            // print the thrown error
            System.out.println("Error. IOException: " + e);
        }
    }
    
    private void loadStudent() {
        int studentNumber = 0;
        try {
            studentNumber = Integer.parseInt(studentNumText.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,"Invalid student number entered.");
            return;
        }
        
        String name = firstNameText.getText() + "," + lastNameText.getText();
        
        current = new Student(firstNameText.getText(), lastNameText.getText(), studentNumber);
        current.loadStudent(catalog); // populate the transcript and plan
        if (current.getDegree() != null) {
            if (current.getDegree() instanceof BCG) { degreeBox.setSelectedIndex(0); }
            else if (current.getDegree() instanceof CS) { degreeBox.setSelectedIndex(1); }
            else if (current.getDegree() instanceof SEng) { degreeBox.setSelectedIndex(2); }
        }
        loadDegree();
        
        // if found
        firstNameText.setEnabled(true);
        lastNameText.setEnabled(true);
        studentNumText.setText("" + String.format("%07d", studentNumber));
        
        logInButton.setEnabled(false);
        logOutButton.setEnabled(true);
        saveButton.setEnabled(true);
        degreeBox.setEnabled(true);
        studentOptions.setVisible(true);
        studentNumText.setEnabled(false);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        AdminFrame = new javax.swing.JFrame();
        jLabel9 = new javax.swing.JLabel();
        subjectBox1 = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        titleField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        creditWeightBox = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        semesterOfferedBox = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        subjectBox2 = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        codeField1 = new javax.swing.JTextField();
        add = new javax.swing.JRadioButton();
        remove = new javax.swing.JRadioButton();
        update = new javax.swing.JRadioButton();
        codeField2 = new javax.swing.JTextField();
        updateButton = new javax.swing.JButton();
        updateButton1 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        adminOutput = new javax.swing.JTextArea();
        jLabel17 = new javax.swing.JLabel();
        subjectBox3 = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        codeField3 = new javax.swing.JTextField();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        DegreeFrame = new javax.swing.JFrame();
        jScrollPane2 = new javax.swing.JScrollPane();
        degreeOutput = new javax.swing.JTextArea();
        jLabel19 = new javax.swing.JLabel();
        degreeField = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        subjectBox4 = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        codeField4 = new javax.swing.JTextField();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        studentInfo = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        firstNameText = new javax.swing.JTextField();
        lastNameText = new javax.swing.JTextField();
        studentNumText = new javax.swing.JTextField();
        logInButton = new javax.swing.JButton();
        logOutButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        degreeBox = new javax.swing.JComboBox();
        studentOptions = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        subjectBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        codeField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        gradeField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        semesterBox = new javax.swing.JComboBox();
        yearField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        outputArea = new javax.swing.JTextArea();

        jLabel9.setText("Course Code:");

        subjectBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ACCT", "AGBU", "AGR", "AHSS", "ANSC", "ANTH", "AQUA", "ARAB", "ARTH", "ASCI", "AVC", "BADM", "BINF", "BIOC", "BIOL", "BIOM", "BIOP", "BIOT", "BOT", "BUS", "CCJP", "CDE", "CHEM", "CHIN", "CIS", "CLAS", "CLIN", "CME", "COOP", "CROP", "CRWR", "DAFL", "DAGR", "DENM", "DEQN", "DFN", "DHRT", "DTM", "DVT", "ECON", "ECS", "EDRD", "ENGG", "ENGL", "ENVB", "ENVM", "ENVS", "EQN", "EURO", "FARE", "FCSS", "FDNT", "FINA", "FOOD", "FRAN", "FREN", "FRHD", "FSQA", "GEOG", "GERM", "GERO", "GREK", "HHNS", "HISP", "HIST", "HK", "HORT", "HROB", "HTM", "HUMN", "IBIO", "IDEV", "IMPR", "INT", "IPS", "ISS", "ITAL", "JUST", "KIN", "LACS", "LARC", "LAT", "LEAD", "LING", "LRS", "LTS", "MATH", "MBG", "MCB", "MCM", "MCS", "MDST", "MGMT", "MICR", "MUSC", "NANO", "NEUR", "NRS", "NUTR", "OAGR", "PABI", "PATH", "PBIO", "PHIL", "PHYS", "PLNT", "POLS", "POPM", "PORT", "PSYC", "REAL", "RPD", "RST", "SART", "SCMA", "SOAN", "SOC", "SPAN", "THST", "TOX", "TRMH", "UNIV", "VETM", "WAT", "WLU", "WMST", "XSEN", "XSHR", "ZOO" }));

        jLabel10.setText("*");

        jLabel11.setText("Course Title:");

        jLabel12.setText("Credit Weight:");

        creditWeightBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0.00", "0.25", "0.50", "0.75", "1.00", "1.25", "1.50", "1.75", "2.00", "2.25", "2.50", "2.75", "3.00" }));

        jLabel13.setText("Semester Offered:");

        semesterOfferedBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F", "W", "B" }));

        jLabel14.setText("Prerequisite Course Code:");

        subjectBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ACCT", "AGBU", "AGR", "AHSS", "ANSC", "ANTH", "AQUA", "ARAB", "ARTH", "ASCI", "AVC", "BADM", "BINF", "BIOC", "BIOL", "BIOM", "BIOP", "BIOT", "BOT", "BUS", "CCJP", "CDE", "CHEM", "CHIN", "CIS", "CLAS", "CLIN", "CME", "COOP", "CROP", "CRWR", "DAFL", "DAGR", "DENM", "DEQN", "DFN", "DHRT", "DTM", "DVT", "ECON", "ECS", "EDRD", "ENGG", "ENGL", "ENVB", "ENVM", "ENVS", "EQN", "EURO", "FARE", "FCSS", "FDNT", "FINA", "FOOD", "FRAN", "FREN", "FRHD", "FSQA", "GEOG", "GERM", "GERO", "GREK", "HHNS", "HISP", "HIST", "HK", "HORT", "HROB", "HTM", "HUMN", "IBIO", "IDEV", "IMPR", "INT", "IPS", "ISS", "ITAL", "JUST", "KIN", "LACS", "LARC", "LAT", "LEAD", "LING", "LRS", "LTS", "MATH", "MBG", "MCB", "MCM", "MCS", "MDST", "MGMT", "MICR", "MUSC", "NANO", "NEUR", "NRS", "NUTR", "OAGR", "PABI", "PATH", "PBIO", "PHIL", "PHYS", "PLNT", "POLS", "POPM", "PORT", "PSYC", "REAL", "RPD", "RST", "SART", "SCMA", "SOAN", "SOC", "SPAN", "THST", "TOX", "TRMH", "UNIV", "VETM", "WAT", "WLU", "WMST", "XSEN", "XSHR", "ZOO" }));

        jLabel15.setText("*");

        jButton5.setText("Back");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        buttonGroup1.add(add);
        add.setText("Add");
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        buttonGroup1.add(remove);
        remove.setText("Remove");
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        buttonGroup1.add(update);
        update.setText("Update");
        update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateActionPerformed(evt);
            }
        });

        updateButton.setText("Update Course");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        updateButton1.setText("Update Prerequisite");
        updateButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButton1ActionPerformed(evt);
            }
        });

        jLabel16.setText("Mode:");

        adminOutput.setColumns(20);
        adminOutput.setRows(5);
        adminOutput.setEnabled(false);
        jScrollPane3.setViewportView(adminOutput);

        jLabel17.setText("Updated Course Code:");

        subjectBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ACCT", "AGBU", "AGR", "AHSS", "ANSC", "ANTH", "AQUA", "ARAB", "ARTH", "ASCI", "AVC", "BADM", "BINF", "BIOC", "BIOL", "BIOM", "BIOP", "BIOT", "BOT", "BUS", "CCJP", "CDE", "CHEM", "CHIN", "CIS", "CLAS", "CLIN", "CME", "COOP", "CROP", "CRWR", "DAFL", "DAGR", "DENM", "DEQN", "DFN", "DHRT", "DTM", "DVT", "ECON", "ECS", "EDRD", "ENGG", "ENGL", "ENVB", "ENVM", "ENVS", "EQN", "EURO", "FARE", "FCSS", "FDNT", "FINA", "FOOD", "FRAN", "FREN", "FRHD", "FSQA", "GEOG", "GERM", "GERO", "GREK", "HHNS", "HISP", "HIST", "HK", "HORT", "HROB", "HTM", "HUMN", "IBIO", "IDEV", "IMPR", "INT", "IPS", "ISS", "ITAL", "JUST", "KIN", "LACS", "LARC", "LAT", "LEAD", "LING", "LRS", "LTS", "MATH", "MBG", "MCB", "MCM", "MCS", "MDST", "MGMT", "MICR", "MUSC", "NANO", "NEUR", "NRS", "NUTR", "OAGR", "PABI", "PATH", "PBIO", "PHIL", "PHYS", "PLNT", "POLS", "POPM", "PORT", "PSYC", "REAL", "RPD", "RST", "SART", "SCMA", "SOAN", "SOC", "SPAN", "THST", "TOX", "TRMH", "UNIV", "VETM", "WAT", "WLU", "WMST", "XSEN", "XSHR", "ZOO" }));

        jLabel18.setText("*");

        jButton21.setText("Repopulate");
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        jButton22.setText("Save");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout AdminFrameLayout = new javax.swing.GroupLayout(AdminFrame.getContentPane());
        AdminFrame.getContentPane().setLayout(AdminFrameLayout);
        AdminFrameLayout.setHorizontalGroup(
            AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AdminFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AdminFrameLayout.createSequentialGroup()
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(creditWeightBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(AdminFrameLayout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton22))
                    .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(AdminFrameLayout.createSequentialGroup()
                            .addComponent(jLabel13)
                            .addGap(18, 18, 18)
                            .addComponent(semesterOfferedBox, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(AdminFrameLayout.createSequentialGroup()
                            .addComponent(jLabel14)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(subjectBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel15)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(codeField2, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(AdminFrameLayout.createSequentialGroup()
                        .addComponent(updateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateButton1))
                    .addGroup(AdminFrameLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(add)
                        .addGap(18, 18, 18)
                        .addComponent(remove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(update))
                    .addGroup(AdminFrameLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(subjectBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(jLabel18)
                        .addGap(3, 3, 3)
                        .addComponent(codeField3, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(AdminFrameLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(subjectBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(codeField1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                .addContainerGap())
        );
        AdminFrameLayout.setVerticalGroup(
            AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdminFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AdminFrameLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(subjectBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)
                            .addComponent(codeField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(subjectBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(codeField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18))
                        .addGap(18, 18, 18)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(creditWeightBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(semesterOfferedBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(subjectBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(codeField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(updateButton)
                            .addComponent(updateButton1))
                        .addGap(18, 18, 18)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(add)
                            .addComponent(remove)
                            .addComponent(update))
                        .addGap(18, 18, 18)
                        .addGroup(AdminFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton5)
                            .addComponent(jButton21)
                            .addComponent(jButton22)))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );

        degreeOutput.setColumns(20);
        degreeOutput.setRows(5);
        jScrollPane2.setViewportView(degreeOutput);

        jLabel19.setText("Degree Name:");

        jLabel20.setText("Required:");

        subjectBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ACCT", "AGBU", "AGR", "AHSS", "ANSC", "ANTH", "AQUA", "ARAB", "ARTH", "ASCI", "AVC", "BADM", "BINF", "BIOC", "BIOL", "BIOM", "BIOP", "BIOT", "BOT", "BUS", "CCJP", "CDE", "CHEM", "CHIN", "CIS", "CLAS", "CLIN", "CME", "COOP", "CROP", "CRWR", "DAFL", "DAGR", "DENM", "DEQN", "DFN", "DHRT", "DTM", "DVT", "ECON", "ECS", "EDRD", "ENGG", "ENGL", "ENVB", "ENVM", "ENVS", "EQN", "EURO", "FARE", "FCSS", "FDNT", "FINA", "FOOD", "FRAN", "FREN", "FRHD", "FSQA", "GEOG", "GERM", "GERO", "GREK", "HHNS", "HISP", "HIST", "HK", "HORT", "HROB", "HTM", "HUMN", "IBIO", "IDEV", "IMPR", "INT", "IPS", "ISS", "ITAL", "JUST", "KIN", "LACS", "LARC", "LAT", "LEAD", "LING", "LRS", "LTS", "MATH", "MBG", "MCB", "MCM", "MCS", "MDST", "MGMT", "MICR", "MUSC", "NANO", "NEUR", "NRS", "NUTR", "OAGR", "PABI", "PATH", "PBIO", "PHIL", "PHYS", "PLNT", "POLS", "POPM", "PORT", "PSYC", "REAL", "RPD", "RST", "SART", "SCMA", "SOAN", "SOC", "SPAN", "THST", "TOX", "TRMH", "UNIV", "VETM", "WAT", "WLU", "WMST", "XSEN", "XSHR", "ZOO" }));

        jLabel21.setText("*");

        jButton16.setText("Add Degree");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        jButton17.setText("Add Required Course");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jButton18.setText("Back");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jButton19.setText("Remove Required Course");
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });

        jButton20.setText("Remove Degree");
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout DegreeFrameLayout = new javax.swing.GroupLayout(DegreeFrame.getContentPane());
        DegreeFrame.getContentPane().setLayout(DegreeFrameLayout);
        DegreeFrameLayout.setHorizontalGroup(
            DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DegreeFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(DegreeFrameLayout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(degreeField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DegreeFrameLayout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(subjectBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codeField4))
                    .addGroup(DegreeFrameLayout.createSequentialGroup()
                        .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                .addContainerGap())
        );
        DegreeFrameLayout.setVerticalGroup(
            DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DegreeFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DegreeFrameLayout.createSequentialGroup()
                        .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(degreeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(subjectBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21)
                            .addComponent(codeField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton16)
                            .addComponent(jButton17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(DegreeFrameLayout.createSequentialGroup()
                                .addGap(0, 54, Short.MAX_VALUE)
                                .addComponent(jButton18))
                            .addGroup(DegreeFrameLayout.createSequentialGroup()
                                .addGroup(DegreeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton19)
                                    .addComponent(jButton20))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("First Name:");

        jLabel2.setText("Last Name:");

        jLabel3.setText("Student Number:");

        logInButton.setText("Login");
        logInButton.setToolTipText("\"Enter a student number and hit log in\"");
        logInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logInButtonActionPerformed(evt);
            }
        });

        logOutButton.setText("Log Out");
        logOutButton.setEnabled(false);
        logOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logOutButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("Degree:");

        degreeBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bachelor of Computing (General)", "Computer Science (Honours)", "Software Engineering (Honours)" }));
        degreeBox.setEnabled(false);
        degreeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                degreeBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout studentInfoLayout = new javax.swing.GroupLayout(studentInfo);
        studentInfo.setLayout(studentInfoLayout);
        studentInfoLayout.setHorizontalGroup(
            studentInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentInfoLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(studentInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel8))
                .addGap(12, 12, 12)
                .addGroup(studentInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(studentInfoLayout.createSequentialGroup()
                        .addComponent(studentNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(studentInfoLayout.createSequentialGroup()
                        .addComponent(firstNameText, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(logInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(studentInfoLayout.createSequentialGroup()
                        .addComponent(lastNameText, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(logOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(degreeBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        studentInfoLayout.setVerticalGroup(
            studentInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentInfoLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(studentInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(firstNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logInButton))
                .addGap(18, 18, 18)
                .addGroup(studentInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lastNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logOutButton))
                .addGap(15, 15, 15)
                .addGroup(studentInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(studentNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton))
                .addGap(18, 18, 18)
                .addGroup(studentInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(degreeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setText("Course Subject:");

        subjectBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ACCT", "AGBU", "AGR", "AHSS", "ANSC", "ANTH", "AQUA", "ARAB", "ARTH", "ASCI", "AVC", "BADM", "BINF", "BIOC", "BIOL", "BIOM", "BIOP", "BIOT", "BOT", "BUS", "CCJP", "CDE", "CHEM", "CHIN", "CIS", "CLAS", "CLIN", "CME", "COOP", "CROP", "CRWR", "DAFL", "DAGR", "DENM", "DEQN", "DFN", "DHRT", "DTM", "DVT", "ECON", "ECS", "EDRD", "ENGG", "ENGL", "ENVB", "ENVM", "ENVS", "EQN", "EURO", "FARE", "FCSS", "FDNT", "FINA", "FOOD", "FRAN", "FREN", "FRHD", "FSQA", "GEOG", "GERM", "GERO", "GREK", "HHNS", "HISP", "HIST", "HK", "HORT", "HROB", "HTM", "HUMN", "IBIO", "IDEV", "IMPR", "INT", "IPS", "ISS", "ITAL", "JUST", "KIN", "LACS", "LARC", "LAT", "LEAD", "LING", "LRS", "LTS", "MATH", "MBG", "MCB", "MCM", "MCS", "MDST", "MGMT", "MICR", "MUSC", "NANO", "NEUR", "NRS", "NUTR", "OAGR", "PABI", "PATH", "PBIO", "PHIL", "PHYS", "PLNT", "POLS", "POPM", "PORT", "PSYC", "REAL", "RPD", "RST", "SART", "SCMA", "SOAN", "SOC", "SPAN", "THST", "TOX", "TRMH", "UNIV", "VETM", "WAT", "WLU", "WMST", "XSEN", "XSHR", "ZOO" }));

        jLabel4.setText("Course Code:");

        jLabel6.setText("Attempt Grade:");

        jLabel7.setText("Semester Taken:");

        semesterBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F", "W", "S" }));

        jButton1.setText("Check Transcript");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Required Not In Plan/Transcript");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton6.setText("View Plan");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("View Requirements");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Update/Add to Plan");
        jButton8.setToolTipText("");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("Update/Add to Transcript");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("View Transcript");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setText("Remove from Plan");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setText("Remove from Transcript");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton13.setText("Check GPA");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setText("View Prerequisites");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jButton3.setText("Check Credits");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Required Not In Transcript");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton15.setText("Prerequisites Required");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jButton23.setText("Change Grade");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout studentOptionsLayout = new javax.swing.GroupLayout(studentOptions);
        studentOptions.setLayout(studentOptionsLayout);
        studentOptionsLayout.setHorizontalGroup(
            studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentOptionsLayout.createSequentialGroup()
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(studentOptionsLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(studentOptionsLayout.createSequentialGroup()
                                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel6))
                                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(studentOptionsLayout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(subjectBox, 0, 119, Short.MAX_VALUE))
                                    .addGroup(studentOptionsLayout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(codeField))))
                            .addGroup(studentOptionsLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(studentOptionsLayout.createSequentialGroup()
                                        .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(studentOptionsLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(studentOptionsLayout.createSequentialGroup()
                                .addComponent(gradeField, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton23))
                            .addGroup(studentOptionsLayout.createSequentialGroup()
                                .addComponent(semesterBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yearField, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        studentOptionsLayout.setVerticalGroup(
            studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(subjectBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(codeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gradeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jButton23))
                .addGap(4, 4, 4)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(semesterBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yearField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton8)
                    .addComponent(jButton9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jButton12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6)
                    .addComponent(jButton10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton13)
                    .addComponent(jButton15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        outputArea.setColumns(20);
        outputArea.setRows(5);
        outputArea.setEnabled(false);
        jScrollPane1.setViewportView(outputArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(studentInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(studentOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 533, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(studentInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                        .addComponent(studentOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logInButtonActionPerformed
        loadStudent();
    }//GEN-LAST:event_logInButtonActionPerformed

    private void savePerformed() {
        current.saveState(catalog);
    }
    
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        savePerformed();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void logOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logOutButtonActionPerformed
              
        // reset
        studentNumText.setEnabled(true);
        firstNameText.setEnabled(true);
        firstNameText.setText("");
        lastNameText.setEnabled(true);
        lastNameText.setText("");
        studentNumText.setText("");
        degreeBox.setEnabled(false);
        
        logInButton.setEnabled(true);
        logOutButton.setEnabled(false);
        saveButton.setEnabled(false);
        studentOptions.setVisible(false);
    }//GEN-LAST:event_logOutButtonActionPerformed

    private void viewList(ArrayList<Attempt> given) {
        if (given == null || given.isEmpty()) {
            outputArea.setText("List is empty.");
            return;
        }
        
        // bubble sort
        for (int i = given.size()-1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {
                Attempt a = given.get(j);
                Attempt b = given.get(j+1);
                if (a.getSemesterTaken().compareToIgnoreCase(b.getSemesterTaken()) >= 0) {
                    given.set(j, b);
                    given.set(j+1, a);
                }
            }
        }
        
        String output = "";
        for (Attempt a: given) {
            output += a;
        }
        
        outputArea.setText(output);
    }
    
    // view list button
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        viewList(current.getPlanned());
    }//GEN-LAST:event_jButton6ActionPerformed

    // add to plan button
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // add to the plan
        if (codeField.getText() == null || codeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered a course code!");
            return;
        }
        try {
            int year = Integer.parseInt(codeField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the course code box!");
            return;
        }
        
        if (yearField.getText() == null || yearField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered year for the semester you plan to take the course!");
        }
        try {
            Integer.parseInt(yearField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the semester box!");
            return;
        }
        
        String courseCode = subjectBox.getSelectedItem().toString() + "*" + codeField.getText();
        String semesterTaken = semesterBox.getSelectedItem().toString() + yearField.getText();
        Course c = catalog.findCourse(courseCode);
        if (c == null) {
            JOptionPane.showMessageDialog(null, "Course not found.");
            return;
        } 
        current.addPlanned(new Attempt(semesterTaken, c));
        viewList(current.getPlanned());
    }//GEN-LAST:event_jButton8ActionPerformed

    // remove from plan button
    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // remove from plan
        if (codeField.getText() == null || codeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered a course code!");
            return;
        }
        try {
            int year = Integer.parseInt(codeField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the course code box!");
            return;
        }
        
        if (yearField.getText() == null || yearField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered year for the semester you plan to take the course!");
        }
        try {
            Integer.parseInt(yearField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the semester box!");
            return;
        }
        String courseCode = subjectBox.getSelectedItem().toString() + "*" + codeField.getText();
        String semesterTaken = semesterBox.getSelectedItem().toString() + yearField.getText();
        Course c = catalog.findCourse(courseCode);
        if (c == null) {
            JOptionPane.showMessageDialog(null, "Course not found.");
            return;
        } 
        current.removePlanned(new Attempt(semesterTaken, c));
        viewList(current.getPlanned());
    }//GEN-LAST:event_jButton11ActionPerformed

    // add to transcript buttom
    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // ADD TO TRANSCRIPT
        if (codeField.getText() == null || codeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered a course code!");
            return;
        }
        try {
            int year = Integer.parseInt(codeField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the course code box!");
            return;
        }
        
        if (yearField.getText() == null || yearField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered year for the semester you plan to take the course!");
        }
        try {
            Integer.parseInt(yearField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the semester box!");
            return;
        }
        
        String courseCode = subjectBox.getSelectedItem().toString() + "*" + codeField.getText();
        String semesterTaken = semesterBox.getSelectedItem().toString() + yearField.getText();
        Course c = catalog.findCourse(courseCode);
        if (c == null) {
            JOptionPane.showMessageDialog(null, "Course not found.");
            return;
        } 
        if (gradeField.getText() == null || gradeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Grade field is invalid.");
            return;
        }
        if (!gradeField.getText().equals("P") && !gradeField.getText().equals("F")
                && !gradeField.getText().equals("INC") && !gradeField.getText().equals("MNR"))
        {
            // its a number
            try {
                Integer.parseInt(gradeField.getText());
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Grade field is invalid.");
                return;
            }
        }
        current.addTranscript(new Attempt(gradeField.getText(), semesterTaken, c));
        viewList(current.getTranscript());
    }//GEN-LAST:event_jButton9ActionPerformed

    // view transcript buttom
    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        viewList(current.getTranscript());
    }//GEN-LAST:event_jButton10ActionPerformed

    // remove from transcript buttom
    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // remove from transcript
        if (codeField.getText() == null || codeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered a course code!");
            return;
        }
        try {
            int year = Integer.parseInt(codeField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the course code box!");
            return;
        }
        
        if (yearField.getText() == null || yearField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered year for the semester you plan to take the course!");
        }
        try {
            Integer.parseInt(yearField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the semester box!");
            return;
        }
        
        String courseCode = subjectBox.getSelectedItem().toString() + "*" + codeField.getText();
        String semesterTaken = semesterBox.getSelectedItem().toString() + yearField.getText();
        Course c = catalog.findCourse(courseCode);
        if (c == null) {
            JOptionPane.showMessageDialog(null, "Course not found.");
            return;
        } 
        if (gradeField.getText() == null || gradeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Grade field is invalid.");
            return;
        }
        if (!gradeField.getText().equals("P") && !gradeField.getText().equals("F")
                && !gradeField.getText().equals("INC") && !gradeField.getText().equals("MNR"))
        {
            // its a number
            try {
                Integer.parseInt(gradeField.getText());
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Grade field is invalid.");
                return;
            }
        }
        
        current.removeTranscript(new Attempt(gradeField.getText(), semesterTaken, c));
        viewList(current.getTranscript());
    }//GEN-LAST:event_jButton12ActionPerformed

    private void degreeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_degreeBoxActionPerformed
        loadDegree();
    }//GEN-LAST:event_degreeBoxActionPerformed

    // CHECK REQUIREMENTS
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // print the requirements of current degree
        String output = "";
        if (current.getDegree().getRequiredCourses() != null) {
            for (Course c: current.getDegree().getRequiredCourses()) {
                output += c + "\n";
            }
        }
        outputArea.setText(output);
    }//GEN-LAST:event_jButton7ActionPerformed

    // view prerequisites for a given course
    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        if (codeField.getText() == null || codeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered a course code!");
            return;
        }
        try {
            int year = Integer.parseInt(codeField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the course code box!");
            return;
        }
        
        String output = "";
        Course c = catalog.findCourse(subjectBox.getSelectedItem().toString() + "*" + codeField.getText());
        if (c == null) {
            JOptionPane.showMessageDialog(null, "Course does not exist!");
            return;
        }
        output = "Inputted course: \n" + c + "\nPrerequisites:\n";
        if (c.getPrerequisites() != null && !(c.getPrerequisites().isEmpty())) {
            for (Course i : c.getPrerequisites()) {
                output += i;
            }
        }
        outputArea.setText(output);
    }//GEN-LAST:event_jButton14ActionPerformed

    // CHECK CREDITS
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String output = "";

        // credits completed
        double creditsCompleted = 0;
        for (Attempt c : current.getTranscript()) {
            try {
                if (c.getAttemptGrade().equals("P")) {
                    creditsCompleted += c.getCourseAttempted().getCourseCredit();
                    continue;
                }
                double grade = Double.parseDouble(c.getAttemptGrade());
                if (grade < 50 || grade > 100) continue; // invalid grade
                creditsCompleted += c.getCourseAttempted().getCourseCredit();
            }
            catch (NumberFormatException nfe) {
                // grade is P/F/INC/MNR
                continue;
            }
        }
        
        output += "Credits completed: " + creditsCompleted + "\n";
        
        // credits remaining
        // credits completed
        ArrayList<Course> completed = new ArrayList<>();
        for (Attempt c : current.getTranscript()) {
            try {
                if (c.getAttemptGrade().equals("P")) {
                    completed.add(c.getCourseAttempted());
                    continue;
                }
                double grade = Double.parseDouble(c.getAttemptGrade());
                if (grade < 50 || grade > 100) continue; // invalid grade
                completed.add(c.getCourseAttempted());
            }
            catch (NumberFormatException nfe) {
                // grade is P/F/INC/MNR
                continue;
            }
        }
        output += "Credits remaining (checking transcript): " + current.getDegree().numberOfCreditsRemaining(completed);
        
        output += "\nCredits you need to add to your plan: ";
        ArrayList<Course> all = new ArrayList<>();
        if (current.getPlanned() != null) {
            for (Attempt p : current.getPlanned()) {
                all.add(p.getCourseAttempted());
            }
        }
        if (current.getTranscript() != null) {
            for (Attempt t : current.getTranscript()) {
                try {
                    if (t.getAttemptGrade().equals("P")) {
                        all.add(t.getCourseAttempted());
                        continue;
                    }
                    double grade = Double.parseDouble(t.getAttemptGrade());
                    if (grade < 50 || grade > 100) continue; // invalid grade
                    all.add(t.getCourseAttempted());
                }
                catch (NumberFormatException nfe) {
                    // grade is P/F/INC/MNR
                    continue;
                }
            }
        }
        output += current.getDegree().numberOfCreditsRemaining(all);
        
        outputArea.setText(output);
    }//GEN-LAST:event_jButton3ActionPerformed

    // check transcript
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String output = "";
        ArrayList<Course> completed = new ArrayList<>();
        if (current.getTranscript() != null) {
            for (Attempt c : current.getTranscript()) {
                try {
                    if (c.getAttemptGrade().equals("P")) {
                        completed.add(c.getCourseAttempted());
                        continue;
                    }
                    double grade = Double.parseDouble(c.getAttemptGrade());
                    if (grade < 50 || grade > 100) continue; // invalid grade
                    completed.add(c.getCourseAttempted());
                }
                catch (NumberFormatException nfe) {
                    // grade is P/F/INC/MNR
                    continue;
                }
            }
        }
             
        boolean meetsRequirement = current.getDegree().meetsRequirements(completed);
        if (meetsRequirement) {
            output += "Congratulations! You meet the requirements to graduate!";
        }
        else { output += "Sorry, you do not meet the requirements to graduate."; }
        outputArea.setText(output);
    }//GEN-LAST:event_jButton1ActionPerformed

    // CHECK GPA
    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        String output = "";
        ArrayList<Attempt> transcript = current.getTranscript();
        
        // sort the transcript (so we know we are looking at last 10
        // bubble sort
        for (int i = transcript.size()-1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {
                Attempt a = transcript.get(j);
                Attempt b = transcript.get(j+1);
                if (a.getSemesterTaken().compareToIgnoreCase(b.getSemesterTaken()) >= 0) {
                    transcript.set(j, b);
                    transcript.set(j+1, a);
                }
            }
        }
        
        // GPA overall
        double overallGPA = 0;
        double allCount = 0;
        for (Attempt a : transcript) {
            try {
                double grade = Double.parseDouble(a.getAttemptGrade());
                if (grade >= 50 && grade <= 100) {
                    overallGPA += grade;
                    allCount++;
                }
            }
            catch (NumberFormatException nfe) {}
        }
        
        if (allCount > 0) {
            overallGPA = overallGPA / allCount;
            output += "GPA Over All Courses: " + overallGPA + "\n";
        } else {
            output += "No credits earned.\n";
        }
        
        
        // GPA of CIS
        double cisGPA = 0;
        double cisCount = 0;
        for (Attempt a : transcript) {
            if (a.getCourseAttempted().getCourseCode().split("\\*")[0].equals("CIS")) {
                try {
                    double grade = Double.parseDouble(a.getAttemptGrade());
                    if (grade >= 50 && grade <= 100) {
                        cisGPA += grade;
                        cisCount++;
                    }
                }
                catch (NumberFormatException nfe) {}
            }
        }
        if (cisCount > 0) {
            cisGPA = cisGPA / cisCount;
            output += "GPA Over CIS Courses: " + cisGPA + "\n";
        } else {
            output += "No CIS credits have been earned.\n";
        }
        
        // GPA of Past 10
        double past10GPA = 0;
        double past10Count = 0;
        for (int i = transcript.size()-1; i > transcript.size()-11; i --) {
            if (i >= 0) {
                Attempt a = transcript.get(i);
                try {
                    double grade = Double.parseDouble(a.getAttemptGrade());
                    if (grade >= 50 && grade <= 100) {
                        past10GPA += grade;
                        past10Count++;
                    }
                }
                catch (NumberFormatException nfe) {}
            }
        }
        if(past10Count > 0) {
            past10GPA = past10GPA / past10Count;
            output += "GPA Over past 10 courses: " + past10GPA + "\n";
        } else {
            output += "No course credits have been earned.\n";
        }
        
        outputArea.setText(output);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // courses not represented in transcript
        ArrayList<Course> all = new ArrayList<>();
        if (current.getPlanned() != null) {
            for (Attempt p : current.getPlanned()) {
                all.add(p.getCourseAttempted());
            }
        }
        
        
        if (current.getTranscript() != null) {
            for (Attempt t : current.getTranscript()) {
                try {
                    if (t.getAttemptGrade().equals("P")) {
                        all.add(t.getCourseAttempted());
                        continue;
                    }
                    double grade = Double.parseDouble(t.getAttemptGrade());
                    if (grade < 50 || grade > 100) continue; // invalid grade
                    all.add(t.getCourseAttempted());
                }
                catch (NumberFormatException nfe) {
                    // grade is P/F/INC/MNR
                    continue;
                }
            }
        }
        
        String output = "Required courses not represented in transcript: \n";
        if (current.getDegree().remainingRequiredCourses(all) != null) {
            for (Course c : current.getDegree().remainingRequiredCourses(all)) {
                output += c + "\n";
            }
        }
        
        outputArea.setText(output);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // courses not represented in transcript
        ArrayList<Course> transcript = new ArrayList<>();
        if (current.getTranscript() != null) {
            for (Attempt t : current.getTranscript()) {
                try {
                    if (t.getAttemptGrade().equals("P")) {
                        transcript.add(t.getCourseAttempted());
                        continue;
                    }
                    double grade = Double.parseDouble(t.getAttemptGrade());
                    if (grade < 50 || grade > 100) continue; // invalid grade
                    transcript.add(t.getCourseAttempted());
                }
                catch (NumberFormatException nfe) {
                    // grade is P/F/INC/MNR
                    continue;
                }
            }
        }
        
        String output = "Required courses not represented in transcript: \n";
        if (current.getDegree().remainingRequiredCourses(transcript) != null) {
            for (Course c : current.getDegree().remainingRequiredCourses(transcript)) {
                output += c + "\n";
            }
        }
        
        outputArea.setText(output);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        String output = "Prerequisites needed to take courses in your plan:\n";
        ArrayList<Attempt> planned = current.getPlanned();
        ArrayList<Attempt> transcript = current.getTranscript();
        ArrayList<Course> notInPlan = new ArrayList<>();
        
        if (planned != null) {
            for (Attempt p : planned) { // each planned course
                ArrayList<Course> preReqs = p.getCourseAttempted().getPrerequisites();
                if (preReqs!=null) {
                    for (Course preReq : preReqs) { // for each prerequisite
                        // check if it appears in transcript
                        boolean found = false;
                        if (transcript!=null) {
                            for (Attempt t : transcript) {
                                if (preReq.getCourseCode().equals(t.getCourseAttempted().getCourseCode())) {
                                    found = true;
                                }
                            }
                            if (found == false) { // prereq not found in transcript
                                if (!notInPlan.contains(preReq)) {
                                   notInPlan.add(preReq);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (notInPlan != null && !notInPlan.isEmpty()) {
            for (Course n : notInPlan) {
                output += n + "\n";
            }
        }
        outputArea.setText(output);
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        setVisible(true);
        AdminFrame.setVisible(false);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void liveUpdateAdmin() {
        // get a list of the courses in catalog
        HashMap<String, Course> list = catalog.getCourseList();
        String output = "";
        if (catalog.getCourseList()!= null) {
            if (catalog.getCourseList().values() != null) {
                for (Course c : catalog.getCourseList().values()) {
                    output += c + "\n";
                }
            }
        }
        adminOutput.setText(output);
    }
    
    // add radio button selected
    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        subjectBox1.setEnabled(true);
        subjectBox2.setEnabled(true);
        codeField1.setEnabled(true);
        codeField2.setEnabled(true);
        titleField.setEnabled(true);
        creditWeightBox.setEnabled(true);
        semesterOfferedBox.setEnabled(true);   
        updateButton.setEnabled(true);
        updateButton1.setEnabled(true);
        subjectBox3.setEnabled(false);
        codeField3.setEnabled(false);
    }//GEN-LAST:event_addActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        subjectBox1.setEnabled(true);
        subjectBox2.setEnabled(true);
        codeField1.setEnabled(true);
        codeField2.setEnabled(true);
        titleField.setEnabled(false);
        creditWeightBox.setEnabled(false);
        semesterOfferedBox.setEnabled(false);
        updateButton.setEnabled(true);
        updateButton1.setEnabled(true);
        subjectBox3.setEnabled(false);
        codeField3.setEnabled(false);
    }//GEN-LAST:event_removeActionPerformed

    private void updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateActionPerformed
        subjectBox1.setEnabled(true);
        subjectBox2.setEnabled(false);
        codeField1.setEnabled(true);
        codeField2.setEnabled(false);
        titleField.setEnabled(true);
        creditWeightBox.setEnabled(true);
        semesterOfferedBox.setEnabled(true);
        updateButton.setEnabled(true);
        updateButton1.setEnabled(false);
        subjectBox3.setEnabled(true);
        codeField3.setEnabled(true);
    }//GEN-LAST:event_updateActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        liveUpdateAdmin();
        // 3 cases
        String code = null;
        if (add.isSelected()) {
            // get the course code
            try {
                int codeNumber = Integer.parseInt(codeField1.getText());
                if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
                code = subjectBox1.getSelectedItem().toString() + "*" + codeField1.getText();
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Invalid course code entered.");
                return;
            }
            // check course title
            String courseTitle = titleField.getText();
            if (courseTitle == null || courseTitle.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Invalid course title entered.");
                return;
            }
            double creditWeight = Double.parseDouble(creditWeightBox.getSelectedItem().toString()); // the credit weight
            char semesterOffered = semesterOfferedBox.getSelectedItem().toString().charAt(0); // the character
            // assume prereqs are null (they have manually update each preReq
            Course temp = new Course(code, courseTitle, creditWeight, null, semesterOffered);   
            catalog.addCourse(temp);
            liveUpdateAdmin();
        }
        else if (remove.isSelected()) {
            try {
                int codeNumber = Integer.parseInt(codeField1.getText());
                if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
                code = subjectBox1.getSelectedItem().toString() + "*" + codeField1.getText();
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Invalid course code entered.");
                return;
            }
            catalog.removeCourse(new Course(code));
            liveUpdateAdmin();
        }
        else if (update.isSelected()) {
            // get the course code
            try {
                int codeNumber = Integer.parseInt(codeField1.getText());
                if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
                code = subjectBox1.getSelectedItem().toString() + "*" + codeField1.getText();
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Invalid course code entered.");
                return;
            }
            Course temp = catalog.findCourse(code);
            if (temp == null) {
                JOptionPane.showMessageDialog(null, "Course not found.");
                return;
            }
            catalog.removeCourse(temp); // remove the course
            // try go get updated course code
            try {
                int codeNumber = Integer.parseInt(codeField3.getText());
                if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
                code = subjectBox3.getSelectedItem().toString() + "*" + codeField3.getText();
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Invalid course code entered.");
                catalog.addCourse(temp);
                return;
            }
            Course check = catalog.findCourse(code); // check if the course exists
            if (check != null) {
                JOptionPane.showMessageDialog(null, "Cant update to this code because it already exists.");
                catalog.addCourse(temp);
                return;
            }
            // get the other values
            String courseTitle = titleField.getText();
            double creditWeight = Double.parseDouble(creditWeightBox.getSelectedItem().toString());
            char semesterOffered = semesterOfferedBox.getSelectedItem().toString().charAt(0);
            // make a new course
            Course newCourse = new Course(code, courseTitle, creditWeight, null, semesterOffered);
            catalog.addCourse(newCourse);
            liveUpdateAdmin();
        }
        liveUpdateAdmin();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void updateButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButton1ActionPerformed
        liveUpdateAdmin();
        // 3 cases
        String code = null;
        try {
            int codeNumber = Integer.parseInt(codeField1.getText());
            if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
            code = subjectBox1.getSelectedItem().toString() + "*" + codeField1.getText();
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid course code entered.");
            return;
        }
        Course theCourse = catalog.findCourse(code); // find the course they want to remove preReqs from
        if (theCourse == null) {
            JOptionPane.showMessageDialog(null, "Course does not exist.");
            return;
        }
        if (add.isSelected()) {
            String preReqCode = null;
            try {
                int codeNumber = Integer.parseInt(codeField2.getText());
                if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
                preReqCode = subjectBox2.getSelectedItem().toString() + "*" + codeField2.getText();
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Invalid course code entered.");
                return;
            }
            
            // check if prereq exists
            Course preReq = catalog.findCourse(preReqCode);
            if (preReq == null) {
                JOptionPane.showMessageDialog(null, "Prerequisite code does not exist.");
                return;
            }
            
            ArrayList<Course> preReqList = theCourse.getPrerequisites();
            ArrayList<Course> newList = new ArrayList<>();
            boolean found = false;
            if (preReqList != null) {
                for (Course p : preReqList) {
                    newList.add(p);
                    if (p.getCourseCode().equals(preReqCode)) { found = true; }
                }
            }
            if (found == true) {
                JOptionPane.showMessageDialog(null, "Prerequisite already exists.");
            } else {
                newList.add(new Course(preReqCode));
            }
            Course copy = new Course(theCourse.getCourseCode(), theCourse.getCourseTitle(), theCourse.getCourseCredit(), newList, theCourse.getSemesterOffered());
            catalog.removeCourse(theCourse);
            catalog.addCourse(copy);
            liveUpdateAdmin();
            liveUpdateAdmin();
        }
        else if (remove.isSelected()) {
            
            String preReqCode = null;
            try {
                int codeNumber = Integer.parseInt(codeField2.getText());
                if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
                preReqCode = subjectBox2.getSelectedItem().toString() + "*" + codeField2.getText();
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Invalid course code entered.");
                return;
            }
            
            ArrayList<Course> preReqList = theCourse.getPrerequisites();
            ArrayList<Course> newList = new ArrayList<>();
            boolean found = false;
            if (preReqList != null) {
                for (Course p : preReqList) {
                    if(!p.getCourseCode().equals(preReqCode)) {
                        newList.add(p);
                    } else { found = true; }
                }
            }
            Course copy = new Course(theCourse.getCourseCode(), theCourse.getCourseTitle(), theCourse.getCourseCredit(), newList, theCourse.getSemesterOffered());
            if (found == true) JOptionPane.showMessageDialog(null, "Removed prerequisite.");
            catalog.removeCourse(theCourse);
            catalog.addCourse(copy);
            liveUpdateAdmin();
        }
        liveUpdateAdmin();
    }//GEN-LAST:event_updateButton1ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        DegreeFrame.setVisible(false);
        setVisible(true);
    }//GEN-LAST:event_jButton18ActionPerformed

    
    private void viewDegrees() {
        String output = "";
        for (String deg : degrees.keySet()) {
            if (degrees.get(deg) != null) {
                output += deg + ":\n" + Arrays.toString(degrees.get(deg).toArray()) + "\n\n\n";
            }
        }
        degreeOutput.setText(output);
    }
    
    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        // add the name to the hashmap
        ArrayList<Course> temp = new ArrayList<>();
        if (degrees.containsKey(degreeField.getText())) {
            JOptionPane.showMessageDialog(null, "Degree already exists.");
            return;
        }
        degrees.put(degreeField.getText(), temp);
        viewDegrees();
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        String code = null;
        
        if (!degrees.containsKey(degreeField.getText())) {
            JOptionPane.showMessageDialog(null, "Degree does not exist.");
            return;
        }
        
        try {
            int codeNumber = Integer.parseInt(codeField4.getText());
            if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
            code = subjectBox4.getSelectedItem().toString() + "*" + codeField4.getText();
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid course code entered.");
            return;
        }
        
        Course temp = catalog.findCourse(code);
        if (temp == null) {
            JOptionPane.showMessageDialog(null, "Course does not exist.");
        }
        
        // else get the arraylist of courses from degree and add it, or make a new one
        if (degrees.get(degreeField.getText()) == null || degrees.get(degreeField.getText()).isEmpty()) {
            ArrayList<Course> newList = new ArrayList<>();
            newList.add(temp);
            degrees.put(degreeField.getText(), newList);
        }
        else {
            for (Course c : degrees.get(degreeField.getText())) {
                if (c.getCourseCode().equals(temp.getCourseCode())) {
                    JOptionPane.showMessageDialog(null, "Already a prereq!");
                    return;
                }
            }
            degrees.get(degreeField.getText()).add(temp);
        }
        
        viewDegrees();
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
        if (!degrees.containsKey(degreeField.getText())) {
            JOptionPane.showMessageDialog(null, "Degree does not exist.");
            return;
        }
        degrees.remove(degreeField.getText());
        viewDegrees();
    }//GEN-LAST:event_jButton20ActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        String code = null;
        
        if (!degrees.containsKey(degreeField.getText())) {
            JOptionPane.showMessageDialog(null, "Degree does not exist.");
            return;
        }
        
        try {
            int codeNumber = Integer.parseInt(codeField4.getText());
            if (codeNumber < 1000 || codeNumber > 9999) throw new NumberFormatException(); // ease of use throw
            code = subjectBox4.getSelectedItem().toString() + "*" + codeField4.getText();
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid course code entered.");
            return;
        }
        
        // else get the arraylist of courses from degree and add it, or make a new one
        if (degrees.get(degreeField.getText()) == null || degrees.get(degreeField.getText()).isEmpty()) {
            JOptionPane.showMessageDialog(null, "There are no prerequisites.");
        }
        else {
            for (Course c : degrees.get(degreeField.getText())) {
                if (c.getCourseCode().equals(code)) {
                    degrees.get(degreeField.getText()).remove(c);
                    viewDegrees();
                    return;
                }
            }
            JOptionPane.showMessageDialog(null, "Course not found.");
        }
        
        viewDegrees();
    }//GEN-LAST:event_jButton19ActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        catalog.readSaveState();
        catalog.initializeCatalog();
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        catalog.saveCatalog();
    }//GEN-LAST:event_jButton22ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        // check that everything else matches
        if (codeField.getText() == null || codeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered a course code!");
            return;
        }
        try {
            int year = Integer.parseInt(codeField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the course code box!");
            return;
        }
        
        if (yearField.getText() == null || yearField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not entered year for the semester you plan to take the course!");
        }
        try {
            Integer.parseInt(yearField.getText());
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid year entry in the semester box!");
            return;
        }
        
        String courseCode = subjectBox.getSelectedItem().toString() + "*" + codeField.getText();
        String semesterTaken = semesterBox.getSelectedItem().toString() + yearField.getText();
        Course c = catalog.findCourse(courseCode);
        if (c == null) {
            JOptionPane.showMessageDialog(null, "Course not found.");
            return;
        } 
        if (gradeField.getText() == null || gradeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Grade field is invalid.");
            return;
        }
        if (!gradeField.getText().equals("P") && !gradeField.getText().equals("F")
                && !gradeField.getText().equals("INC") && !gradeField.getText().equals("MNR"))
        {
            // its a number
            try {
                Integer.parseInt(gradeField.getText());
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Grade field is invalid.");
                return;
            }
        }
        
        if (current.getTranscript() != null) {
            for (Attempt t : current.getTranscript()) {
                if (t.getCourseAttempted().equals(c) && semesterTaken.equals(t.getSemesterTaken())) {
                    t.setAttemptGrade(gradeField.getText());
                }
            }
        }
        viewList(current.getTranscript());
    }//GEN-LAST:event_jButton23ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Planner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Planner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Planner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Planner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Planner().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFrame AdminFrame;
    private javax.swing.JFrame DegreeFrame;
    private javax.swing.JRadioButton add;
    private javax.swing.JTextArea adminOutput;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField codeField;
    private javax.swing.JTextField codeField1;
    private javax.swing.JTextField codeField2;
    private javax.swing.JTextField codeField3;
    private javax.swing.JTextField codeField4;
    private javax.swing.JComboBox creditWeightBox;
    private javax.swing.JComboBox degreeBox;
    private javax.swing.JTextField degreeField;
    private javax.swing.JTextArea degreeOutput;
    private javax.swing.JTextField firstNameText;
    private javax.swing.JTextField gradeField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField lastNameText;
    private javax.swing.JButton logInButton;
    private javax.swing.JButton logOutButton;
    private javax.swing.JTextArea outputArea;
    private javax.swing.JRadioButton remove;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox semesterBox;
    private javax.swing.JComboBox semesterOfferedBox;
    private javax.swing.JPanel studentInfo;
    private javax.swing.JTextField studentNumText;
    private javax.swing.JPanel studentOptions;
    private javax.swing.JComboBox subjectBox;
    private javax.swing.JComboBox subjectBox1;
    private javax.swing.JComboBox subjectBox2;
    private javax.swing.JComboBox subjectBox3;
    private javax.swing.JComboBox subjectBox4;
    private javax.swing.JTextField titleField;
    private javax.swing.JRadioButton update;
    private javax.swing.JButton updateButton;
    private javax.swing.JButton updateButton1;
    private javax.swing.JTextField yearField;
    // End of variables declaration//GEN-END:variables
}
