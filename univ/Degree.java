package univ;

// Seegal Panchal

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author seega
 */
public abstract class Degree implements Serializable, DegreeInterface {
    private String title;
    private ArrayList<Course> listOfRequiredCourseCodes;

    public Degree() {
        this.title = null;
        this.listOfRequiredCourseCodes = new ArrayList<>();
    }

    public Degree(String title) {
        this();
        this.title = title;
    }

    public Degree(String title, ArrayList<Course> listOfRequiredCourseCodes) {
        this(title);
    }

    protected void setDegreeTitle(String title) {
        if (title != null && !(title.isEmpty())) {
            this.title = title;
        }
    }

    protected void setRequiredCourses(ArrayList<Course> listOfRequiredCourseCodes) {
        if (listOfRequiredCourseCodes != null && !(listOfRequiredCourseCodes.isEmpty())) {
            this.listOfRequiredCourseCodes = listOfRequiredCourseCodes;
        }
    }

    public String getDegreeTitle() {
        return title;
    }

    public ArrayList<Course> getRequiredCourses() {
        return listOfRequiredCourseCodes;
    }
    
    public void readCourseCodeList(String fileName)
    {
        try
        {
            // declare and instantiate the readers
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fin);
            
            // read an object from the file
            setRequiredCourses((ArrayList<Course>)in.readObject());
            
            // close the readers
            in.close();
            fin.close();
        }
        // input output exception, such as File Not Found exception
        catch(IOException e) 
        {
            System.out.println("Here the problem lies.");
            // print the thrown error
            System.out.println("Error. IOException: " + e);
        }
        // object being read is not the one being read exception
        catch(ClassNotFoundException c)
        {
            // print the thrown error
            System.out.println("Error. ClassNotFoundException: " + c);
        }
    }

    public abstract boolean meetsRequirements(ArrayList<Course> allTheCoursesPlannedAndTaken);
    public abstract double numberOfCreditsRemaining(ArrayList<Course> allTheCoursesPlannedAndTaken);
    public abstract ArrayList<Course> remainingRequiredCourses(ArrayList<Course> allTheCoursesPlannedAndTaken);
    
    @Override
    public abstract String toString();
    @Override
    public abstract boolean equals(Object o);
}