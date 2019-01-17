
import java.io.Serializable;
import univ.Course;

// Seegal Panchal

public class Attempt implements Serializable
{
    
    private String attemptGrade;
    private String semesterTaken;
    private Course courseAttempted;

    /**
     * No parameter Constructor for the Attempt class
     */
    public Attempt() {
            this.attemptGrade = null;
            this.semesterTaken = null;
            this.courseAttempted = null;
    }

    /**
     * Sets the course attempted
     * @param attempted The course you took
     */
    public Attempt(Course attempted) {
            this();
            this.courseAttempted = attempted;
    }

    /**
     * Constructor with semester taken and course attempted
     * @param semesterTaken The semester the course is planned/taken
     * @param attempted  The course that was attempted
     */
    public Attempt(String semesterTaken, 
        Course attempted) {
            this(attempted);
            this.semesterTaken = semesterTaken;
    }

    /**
     * Full constructor settings all the values
     * @param attemptGrade The grade of the attempted course
     * @param semesterTaken The semester the course was taken
     * @param attempted The attempted course
     */
    public Attempt(String attemptGrade, 
        String semesterTaken, 
        Course attempted) {
            this(semesterTaken, attempted);
            this.attemptGrade = attemptGrade;
    }

    /**
     * Deep copy constructor of the Attempt Class
     * @param copy The attempt you want to make a copy of
     */
    public Attempt(Attempt copy) {
        this(copy.attemptGrade, copy.semesterTaken, copy.courseAttempted);
    }
    
    /**
     * Mutator for the attempt grade variable
     * @param attemptGrade the new grade value
     */
    public void setAttemptGrade(String attemptGrade) {
        if (attemptGrade != null && !attemptGrade.isEmpty()) {
            this.attemptGrade = attemptGrade;
        }
    }

    /**
     * The semester the course was taken mutator
     * @param semester THe semester the course was taken
     */
    public void setSemesterTaken(String semester) {
        if (semester != null && !semester.isEmpty()) {
            this.semesterTaken = semester;
        }
    }

    /**
     * Sets the course that was attempted
     * @param attempted The new course to set
     */
    public void setCourseAttempted(Course attempted) {
        if (attempted != null) {
            this.courseAttempted = new Course(attempted);
        }
    }

    /**
     * Returns the grade of the attempted course
     * @return The grade of the attempted course
     */
    public String getAttemptGrade() {
        return this.attemptGrade;
    }

    /**
     * Accessor for the semester taken variable
     * @return The semester the attempt was done
     */
    public String getSemesterTaken() { 
        return this.semesterTaken; 
    }

    /**
     * Accessor for the course attempted variable
     * @return A course value for which course was taken (should never be null)
     */
    public Course getCourseAttempted() {
        return this.courseAttempted;
    }

    /**
     * 
     * @return The string output of the course
     */
    @Override
    public String toString() {
        String output = "";

        if (courseAttempted != null) { 
            output += courseAttempted; 
        } else { output += "Course not set.\n"; }

        if (attemptGrade != null) { 
            output += "Attempt Grade: " + attemptGrade + "\n"; 
        } else { output += "No grade set.\n"; }

        if (semesterTaken != null) {
            output += "Semester Taken: " + semesterTaken + "\n";
        } else { output += "Semester not set.\n"; }

        return output + "\n";
    }

    /**
     * Checks if the given object is equal to the current Attempt
     * @param o The object given
     * @return True if its equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {

        if (o == this) { return true; }
        if (!(o instanceof Attempt)) { return false; }

        Attempt attempt = (Attempt) o;

        // if one is null and the other is not
        if (this.courseAttempted == null ^ attempt.courseAttempted == null) {
            return false;
        } 
        // else if the courses are not the same
        else if (this.courseAttempted != null 
        && !(this.courseAttempted.equals(attempt.courseAttempted))) {
            return false;
        }

        if (this.attemptGrade == null ^ attempt.attemptGrade == null) {
            return false;
        }
        else if (this.attemptGrade != null 
        && !(this.attemptGrade.equals(attempt.attemptGrade))) {
            return false;
        }

        if (this.semesterTaken == null ^ attempt.semesterTaken == null) {
            return false;
        }
        else if (this.semesterTaken != null 
        && !(this.semesterTaken.equals(attempt.semesterTaken))) {
            return false;
        }

        return true;
    }
    
    /**
     * checks if the course attempted is empty
     * @return 
     */
    public boolean isEmpty() {
        if (courseAttempted == null) { 
            return true; 
        }
        return false; // return false if all values have something
    }
}