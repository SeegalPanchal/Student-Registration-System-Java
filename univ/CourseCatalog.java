package univ;

// Seegal Panchal
// 10/22/18
// Reads the database and creates a list of courses

import java.util.HashMap;
import java.util.ArrayList;
import java.util.*;
import java.io.*;
import database.*;
import javax.swing.JOptionPane;

public class CourseCatalog implements Serializable
{

    private final HashMap<String, Course> catalog;
    private static final String FILENAME = "catalog.ser";
    private MyConnection mC = new MyConnection();
    private PrepStudentScript initTables;
    private ArrayList<String> courseString = new ArrayList<>();
    
    /**
     * Creates a course catalog
     *
     */
    public CourseCatalog()
    {
        catalog = new HashMap<>();
    }
    
    /**
     * Creates a course catalog with an previously loaded list of courses
     *
     * @param previously loaded list of courses
     */
    public CourseCatalog(HashMap<String, Course> catalog)
    {
        this.catalog = catalog;
    }
    
    /**
     * Reads a CSV file that is then written to a hashmap of courses
     *
     * @param fileName the CSV containing the courses
     */
    public void initializeCatalog()
    {
        ArrayList<String> courses = mC.getAllCourses();
        System.out.println(Arrays.asList(courses.toArray()));
        if (courses != null) {
            for (String temp : courses) {
                String [] line = temp.trim().split(",", -1);
                // temp course variable
                Course c;
                
                // colon delimited prerequisites
                String preReqList[] = null;
                // make a temp preReq list
                ArrayList<Course> preReq = new ArrayList<>();
                
                // if there are prerequisites
                if (line[4].length() > 0) {
                    // split it on the delimiter
                    preReqList = line[4].trim().split(":");
                    // find the course
                    for (int i = 0; i < preReqList.length; i++) {
                        c = findCourse(preReqList[i]);
                        if (c == null) {
                            addCourse(new Course(preReqList[i]));
                            c = findCourse(preReqList[i]);
                        }
                        preReq.add(c);
                    }
                } else { preReq = null; }
                
                if (isValidCourseCode(line[0])) {
                    catalog.put(line[0], new Course(line[0], line[2], Double.parseDouble(line[1]), preReq, line[3].charAt(0)));
                }
            }
            // fix
            for (Course c : catalog.values()) { // check every course
                ArrayList<Course> newPreReq = new ArrayList<>();
                if (c.getPrerequisites() == null) continue;
                for (Course d : c.getPrerequisites()) { // look at its prereqs
                    newPreReq.add(new Course(findCourse(d.getCourseCode())));
                }
                if (newPreReq == null || newPreReq.isEmpty()) {
                    c.setPrerequisites(null);
                } else { c.setPrerequisites(newPreReq); }
            }
        }
    }
    
    /**
     * adds a course to the catalog
     *
     * @param the course to be added
     */
    public void addCourse(Course toAdd)
    {
        // only add if the course object passed is not null
        // and if the course is not already in the catalog
        if (toAdd != null && !catalog.containsKey(toAdd.getCourseCode())) {
            // add a deep copy of the course to the hashmap
            catalog.put(toAdd.getCourseCode(), new Course(toAdd));
        }
        else {
            System.out.println("Course is either null or is already in the catalog.");
        }
    }

    /**
     * removes a course from the catalog
     *
     * @param the course to be removed
     */
    public void removeCourse(Course toRemove)
    {
        // only remove if the course object passed is not null
        // and if the course actually exists in catalog
        if (toRemove != null && catalog.containsKey(toRemove.getCourseCode())) {
            catalog.remove(toRemove.getCourseCode()); // add it to the hashmap
        }
        else {
            System.out.println("Course is either null or is not in the catalog.");
        }
    }

    /**
     * Saved the catalog in a serial file which can then be loaded later
     *
     */
    public void saveCatalog()
    {
        // write the entire hashmap to the database after deleting the previous representation of courses
        mC.deleteAllCourses();
        
        if (catalog.values() != null) {
            for (Course c : catalog.values()) {
                // code credit name semester prereq
                // create an arraylist of the prerequisites
                ArrayList<Course> pre = c.getPrerequisites();
                String preReqs = "";
                if (pre!=null) {
                    for (Course p : pre) {
                        preReqs += p.getCourseCode() + ":";
                        
                    }
                }
                if (preReqs.length()-1 > 0) {
                    preReqs = preReqs.substring(0, preReqs.length()-1);
                }
                //System.out.println(preReqs);
                mC.addCourse(c.getCourseCode(), c.getCourseCredit() + "", c.getCourseTitle(), c.getSemesterOffered() + "", preReqs);
            }
        }
        
    }
    
    /**
     * reads a serialized file containing a previous version of the catalog
     *
     */
    public void readSaveState()
    {
        mC.repopulateCourses();
    }

    /**
     * Finds and returns a course in the catalog
     *
     * @param course code for the course you are looking for
     * @return the course corresponding to the course code, or null
     */
    public Course findCourse(String courseCode)
    {
        if (isValidCourseCode(courseCode) && catalog.containsKey(courseCode)) {
            // return a deep copy of the course found in catalog
            return new Course(catalog.get(courseCode));
        }
        return null;
    }
    
    /**
     * checks if the course code is formatted validly
     *
     * @param courseCode course code to check
     * @return boolean, true = formatted well, false otherwise
     */
    public boolean isValidCourseCode(String courseCode)
    {
        // return if they inputted null
        if (courseCode == null) return false;
        if (courseCode.isEmpty()) return false;
        
        // check if the length is wrong
        if (courseCode.length() < 8 || courseCode.length() > 9) return false;
        
        // check if the asterisk is in the correct position
        if (courseCode.indexOf('*') < 3 || courseCode.indexOf('*') > 4) return false;
        
        // split it on the asterisk
        String code[] = courseCode.split("\\*");
        
        // if there aren't 4 numbers return false
        if (code[1].length() != 4) return false;
        
        // check if it was an integer
        int number = -1;
        try { 
            number = Integer.parseInt(code[1]);
            if (number < 1000 || number > 9999) return false;
        }
        catch (NumberFormatException nfe) { 
            return false;
        }
        
        // if there aren't 3/4 letters return false
        if (code[0].length() < 3 || code[0].length() > 4) return false;
        char checkLetters[] = code[0].toCharArray();
        
        // check if the first 3/4 letters are all letters
        for (char c : checkLetters) {
            if (!Character.isLetter(c)) return false;
        }

        // if all the tests were passed, return true
        return true;
    }

    /**
     * Accesor for the list of courses in the catalog
     *
     * @return The hashmap of courses
     */
    public HashMap<String, Course> getCourseList() {
        return catalog;
    }
    
    public ArrayList<String> getCourseString() {
        return courseString;
    }
    
    /**
     * Formats the course catalog to be outputted
     *
     * @return A formmated string that can be outputted
     */
    @Override
    public String toString() {
        String output = "";
        for (String key : catalog.keySet()) {
            output += "Key: " + key + "\nCourse:\n" + catalog.get(key) + "\n";
        }
        
        return output;
    }

    /**
     * CHecks if given object is equivalent to current catalog
     *
     * @param o the objec to test
     * @return true if the object is a catalog with the same courses, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof CourseCatalog)) {
            return false;
        }
        // the object is a catalog
        else {
            // cast it to a catalog
            CourseCatalog c = (CourseCatalog)o;

            boolean found = false;
            // code to check if they are equal
            for (Course a : catalog.values()) {
                for (Course b: c.getCourseList().values()) {
                    if (a.equals(b)) {
                        found = true;
                    }
                }
                if (found == false) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the list of courses in catalog is empty
     *
     * @return true if its empty, false otherwise
     */
    public boolean isEmpty()
    {
        return catalog.isEmpty();
    }
}