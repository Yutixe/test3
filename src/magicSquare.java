import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


public class magicSquare {

	public static void main(String[] args) {

		//create the solver object
        Solver solver = new Solver();
        
        
        int n=3;   //n is the length of the side of the square
        int T = n*(n*n + 1)/2;    //T is the sum for teh rows, columns and diagonals
        
        //create a 2D array of IntVars for the square
        IntVar[][] square = VariableFactory.enumeratedMatrix("square",  n,  n,  1,  n*n, solver);
        
        //create an IntVar for the target, since it is required for the sum constraint
        //the first way below is the recommended way to do this, the second is ad hoc
        IntVar target = VariableFactory.fixed(T, solver);
        //IntVar target = VariableFactory.enumerated("target",  T,  T, solver);
        
        //now create references to the decision variables, in different arrays, to make it easier to write the constraints
        IntVar[] flatVars = new IntVar[n*n];
        IntVar[][] transpose = new IntVar[n][n];
        IntVar[] diagdown = new IntVar[n];
        IntVar[] diagup = new IntVar[n];
        
        for (int i = 0; i<n; i++) {
        	for (int j = 0; j<n; j++) {
        		flatVars[(n*i)+j] = square[i][j];
        		transpose[j][i] = square[i][j];
        	}
        	diagdown[i] = square[i][i];
        	diagup[i] = square[(n-1)-i][i];
        }
        
        //now post the constraints on the rows, columns and diagonal
         for (int i = 0; i<n; i++) {
        	 solver.post(IntConstraintFactory.sum(square[i], target));
        	 solver.post(IntConstraintFactory.sum(transpose[i], target));
         }
         solver.post(IntConstraintFactory.sum(diagdown, target));
         solver.post(IntConstraintFactory.sum(diagup, target));
         
         
         //now make sure that all values are different
         
         //comparing alldifferent over the full set of vars, vs
         //pairwise not-equals constraints, plus alldifferent over each row, column and diagonal separately
         //also checking the result of restricting propagation to bounds consistency rather than full GAC
         
         //solver.post(IntConstraintFactory.alldifferent(flatVars, "BC"));
         /**/
         int sq = n*n;
         for (int i = 0; i<sq; i++)
        	 for (int j = i+1; j<sq; j++)
        		 solver.post(IntConstraintFactory.arithm(flatVars[i], "!=", flatVars[j]));
         /**/
         /**/
      /*   for (int i = 0; i<n; i++) {
             solver.post(IntConstraintFactory.alldifferent(square[i],"BC"));
             solver.post(IntConstraintFactory.alldifferent(transpose[i],"BC"));
         }
         solver.post(IntConstraintFactory.alldifferent(diagdown,"BC"));
         solver.post(IntConstraintFactory.alldifferent(diagup,"BC"));
        */
         /**/
         
         //breaking symmetries, to reduce search
         solver.post(IntConstraintFactory.arithm(square[0][0], "<", square[n-1][n-1]));
         solver.post(IntConstraintFactory.arithm(square[0][0], "<", square[0][n-1]));
         solver.post(IntConstraintFactory.arithm(square[0][0], "<", square[n-1][0]));
         solver.post(IntConstraintFactory.arithm(square[0][n-1], "<", square[n-1][0]));
         
         //trying to change the search strategy to speed things up
         //the default version of impact has the parameters 2, 3 and 10, but sets the final
         //flag to true
         solver.set(IntStrategyFactory.impact(flatVars, 2,3,10, 0, false));
         
         Chatterbox.showSolutions(solver);
         solver.findSolution();
         Chatterbox.printStatistics(solver);
	}

}

