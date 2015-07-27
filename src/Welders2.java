import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.constraints.LCF;

public class Welders2 {

	public static void main (String[] args) {
		
		//CReate a solver object that will solve the problem
		Solver solver = new Solver();
		
		//REad the problem in from file in standard format
		WeldersReader reader = new WeldersReader("welders0.txt");
		
		int numberOfWelders = reader.getNumberOfWelders();   //the number of welders in the problem
		int numberOfJobs =  reader.getNumberOfJobs();      //the number of jobs in the problem
	    int maxWelders = reader.getMaxWelders();   //the maximum number of welders at any one time
		int[][] rwj = reader.getWelderJobRequirements(); //for each welder, what jobs are they required for
	    int[] mw = reader.getMinWelders(); //minimum number of welders needed for each job
	    int[] wr = reader.getWelderRate();         //the hourly rate for each welder
	    int costBound = reader.getCostBound();
	    
	    //VAriable
	    
	    //the total cost
	  	IntVar tc = VF.enumerated("TotalCost", 0, costBound, solver);
		//each welder's total work time
		IntVar[] wt = VF.enumeratedArray("WorkTimeOfWelder", numberOfWelders, 0, numberOfJobs, solver);
	    //the number of welders works on each job
	    IntVar[] nw = VF.enumeratedArray("nw", numberOfJobs, 0, numberOfWelders, solver);
	    //the distribution of welders works on each job
	    IntVar[][] nW = new IntVar[numberOfJobs][numberOfWelders];
	    //the distribution of jobs worked by each welder
	    IntVar[][] nj = new IntVar[numberOfWelders][numberOfJobs];
		//an array for the finish time of each job
		IntVar[] jobf = VF.enumeratedArray("jobf", numberOfJobs, 0, numberOfWelders*numberOfJobs, solver);
		//a 2d array for the finish time of each welder on each job
	    IntVar[][] jobwf = new IntVar[numberOfJobs][numberOfWelders];
	    //a 2d array for the job assigned to each welder [welder][job]
	    Task[][] welderj = new Task[numberOfWelders][numberOfJobs];
		//an array for the job variables [job][welder]
		Task[][] jobw = new Task[numberOfJobs][numberOfWelders];
		//change maxWelders to IntVar
		IntVar maxw = VF.fixed(maxWelders, solver);
		Task[] linearjob = new Task[numberOfJobs*numberOfWelders];
		
	    for(int w=0; w<numberOfWelders; w++){
	    	for(int j=0; j<numberOfJobs; j++){
	    		//the task variable indicate the time when the welder "w" work on job "j" 
	    		welderj[w][j] = VF.task(VF.bounded("welder"+w+"job"+j, 0, numberOfJobs, solver),
						VF.enumerated("during", 0, 1,solver), // during 0 means this welder don't work on this job
						VF.bounded("finish"+w+j, 0, numberOfJobs*numberOfWelders, solver));
	    		//the task variable indicate the time when the job "j" was worked by welder "w"
	    		jobw[j][w] = welderj[w][j];
	    		nj[w][j] = welderj[w][j].getDuration();
	    	    nW[j][w] = welderj[w][j].getDuration();
	    	    jobwf[j][w] = welderj[w][j].getEnd();
	    	    linearjob[w*numberOfJobs+j] = welderj[w][j];
	    	}
	    }
	    
		//the transpose of the rwj matrix: for each job, which welders it required for
		int[][] rjw = new int[numberOfJobs][numberOfWelders];	
		for(int w=0; w<numberOfWelders; w++){
			for(int j=0; j<numberOfJobs; j++){
				rjw[j][w] = rwj[w][j];
			}
		}
	    
	    //COnstraint
		
		//each job need minimum number of welders
		for(int j=0; j<numberOfJobs; j++){
			    solver.post(IntConstraintFactory.sum(nW[j], nw[j]));
			    solver.post(IntConstraintFactory.arithm(nw[j],">=",mw[j]));
		}
		
		//each job need particular welders
		for(int j=0; j<numberOfJobs; j++){
			for(int w=0; w<numberOfWelders; w++){
			    solver.post(IntConstraintFactory.arithm(nW[j][w],">=",rjw[j][w]));
		    }
		}
		
		//job completed in order
		for(int j=0; j<numberOfJobs; j++){
			solver.post(IntConstraintFactory.maximum(jobf[j], jobwf[j]));
			for(int before = 0; before < numberOfJobs-1; before++) {
				solver.post(ICF.arithm(jobf[before], "<", jobf[before+1]));
			}
		}
		
		//a welder can not do different works simutaneously
		for(int w=0; w<numberOfWelders; w++){
		   for(int j1=0; j1<numberOfJobs-1; j1++){
			   for(int j2=j1+1; j2<numberOfJobs; j2++){
				   solver.post(LCF.or(ICF.arithm(welderj[w][j1].getEnd(), "<=", welderj[w][j2].getStart()),
        		           ICF.arithm(welderj[w][j2].getEnd(), "<=", welderj[w][j1].getStart())));
		    }
		 }
		}
		
		//max number of welders work simutaneously
		IntVar[] height = new IntVar[numberOfWelders*numberOfJobs];
		for(int w=0; w<numberOfWelders*numberOfJobs; w++){
		   height[w] = linearjob[w].getDuration();
		}	
		solver.post(ICF.cumulative(linearjob, height, maxw));		    

	    
		//the total cost less than the bound
		for (int w = 0; w<numberOfWelders; w++) {
	           solver.post(IntConstraintFactory.sum(nj[w], wt[w]));        	
	        }
	     solver.post(IntConstraintFactory.scalar(wt, wr,tc)); 
		
	    
	    //SEarch
	     
			//solver.set(IntStrategyFactory.minDom_LB(tc));		
			//solver.set(IntStrategyFactory.lexico_LB(tc));
	    
	        //use a pretty print object to display the results
	  		Chatterbox.showSolutions(solver);
	  		
	  		//find a solution
	  		solver.findSolution();
	  		//find the optimal solution to minimize the total cost
	  		//solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, tc);
	  				
	  		//print out the search		
	        Chatterbox.printStatistics(solver);		
		
	}
}