package univ;

import java.io.Serializable;
import java.util.*;

public class CS extends HonoursDegree implements Serializable {

    public CS() {
        super("CS");
        super.readCourseCodeList("cscourses.ser");
    }

    @Override
    public boolean meetsRequirements(ArrayList<Course> allTheCoursesPlannedAndTaken) {      
        double totalCredits = 0.0,
        cisCredits = 0.0,
        aOACredits = 0.0,
        electives = 0.0,
        credits1000 = 0.0,
        cis3000 = 0.0,
        cis4000 = 0.0,
        aOA3000 = 0.0;

        // first check if required courses are completed
        ArrayList<Course> r = remainingRequiredCourses(allTheCoursesPlannedAndTaken);
        if (r != null && r.size() > 0) { return false; }
        r = super.getRequiredCourses();

        HashMap<String, Double> credits = new HashMap<>();
        for (Course c : allTheCoursesPlannedAndTaken) {

            String dept = c.getCourseCode().split("\\*")[0];
            int lvl = Integer.parseInt(c.getCourseCode().split("\\*")[1]);

            // count the total number of credits
            if (credits.containsKey(dept) && credits.get(dept) < 11) { 
                credits.put(dept, credits.get(dept) + c.getCourseCredit());
                if (lvl >= 2000) { totalCredits += c.getCourseCredit(); }
            } else if (!credits.containsKey(dept)) { 
                credits.put(dept, c.getCourseCredit());
                if (lvl >= 2000) { totalCredits += c.getCourseCredit(); }
            }
            // check if its a 1000 level credit
            if (lvl < 2000) { 
                credits1000 += c.getCourseCredit(); 
            }
            if (dept.equals("CIS")) {
                cisCredits += c.getCourseCredit();
                if (lvl >= 3000) {
                    cis3000 += c.getCourseCredit();
                    if (lvl >= 4000) {
                        cis4000 += c.getCourseCredit();
                    }
                }
                // if its not a required course, its an elective
                boolean found = false;
                for (Course d : r) {
                    if (c.equals(d)) { found = true; }
                }
                // its an elective
                if (found == false) { electives += c.getCourseCredit(); }
            } else { 
                aOACredits += c.getCourseCredit();
                if (lvl >= 3000) {
                    aOA3000 += c.getCourseCredit();
                }
            }
        }

        totalCredits += (credits1000 > 6) ? 6 : credits1000;
        if (totalCredits < 20.00) { 
            return false; }
        if (cisCredits < 11.25) { 
            return false; }
        if (aOACredits < 4.00) { 
            return false; }
        if (electives < 4.75) { 
            return false; }
        if (aOA3000 < 1.00) { 
            return false; }
        if (cis3000 < 6.00) { 
            return false; }
        if (cis4000 < 2.00) { 
            return false; }
        return true;
    }

    public double numberOfCreditsRemaining(ArrayList<Course> allTheCoursesPlannedAndTaken) {
        double remainingCredits = 20.00;
        for (Course c : allTheCoursesPlannedAndTaken) {
            remainingCredits -= c.getCourseCredit();
        }
        return (remainingCredits > 0) ? remainingCredits : 0;
    }

    @Override
    public ArrayList<Course> remainingRequiredCourses(ArrayList<Course> allTheCoursesPlannedAndTaken) {
        
        ArrayList<Course> required = super.getRequiredCourses(); // the required course list
        ArrayList<Course> notTaken = new ArrayList<>();
        
        boolean found = false;
        // check if our required courses coincide with planned/taken courses
        for (Course r: required) {
            found = false;
            for (Course c: allTheCoursesPlannedAndTaken) {
                if (r.equals(c)) { // if the course was found
                    found = true;
                    break;
                }
            }
            if (found == false) { // if the course was found
                notTaken.add(new Course(r));
            }
        }
        
        return notTaken;
    }

    @Override
    public String toString() {
        String toString = "";

        if (this.getDegreeTitle() != null && !(this.getDegreeTitle().isEmpty())) {
            toString += this.getDegreeTitle() + "\n";
        }

        return toString.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Degree)) {
            return false;
        }

        CS cs = (CS) o;
        if (!(this.getDegreeTitle().equals(cs.getDegreeTitle()))) {
            return false;
        }
        // I have to change this so it checks every course
        return this.getRequiredCourses().equals(cs.getRequiredCourses());
    }
}
