import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

public class Welders1 {

	public static void main (String[] args) {
		
		//CReate a solver object that will solve the problem
		Solver solver = new Solver();
		
		//REad the problem in from file in standard format
		WeldersReader reader = new WeldersReader("welders0.txt");
		
		int numberOfWelders = reader.getNumberOfWelders();   //the number of welders in the problem
		int numberOfJobs =  reader.getNumberOfJobs();      //the number of jobs in the problem
	    //int maxWelders = reader.getMaxWelders();   //the maximum number of welders at any one time
		int[][] rwj = reader.getWelderJobRequirements(); //for each welder, what jobs are they required for
	    int[] mw = reader.getMinWelders(); //minimum number of welders needed for each job
	    int[] wr = reader.getWelderRate();         //the hourly rate for each welder
	    int costBound = reader.getCostBound();
	    

		//VAriable
		
	    //the total cost
		IntVar tc = VF.enumerated("TotalCost", 0, costBound, solver);
		//the cost paid for each job
		IntVar[] jc = VF.enumeratedArray("CostOfJob", numberOfJobs, 0, costBound, solver);
		
	    //a 2d array for the jobs allocated to each welder [wel][job]
		IntVar[][] awj = VF.enumeratedMatrix("Allocation", numberOfWelders, numberOfJobs, 0 , 1, solver); 
		
		//the transpose of the above matrix
		IntVar[][] awjT = new IntVar[numberOfJobs][numberOfWelders];	
		for(int w=0; w<numberOfWelders; w++){
			for(int j=0; j<numberOfJobs; j++){
				awjT[j][w] = awj[w][j];
			}
		}
		
		//the number of welders for each job
		IntVar[] jobW = VF.enumeratedArray("WeldersInJob", numberOfJobs, 0, numberOfWelders, solver);		
		
		//COnstraint
		
		//the total cost never over the bound
		 for (int j = 0; j<numberOfJobs; j++) {
	           solver.post(IntConstraintFactory.scalar(awjT[j], wr, jc[j]));        	
	        }
	     solver.post(IntConstraintFactory.sum(jc,tc));        	
	        
		 
		 //each job has more than the minimum number of welders
	     for(int j=0;j<numberOfJobs;j++){
		 solver.post((IntConstraintFactory.sum(awjT[j],jobW[j])));
	     }
	     for (int j = 0; j<numberOfJobs; j++) {
	            solver.post(IntConstraintFactory.arithm(jobW[j], ">=", mw[j]));        	
	        }
		
		 //each job require particular welders
		 for(int w=0; w<numberOfWelders; w++)
			{
				for(int j=0; j<numberOfJobs; j++)
				{
					solver.post(IntConstraintFactory.arithm(awj[w][j], ">=", rwj[w][j]));
				}
			}
		 
		 
		 //SEarch
		 
		//Specify a search strategy (or take the default)
		//solver.set(IntStrategyFactory.minDom_LB(jc));		
	    solver.set(IntStrategyFactory.domOverWDeg(jc, 1));
		//solver.set(IntStrategyFactory.lexico_LB(jc));
		//solver.set(IntStrategyFactory.lastConflict(solver));
		
		
		//use a pretty print object to display the results
		Chatterbox.showSolutions(solver);
		
		//find a solution
		solver.findSolution();
		//find the optimal solution to minimize the total cost
		//solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, tc);
		System.out.println(tc);
				
		//print out the search		
        Chatterbox.printStatistics(solver);		
	}
}