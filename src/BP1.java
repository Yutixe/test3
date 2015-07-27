import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

/*
 * A class for solving a basic 1D bin packing decision problem
 * Data values are hard coded into the class
 */
public class BP1 {

	public static void main(String[] args) {

		/*
		//the sizes of the individual objects
		int[] sizes = {4,7,2,9,5,8,4};
		int nObjects = sizes.length;
		int nBins = 3;
		int binSize = 15; 
        */
		
		//the sizes of the individual objects
		int[] sizes = {42, 63, 67, 57, 93, 90, 38, 36, 45, 42};
		int nObjects = sizes.length;  //the number of objects
		int nBins = 5;                //the number of bins
		int binSize = 150;            //the (uniform) bin capacity
		
		//create the solver
		Solver solver = new Solver();
		
		//VARIABLES
		
		//an array of intVars, for the total size added to each bin
		IntVar[] binLoad = VariableFactory.enumeratedArray("loads", nBins, 0, binSize, solver);

		//a 2D array of 0/1, so that binPacking[i][j]==1 means that object j was placed in bin i
        IntVar[][] binPacking = VariableFactory.enumeratedMatrix("solution", nBins, nObjects, 0 , 1, solver);
        
        //the transpose of the above matrix, so that we can work with an array for each object
		IntVar[][] binPackingT = new IntVar[nObjects][nBins];
        for (int bin = 0; bin < nBins; bin++) {
        	for (int object = 0; object < nObjects; object++) {
        		binPackingT[object][bin] = binPacking[bin][object];
        	}
        }
        
        //CONSTRAINTS
        
        //for each object in a bin, add the sizes to the get the bin load
        for (int bin = 0; bin<nBins; bin++) {
            solver.post(IntConstraintFactory.scalar(binPacking[bin], sizes, binLoad[bin]));        	
        }
        
        //for each object, make sure it is in exactly 1 bin
        for (int object = 0; object < nObjects; object++) {
        	solver.post(IntConstraintFactory.sum(binPackingT[object], VariableFactory.fixed(1, solver)));
        }
        
        //SEARCH
        
        Chatterbox.showSolutions(solver);
        solver.findSolution();
        Chatterbox.printStatistics(solver);
        
        //print out our own solution
        //Bin i: obj j (size of j)* [bin load]
        //*
        for (int bin = 0; bin < nBins; bin++) {
        	System.out.print("Bin " + bin + ": ");
        	for (int object = 0; object < nObjects; object++) {
        		if (binPacking[bin][object].getValue() == 1) {     //if object is in bin
        			System.out.print(object + "(" + sizes[object] + ") ");   //print the details
        		}
        	}
        	System.out.println("[" + binLoad[bin].getValue() + "]");    //print the bin load
        }        
	}

}

