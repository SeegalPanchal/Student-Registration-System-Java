/**
    Seegal Panchal
*/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import univ.*;
import database.*;
import java.util.Arrays;

public class Student implements Serializable
{
    private String first;
    private String last;
    private Integer studentNum;
    Degree degree;
    MyConnection c = new MyConnection();

    // refactor, 
    // student should contain a list of Completed courses

    private ArrayList<Attempt> transcript = new ArrayList<>(); // list of completed courses
    private ArrayList<Attempt> planned = new ArrayList<>(); // list of planned courses

    public Student() {
        this.first = null;
        this.last = null;
        this.studentNum = -1;
        this.transcript = new ArrayList<>();
        this.planned = new ArrayList<>();
        this.degree = null;
    }

    // constructors
    public Student(Integer studentNum) {
        this();
        this.studentNum = studentNum;
    }

    public Student(Integer studentNum, String first) {
        this(studentNum);
        this.first = first;
    }

    public Student(String first, String last, Integer studentNum) {
        this(studentNum, first);
        this.last = last;
    }

    public Student(String first, String last, Integer studentNum, ArrayList<Attempt> transcript) {
        this(first, last, studentNum);
        this.transcript = copyList(transcript);
    }

    public Student(String first, String last, Integer studentNum, ArrayList<Attempt> completed, ArrayList<Attempt> planned) {
        this(first, last, studentNum, completed);
        this.planned = copyList(planned);
    }

    /**
     * Finds the student and populates the transcript and plan
     * @param catalog The catalog of all courses
     */
    public void loadStudent(CourseCatalog catalog) {
        System.out.println("LOADING STUDENT");
        try {
            String sName = "";
            if (this.first != null) {
                sName = this.first;
            }
            sName = sName + ",";
            if (this.last != null) {
                sName = sName + this.last;
            }
            DBStudent temp = c.loadStudent(this.studentNum + "", sName);
            // load the degree
            String deg = temp.getDegree();
            switch (temp.getDegree()) {
                case "BCG":
                    degree = new BCG();
                    break;
                case "CS":
                    degree = new CS();
                    break;
                case "SEng":
                    degree = new SEng();
                    break;
                default:
                    break;
            }
            
            // load the courses
            ArrayList<String> planner = temp.getCourses();
            System.out.println(Arrays.toString(planner.toArray()));
            if (planner != null) {
                for (String course : planner) {
                    Attempt newAttempt;
                    boolean transcriptAttempt = false;
                    boolean plannerAttempt = false;
                    String[] parts = course.split(",", -1); // split it into parts
                    // course code, attempt grade, semester taken
                    Course taken = catalog.findCourse(parts[0]);
                    if (taken == null) { // course does not exist in catalog
                        continue;
                    }
                    
                    // or try to parse it
                    try {
                        Double.parseDouble(parts[1]);
                        transcriptAttempt = true;
                    }
                    catch (NumberFormatException nfe) {
                        if (parts[1].equals("P") || parts[1].equals("F")
                            || parts[1].equals("INC") || parts[1].equals("MNR"))
                        {
                            // this gets added to the transcript
                            transcriptAttempt = true;
                        } else {
                            plannerAttempt = true;
                        }
                    }
                    // add to plan/transcript
                    if (plannerAttempt == true) {
                        newAttempt = new Attempt(null, parts[2], taken);
                        this.planned.add(newAttempt);
                        
                    } else if (transcriptAttempt == true) {
                        newAttempt = new Attempt(parts[1], parts[2], taken);
                        this.transcript.add(newAttempt);
                    }
                }
            }
        }
        catch (Exception studentNotFound) {
            // dont need to do anything
            System.out.println("Student not found.");
            return;
        }
    }
    
    /**
     * Adds the student to database
     * @param catalog The catalog of all courses
     */
    public void saveState(CourseCatalog catalog) {
        System.out.println("SAVING STUDENT.");
        // save the student
        String sName = "";
        if (this.first != null) {
            sName = this.first;
        }
        sName = sName + ",";
        if (this.last != null) {
            sName = sName + this.last;
        }
        String sID = studentNum + "";
        String sDegree = degree.getDegreeTitle();
        ArrayList<String> sCourses = new ArrayList<>();
        // for everything in transcript
        String sAttempt = "";
        for (Attempt t : transcript) {
            sAttempt = t.getCourseAttempted().getCourseCode() + "," + t.getAttemptGrade() + "," + t.getSemesterTaken();
            sCourses.add(sAttempt);
        }
        for (Attempt p : planned) {
            sAttempt = p.getCourseAttempted().getCourseCode() + "," + p.getAttemptGrade() + "," + p.getSemesterTaken();
            sCourses.add(sAttempt);
        }
        System.out.println(Arrays.asList(sCourses.toArray()));
        DBStudent thisStudent = new DBStudent(sID, sName, sDegree, sCourses);
        c.saveStudent(thisStudent);
    }

    /**
     * Add an attempt to the transcript
     * @param completed the attempt to add
     */
    public void addTranscript(Attempt completed) {
        boolean found = false;
        if (completed != null && !(completed.isEmpty())) {
            for (Attempt a : transcript) {
                if (a.equals(completed)) {
                    a.setCourseAttempted(completed.getCourseAttempted());
                    a.setAttemptGrade(completed.getAttemptGrade());
                    a.setSemesterTaken(completed.getSemesterTaken());
                    found = true;
                }
            }
            if (found == false) {
                this.transcript.add(new Attempt(completed));
            }
        }   
    }

    /**
     * Adds an Attempt to the Planned list
     * @param planned the attempt to add
     */
    public void addPlanned(Attempt planned) {
        boolean found = false;
        if (planned != null && !(planned.isEmpty())) {
            for (Attempt a : this.planned) {
                if (a.equals(planned)) {
                    a.setCourseAttempted(planned.getCourseAttempted());
                    a.setAttemptGrade(planned.getAttemptGrade());
                    a.setSemesterTaken(planned.getSemesterTaken());
                    found = true;
                }
            }
            if (found == false) {
                this.planned.add(new Attempt(planned));
            }
        }
    }

    /**
     * Remove an attempt from the transcript
     * @param attempt The attempt to remove
     */
    public void removeTranscript(Attempt attempt) {
        if (attempt != null && !(attempt.isEmpty())) {
            transcript.remove(attempt);
        }
    }

    /**
     * Remove an attempt from the planned list
     * @param attempt The attempt to remove
     */
    public void removePlanned(Attempt attempt) {
        if (attempt != null && !(attempt.isEmpty())) {
            planned.remove(attempt);
        }
    }

    /**
     * Mutator method for the name of the student
     * @param first
     * @throws NullPointerException 
     */
    public void setFirstName(String first) throws NullPointerException {
        if (first != null && !first.isEmpty()) {
            this.first = first;
        } else { throw new NullPointerException(); }
    }

    /**
     * Set the last name of the student
     * @param last the name to set
     * @throws NullPointerException 
     */
    public void setLastName(String last) throws NullPointerException {
        if (last != null && !last.isEmpty()) {
            this.last = last;
        } else { throw new NullPointerException(); }
    }

    /**
     * Sets the student number
     * @param studentNum The number to set
     */
    public void setStudentNumber(Integer studentNum) { 
        if (studentNum >= 0 && studentNum <= 9999999) {
            this.studentNum = studentNum; 
        }
    }
    
    /**
     * Sets the planned list
     * @param plan new list to set
     */
    public void setPlanned(ArrayList<Attempt> plan) {
        this.planned = copyList(plan);
    }
    
    /**
     * Sets the transcript
     * @param transcript the new transcript
     */
    public void setTranscript(ArrayList<Attempt> transcript) {
        this.transcript = copyList(transcript);
    }
    
    /**
     * Sets the degree
     * @param deg new degree to set
     */
    public void setDegree(Degree deg) {
        this.degree = deg;
    }
    
    /**
     * Creates full name of the student
     * @return the full name of the name student
     */
    public String getFullName() {
        String fullName = "";
        if (this.first == null && this.last == null) {
            return null;
        } else if (this.first == null) {
            fullName = this.last;
        } else if (this.last == null) {
            fullName = this.first;
        } else {
            fullName = this.first + " " + this.last;
        }
        return fullName;
    }

    /**
     * Getters for the first name of the student
     * @return the first name of the student
     */
    public String getFirstName() { 
        return this.first; 
    }

    /**
     * Getter for the last name of the student
     * @return the last name of the student
     */
    public String getLastName() { 
        return this.last; 
    }

    /**
     * Getter for the student number
     * @return The student number
     */
    public Integer getStudentNumber() { 
        return this.studentNum; 
    }
    
    /**
     * Accessor for the transcript of the current student
     * @return the transcript of the current student
     */
    public ArrayList<Attempt> getTranscript() {
        return this.transcript;
    }
    
    /**
     * Accessor for the planned list
     * @return The list of planned attempts
     */
    public ArrayList<Attempt> getPlanned() {
        return this.planned;
    }
    
    /**
     * The students current degree
     * @return the title of the degree
     */
    public Degree getDegree() {
        return this.degree;
    }

    /**
     * Copies the attempt list that was passed (helper method to make deep copies)
     * @param copy the list to copy
     * @return a new copy of the list
     */
    private ArrayList<Attempt> copyList(ArrayList<Attempt> copy) {
        ArrayList<Attempt> listCopy = new ArrayList<>();
        if (copy != null && !copy.isEmpty()) {
            for (Attempt a : copy) {
                Attempt temp = new Attempt(a);
                listCopy.add(temp);
            }
        } else listCopy = null;
        return listCopy;
    }

    /**
     * Calculates the string representation of the Student class
     * @return the string output
     */
    @Override
    public String toString() {
        String output = "";
        
        if (this.first != null ) { 
            output += "First Name: " + this.first + "\n"; 
        } else { output += "First Name: not set.\n"; }
        if (this.last != null ) { 
            output += "Last Name: " + this.last + "\n"; 
        } else { output += "Last Name: not set.\n"; }
        if (this.studentNum >= 0 && this.studentNum <= 9999999 ) { 
            output += "Student Number: " + this.studentNum + "\n";
        } else { output += "Student Number: not set.\n"; }

        return output;
    }

    /**
     * Checks if the given object is equal to the current student
     * @param o the given object
     * @return True if they are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Student)) {
            return false;
        }

        Student student = (Student) o;

        if (this.first == null ^ student.first == null) {
            return false;
        } else if (this.first != null && !(this.first.equals(student.first))) { 
            return false;
        }

        if (this.last == null ^ student.last == null) {
            return false;
        } else if (this.last != null && !(this.last.equals(student.last))) { 
            return false;
        }
        
        return Objects.equals(this.studentNum, student.studentNum);
    }
}