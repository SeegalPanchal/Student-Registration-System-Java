package univ;

// Seegal Panchal

import java.io.Serializable;


public abstract class GeneralDegree extends Degree implements Serializable {
    protected static final double rqrdNumberOfCredits = 15.00;

    public GeneralDegree() 
    {
        super();
    }
    
    public GeneralDegree(String title)
    {
        super(title);
    }
}