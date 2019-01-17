package univ;

// Seegal Panchal

import java.io.Serializable;
import univ.Degree;
import java.util.ArrayList;

public abstract class HonoursDegree extends Degree implements Serializable {
    protected static final double rqrdNumberOfCredits = 20.00;

    public HonoursDegree() 
    {
        super();
    }
    
    public HonoursDegree(String title)
    {
        super(title);
    }
}