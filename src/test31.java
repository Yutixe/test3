import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class test31 {
public static void main(String[] args) {
	
//create a solver object that will solve the problem for us
Solver solver = new Solver();
//create the variables and domains for the problem, and add to the solver
IntVar[] A = VariableFactory.enumeratedArray("V1",  4, 1, 5, solver);

//create the constraints
solver.post(IntConstraintFactory.arithm(A[3],"-", A[0] ,">=", 1));
solver.post(IntConstraintFactory.arithm(A[0],"<", A[1]));
solver.post(IntConstraintFactory.arithm(A[1],"+", A[2],">", 6));
solver.post(IntConstraintFactory.arithm(A[1],"+", A[3],"=", 5));
solver.post(IntConstraintFactory.arithm(A[3],"<", A[2]));

 //use a pretty print object to display the results
 Chatterbox.showSolutions(solver); //just show the final result

 //ask the solver to find a solution
 solver.findSolution();

 //print out the search statistics
 Chatterbox.printStatistics(solver);
}
}
