import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;


/*
 * A simple solver for a job shop scheduling problem
 */
public class SimpleJSS {

	public static void main(String[] args) {
		
		//read the problem in from file in standard format
		JSSPReader reader = new JSSPReader("jssp1.txt");
		
		int numJobs = reader.getNumJobs();
		int numRes = reader.getNumResources();
		int[][][] problem = reader.getProblem();
		int sumDurations = reader.getSumDurations();
		
		//a maximum value for the makespan
		//int deadline = 20;
		int deadline = sumDurations;
		
		//a 2d array for the Task variables [job][task]
		Task[][] jobtask = new Task[numJobs][numRes];
		//a 2d array for the tasks assigned to each machine [res][job]
		Task[][] restask = new Task[numRes][numJobs];
		//an array for recording the next index for each restask array
		int resIndices[] = new int[numRes];
		for (int i =0; i<numRes; i++) {
			resIndices[i] = 0;
		}
		
		//SOLVER
		Solver solver = new Solver();
		
		//VARIABLES
		

		IntVar[][] heights = new IntVar[numRes][numJobs];
		IntVar[] capacity = new IntVar[numRes];
		//IntVar[] jobEnd = new IntVar[numJobs];
		IntVar[] allTasks = new IntVar[numJobs*numRes];
		//for each job, for each task, create a Task variable, and add it to the jobtask and restask arrays
		int resource = 0;
		int tasks = 0;
		for (int j = 0; j<numJobs; j++) {
			System.out.println("Job " + j);
			for (int t = 0; t < numRes; t++) {
				System.out.print("task " + t + " ");
				resource = problem[j][t][0];
				System.out.println("is on resource " + resource +"; duration " + problem[j][t][1]);
				jobtask[j][t] = VF.task(VF.bounded("j" + j + "t" + t + "s", 0, sumDurations, solver),
						VF.fixed("j" + j + "t" + t + "d", problem[j][t][1],solver),
						VF.bounded("j" + j + "t" + t + "e", 0, sumDurations, solver));
				restask[resource][resIndices[resource]] = jobtask[j][t];
				heights[resource][resIndices[resource]] = VF.fixed(1, solver);
                capacity[resource] = VF.fixed(1, solver);
				resIndices[resource]++;
				allTasks[tasks++] = jobtask[j][t].getStart();
			}
			//jobEnd[j] = jobtask[j][numRes-1].getEnd();
		}
		
		IntVar makespan = VF.bounded("makespan",  0,  sumDurations, solver);

		//CONSTRAINTS
		
		//for each resource, create a cumulative constraint, for the relevant tasks, all with height 1, for capacity 1
		/*
		for (int res = 0; res < numRes; res++) {
			solver.post(ICF.cumulative(restask[res], heights[res], capacity[res]));
		}
		*/
		
		/*
		*/ 
		//for each resource, create "no overlap" constraints between the tasks
		for (int res = 0; res < numRes; res++) {
			for (int task1 = 0; task1 < numJobs-1; task1++) {
				for (int task2 = task1+1; task2 < numJobs; task2++) {
			        solver.post(LCF.or(ICF.arithm(restask[res][task1].getEnd(), "<=", restask[res][task2].getStart()),
			        		           ICF.arithm(restask[res][task2].getEnd(), "<=", restask[res][task1].getStart())));	
				}
			}
		}
		/*
		*/
		
		//for each job, create precedence constraints between each task
		for (int j = 0; j < numJobs; j++) {
			for (int before = 0; before < numRes-1; before++) {
					solver.post(ICF.arithm(jobtask[j][before].getEnd(), 
							    "<=", 
							    jobtask[j][before+1].getStart()));
			}
			solver.post(ICF.arithm(jobtask[j][numRes-1].getEnd(), "<=", makespan));
		}
		
		solver.post(ICF.arithm(makespan, "<=", deadline));
				
		//Search Strategy
        solver.set(IntStrategyFactory.minDom_LB(allTasks));
       // solver.set(IntStrategyFactory.domOverWDeg(allTasks, 0));
        //solver.set(IntStrategyFactory.impact(allTasks, 0));

		//SOLVE
		
        Chatterbox.showSolutions(solver);
		//solver.findSolution();
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, makespan);
		
        Chatterbox.printStatistics(solver);		
	}
 
}

