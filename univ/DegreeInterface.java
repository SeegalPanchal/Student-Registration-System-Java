/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package univ;

import java.util.ArrayList;

/**
 *
 * @author seega
 */
public interface DegreeInterface {
    public abstract boolean meetsRequirements(ArrayList<Course> allTheCoursesPlannedAndTaken);
    public abstract double numberOfCreditsRemaining(ArrayList<Course> allTheCoursesPlannedAndTaken);
    public abstract ArrayList<Course> remainingRequiredCourses(ArrayList<Course> allTheCoursesPlannedAndTaken);
}
