import java.io.File;
import java.io.IOException;
import java.util.Scanner;


public class WeldersReader {

	private int numberOfWelders;   //the number of welders in the problem
	private int numberOfJobs;      //the number of jobs in the problem
    private int maxWelders;   //the maximum number of welders at any one time
	private int[][] welderJobRequirements; //for each welder, what jobs are they required for
    private int[] minWelders; //minimum number of welders needed for each job
    private int[] welderRate;         //the hourly rate for each welder
    private int costBound;            //the bound on the cost for a decision problem

	public WeldersReader(String filename) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(filename));
			numberOfWelders = scanner.nextInt();
			numberOfJobs = scanner.nextInt();
			maxWelders = scanner.nextInt();
			welderJobRequirements = new int[numberOfWelders][numberOfJobs];
			for (int w=0;w<numberOfWelders;w++){
				for (int j=0;j<numberOfJobs;j++) {
					welderJobRequirements[w][j] = scanner.nextInt();
				}
			}	
			minWelders = new int[numberOfJobs];
			for (int j=0;j<numberOfJobs;j++) {
				minWelders[j] = scanner.nextInt();
			}
			welderRate = new int[numberOfWelders];
			for (int w=0;w<numberOfWelders;w++) {
				welderRate[w] = scanner.nextInt();
			}
			costBound = scanner.nextInt();
		}
		catch (IOException e) {
			System.out.println("File error:" + e);
		}
	}
		
	    

	public int getNumberOfWelders() {
		return numberOfWelders;
	}



	public int getNumberOfJobs() {
		return numberOfJobs;
	}



	public int getMaxWelders() {
		return maxWelders;
	}



	public int[][] getWelderJobRequirements() {
		return welderJobRequirements;
	}



	public int[] getMinWelders() {
		return minWelders;
	}



	public int[] getWelderRate() {
		return welderRate;
	}

	public int getCostBound() {
		return costBound;
	}



	public static void main(String[] args) {
		WeldersReader reader = new WeldersReader("welders3.txt");
		int limit = reader.getNumberOfWelders();
		int[] minWelderArray = reader.getMinWelders();
		int[] a = reader.getWelderRate();
		for (int i = 0; i < limit; i++) {
		   System.out.print(a[i] + " ");
		}
		System.out.println();
	}
}
