package univ;

import java.io.Serializable;
import java.util.*;
import java.util.Set;

public class BCG extends GeneralDegree implements Serializable {

    // A set of science values
    private static final Set<String> science = new HashSet<String>(Arrays.asList(new String[] 
    { "BIOL","MATH", "ECON", "BIOM", "ECOL", "FOOD", "PHYS", "CHEM", "ZOO", "STAT" } ));

    public BCG() {
        super("BCG");
        super.readCourseCodeList("bcgcourses.ser");
    }

    @Override
    public boolean meetsRequirements(ArrayList<Course> allTheCoursesPlannedAndTaken) {      
        double totalCredits = 0.0, 
        credits1000 = 0.0, 
        credits3000 = 0.0, 
        creditsCisStat2000 = 0.0, 
        creditsCis3000 = 0.0, 
        creditsScience = 0.0, 
        creditsArt = 0.0;

        // first check if required courses are completed
        ArrayList<Course> r = remainingRequiredCourses(allTheCoursesPlannedAndTaken);
        if (r != null && r.size() > 0) { return false; }

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
            // cis or stat course at the 2000 level or higher
            if (lvl >= 2000 && (dept.equals("CIS") || dept.equals("STAT"))) {
                creditsCisStat2000 += c.getCourseCredit();
            }
            // 4 credits at 3000 level or higher
            if (lvl >= 3000) { 
                credits3000 += c.getCourseCredit();
                if (dept.equals("CIS")) {
                    creditsCis3000 += c.getCourseCredit();
                }
            }
            // 2 science and 2 arts credits
            if (science.contains(dept)) {
                creditsScience += c.getCourseCredit();
            }
            else {
                creditsArt += c.getCourseCredit();
            }
        }

        totalCredits += (credits1000 > 6) ? 6 : credits1000;
        if (totalCredits < 15) { return false; }
        if (credits3000 < 4) { return false; }
        if (creditsCisStat2000 < 0.5) { return false; }
        if (creditsCis3000 < 1) { return false; }
        if (creditsScience < 2) { return false; }
        if (creditsArt < 2) { return false; }
        return true;
    }

    public double numberOfCreditsRemaining(ArrayList<Course> allTheCoursesPlannedAndTaken) {
        double remainingCredits = 15.00;
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

        BCG bcg = (BCG) o;
        if (!(this.getDegreeTitle().equals(bcg.getDegreeTitle()))) {
            return false;
        }
        // I have to change this so it checks every course
        return this.getRequiredCourses().equals(bcg.getRequiredCourses());
    }
}
