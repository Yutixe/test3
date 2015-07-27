import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;

/*
 * simple multi-capacity scheduling problem, using the cumulative constraint
 */
public class MultiCap1 {

	public static void main(String[] args) {

		/*------PROBLEM SETUP--------*/
		
		//as input, the duration of each task
		int[] lengths = {3,3,2,2,4,1,3,1,1,1,1};
		
		//the resource consumption for each task
		int[] consumption = {1,1,2,3,1,5,2,6,4,1,2};
		
		//get the number of tasks (and quit if the input arrays were not of same length)
		int numberOfTasks = lengths.length;
		if (numberOfTasks != consumption.length) {
			System.exit(1);
		}
		
		//the capacity of the resource
		int capacity = 8;
		
		//the maximum number of time units in which everything must be scheduled
		int deadline = 6;
		
		/*------SOLVER---------------*/
		
		Solver solver = new Solver();
		
		/*------VARIABLES------------*/
		
		//an array of start time variables, one for each task
		IntVar[] start = VF.boundedArray("start", numberOfTasks, 0, deadline-1, solver);

		//an array of end time variables, one for each task
		IntVar[] end = VF.boundedArray("end", numberOfTasks, 1, deadline, solver); //a task will be active from start up to end-1
		
		//an array of Task objects for the cumulative constraint.
		//Each task has a start, a duration and an end, all of them IntVars
		//the durations are fixed as input, so use VF.fixed
		Task[] tasks = new Task[numberOfTasks];
		for (int task = 0; task < numberOfTasks; task++) {
		    tasks[task] = new Task(start[task], VF.fixed(lengths[task], solver), end[task]);   	
		}

		//an array of heights for cumulative (i.e. the consumption of each task)
		IntVar[] height = new IntVar[numberOfTasks];

		/*------CONSTRAINTS----------*/

		//the end time for each task is the start time plus the duration
		//Using arithm, we can write this as end-start = (fixed) length
		for (int task = 0; task < numberOfTasks; task++) {
			solver.post(ICF.arithm(end[task], "-", start[task], "=", lengths[task]));
		}
		
		//create a (fixed) IntVar for each height for the cumulative constraint
		for (int task=0; task < numberOfTasks; task++) {
			height[task] = VF.fixed(consumption[task],  solver);
		}
		
		//post the cumulative constraint saying all must be slotted into the schedule, respecting the capacity
		solver.post(ICF.cumulative(tasks, height, VF.fixed(capacity, solver)));
		
		//post some other constraints to test the model
		/*
		solver.post(ICF.arithm(end[0],  "<=", start[1]));
		//solver.post(ICF.arithm(end[6], "<=", start[10]));
		solver.post(LCF.or(ICF.arithm(end[6],  "<=", start[2]),
			               ICF.arithm(end[2], "<=", start[6])));
		solver.post(ICF.arithm(end[3], "<=", start[6]));
		*/
		/*------SEARCH STRATEGY-------*/
		
		/*------SOLUTION-------------*/
		
		Chatterbox.showSolutions(solver);
		solver.findSolution();
		Chatterbox.printStatistics(solver);
		
		//if a solution is found, display it
		if (solver.isSatisfied() == ESat.TRUE) {
			System.out.print("  ");
			for (int t = 0; t < deadline; t++) {
				System.out.print(t + " ");
			}
			System.out.println();
			for (int task = 0; task < numberOfTasks; task++) {
				System.out.print(task + ": ");
				int startTime = start[task].getValue();
				for (int t = 0; t < startTime; t++) {
					System.out.print("0 ");
				}
				for (int t=0; t<lengths[task]; t++ ) {
					System.out.print(consumption[task] + " ");
				}
				for (int t = end[task].getValue(); t<deadline; t++) {
					System.out.print("0 ");
				}
				System.out.println();
			}
		}
	}

}

