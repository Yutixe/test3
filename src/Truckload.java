import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

/*
 *  Simple truck loading problem (or bounded knapsack)
 *  Given an inventory of items, where each item has a weight and a profit,
 *  find a selection of the items to load into a truck, where there is a limit
 *  on the total weight carried by the truck, and
 *  (i) where at least minimum profit is achieved, or
 *  (ii) where the maximum possible profit is achieved
 */

public class Truckload {

	public static void main(String[] args) {
		Solver solver = new Solver();
		
		/* Problem 1: 
		int[] weights = {9, 4}; //the weights of a group of item types 
		int[] profits = {3, 5}; //the profit obtained from each type - must be same length as weights
		int[] stock =   {5, 3}; //the maximum number of each item 
		int capacity = 50;      //maximum load weight in the truck
        int cost = 24;         //the profit threshold to be achieved (e.g. the cost of transport)
		*/

		/* Problem 2: */
        
		int[] weights = {15, 12, 17, 20, 39, 24, 13}; //the weights of a group of item types 
		int[] profits = {4,   2,  6,  4,  9,  8,  5}; //the profit obtained from each type - must be same length as weights
		int[] stock =   {6,   8,  5,  5,  2,  4,  7}; //the maximum number of each item 
		int capacity = 300;       //maximum load weight in the truck
        int cost = 100;            //the profit threshold to be achieved (e.g. the cost of transport)
/**/
        
		int n = weights.length;    //number of object types
		
		//calculate the max profit and the max stock levels, for setting up the variables
		int maxstock = 0;
		int maxprofit = 0;
		for (int i = 0; i<n; i++) {
			if (stock[i] > maxstock) {
				maxstock = stock[i];
			}
			maxprofit += stock[i]*profits[i];
		}
		
		
        //VARIABLES
		
		IntVar[] load = new IntVar[n];
		for (int i = 0; i<n; i++) {
			load[i] = VariableFactory.enumerated("object"+i, 0,  stock[i], solver); //how many of each type
		}
		IntVar totalWgt = VariableFactory.enumerated("total wgt", 0,  capacity, solver);    //the total weight of selected items
		IntVar totalProfit = VariableFactory.enumerated("profit", 0, maxprofit*n, solver);     //the total profit of selected items
		
		//CONSTRAINTS
		
		//compute the total weight of the chosen items
		//for each type, multiply the number of items by the unit weight, and sum the total to get total weight
		//i.e. compute the scalar product of the load and weights arrays
		solver.post(IntConstraintFactory.scalar(load, weights, totalWgt));
		
		//constraint the total weight to be less than the capacity
		solver.post(IntConstraintFactory.arithm(totalWgt, "<=", capacity));
		
		//compute the total profit of the chosen items
		//i.e. compute the scalar product of load and profits arrays
		solver.post(IntConstraintFactory.scalar(load, profits,  totalProfit));
		
		//constraint the total profit to be above the cost
		solver.post(IntConstraintFactory.arithm(totalProfit, ">", cost));
		
		
		//SEARCH STRATEGY
		
		//SOLVE
		
		//next line would show each intermediate solution
		Chatterbox.showSolutions(solver);
		//solver.findSolution();
		solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, totalProfit);
		
		//next line gets the last solution and displays it, but using internal order, with extra info
		//System.out.println(solver.getSolutionRecorder().getLastSolution());
		
		//next code block interrogates the variables and gets the solutions for ourselves
		/**/
		for (int i = 0; i<n; i++) {
			System.out.print("weights[" + i + "]=" + weights[i] + "; ");
		}
		System.out.println();
		for (int i = 0; i<n; i++) {
			System.out.print("profits[" + i + "]=" + profits[i] + "; ");
		}
		System.out.println();
		for (int i = 0; i<n; i++) {
			System.out.print("load[" + i + "]=" + load[i].getValue() + "; ");
		}
		System.out.println();
		System.out.println("max individual profit= " + maxprofit);
		System.out.println("totalWgt=" + totalWgt.getValue());
		System.out.println("totalProfit=" + totalProfit.getValue());
		/**/
		Chatterbox.printStatistics(solver);;
		
		
		

	}

}

