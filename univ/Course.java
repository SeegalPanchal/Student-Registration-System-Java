package univ;


/**
    Seegal Panchal
*/

import java.io.Serializable;
import java.util.ArrayList;

public class Course implements Serializable
{
    // refactors course will not contain course informations
    // will contain Semester Offered as F,W,B
    private String courseCode;
    private String courseTitle;
    private char semesterOffered;
    private double credit;
    private ArrayList<Course> preReqList;

    /**
     * Created a course with default null values
     *
     */
    public Course()
    {
        courseCode = null;
        courseTitle = null;
        credit = -1;
        preReqList = new ArrayList<>();
        semesterOffered = '\0';
    }
    
    /**
     * Creates a course given course code only
     *
     * @param the course code
     */
    public Course(String courseCode)
    {
        this();
        this.courseCode = courseCode;
    }
    
    /**
     * Creates a course with the code, name, weighting and prerequisites
     *
     * @param courseCode The code of the course
     * @param courseName The name of the course
     * @param creditWeight The weighting of the course
     * @param prerequisites The prerequisite courses that must be taken
     */
    public Course(String courseCode, 
                String courseName,
                double creditWeight, 
                ArrayList<Course> prerequisites)
    {
        this(courseCode);
        this.credit = creditWeight;
        this.courseTitle = courseName; 
        this.preReqList = copyPrerequisites(prerequisites);
    }

    /**
     * Creates a course with the code, name, weighting and prerequisites
     *
     * @param courseCode The code of the course
     * @param courseTitle
     * @param courseName The name of the course
     * @param creditWeight The weighting of the course
     * @param prerequisites The prerequisite courses that must be taken
     */
    public Course(String courseCode, 
                String courseTitle,
                double creditWeight, 
                ArrayList<Course> prerequisites,
                char semesterOffered)
    {
        this(courseCode, 
                courseTitle,
                creditWeight,
                prerequisites);
        this.semesterOffered = semesterOffered;
    }
    
    /**
     * Creates a course that is an exact copy of the given course
     *
     * @param copy The course to make a copy of
     */
    public Course(Course copy)
    {
        // initialized the object with the previous objects values
        this(copy.getCourseCode(), 
                copy.getCourseTitle(),
                copy.getCourseCredit(),
                copy.getPrerequisites(),
                copy.getSemesterOffered());
    }

    /**
     * Sets the course code of course
     * @param courseCode the course code to set
     */
    protected void setCourseCode(String courseCode) {
        if (courseCode != null && !courseCode.isEmpty()) {
            this.courseCode = courseCode;
        }
    }
    
    /**
     * Sets the course title
     * @param courseTitle the new title
     */
    protected void setCourseTitle(String courseTitle) {
        if (courseTitle != null && !courseTitle.isEmpty()) {
            this.courseTitle = courseTitle;
        }
    }
    
    /**
     * Sets the credit worth of the current course
     * @param credit the new credit weight
     */
    protected void setCourseCredit(double credit) {
        if (credit >= 0 && credit <= 1.0) {
            this.credit = credit;
        }
    }
    
    /**
     * Sets the list of prerequisite courses for the course
     * @param prerequisites the new list of preReqs
     */
    protected void setPrerequisites(ArrayList<Course> prerequisites) {
        this.preReqList = copyPrerequisites(prerequisites);
    }

    /**
     * Sets the semester the course is offered
     * @param semester the new semester
     */
    protected void setSemesterOffered(char semester) {
        if (semester == 'F' || semester == 'W' || semester == 'B') {
            this.semesterOffered = semester;
        }
    }
    
    // getters for the values of the course
    public String getCourseCode() { 
        return this.courseCode; 
    }
    
    public String getCourseTitle() { 
        return this.courseTitle; 
    }

    public double getCourseCredit() { 
        return this.credit; 
    }

    public ArrayList<Course> getPrerequisites() { 
        return this.preReqList; 
    }

    public char getSemesterOffered() { 
        return this.semesterOffered; 
    }
        
    /**
     * Helper function to create a deepcopy of an ArrayList
     * @param copy the list to copy
     * @return the new list
     */
    private ArrayList<Course> copyPrerequisites(ArrayList<Course> copy) {
        ArrayList<Course> prerequisitesCopy = new ArrayList<>();
        if (copy != null && !copy.isEmpty()) {
            for (Course c : copy) {
                Course temp = new Course(c);
                prerequisitesCopy.add(temp);
            }
        } else prerequisitesCopy = null;
        return prerequisitesCopy;
    }

    /**
     * Checks if given object is the same as current course
     *
     * @param o object to check against current course
     * @return boolean, true if the object is the same as current course, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Course)) {
            return false;
        }

        Course course = (Course) o;
        if (this.courseCode == null ^ course.courseCode == null) {
            return false;
        } else if (this.courseCode != null && !(this.courseCode.equals(course.courseCode))) {
            return false;
        }

        if (this.courseTitle == null ^ course.courseTitle == null) {
            return false;
        } else if (this.courseTitle != null && !(this.courseTitle.equals(course.courseTitle))) {
            return false;
        }

        // if either are null
        if (this.preReqList == null || course.preReqList == null) {
            return this.preReqList == course.preReqList;
        }
        else { // if neither are null
            for (Course c : this.preReqList) {
                boolean same = false;
                for (Course d : course.preReqList) {
                    if (c.equals(d)) { same = true; }
                }
                if (same == false) { return false; }
            }
        }
        return true;
    }

    /**
     * Formats all the variables for print statement
     *
     * @return formatted String containing the member variables that can be printed
     */
    @Override
    public String toString() {
        // return the course code and its name
        String output = "";
        
        if (this.courseCode != null) {
            output += "Code: " + this.courseCode + "\n";
        }

        if (this.courseTitle != null) {
            output += "Title: " + this.courseTitle + "\n";
        }

        if (this.credit > 0) {
            output += "Credit: " + this.credit + "\n";
        }

        if (this.semesterOffered == 'B' || this.semesterOffered == 'W' || this.semesterOffered == 'F') {
            output += "Semester Offered: " + this.semesterOffered + "\n";
        }

        if (preReqList != null && !preReqList.isEmpty()) {
            output += "Prerequisites: ";
            for (Course c : preReqList){
                output += c.getCourseCode() + ", ";
            }
        } else output += "Prerequisites: none.";
        // remove trailing commas
        output = output.replaceAll(", $", "");
        
        return output + "\n";
    }
}