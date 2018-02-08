import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class Solver {

	WindFarmLayoutEvaluator wfle;
	boolean[][] individuals;
	double[] fits;
	Random rand;
	int num_individuals;
	int soln[];
	int newSoln[];
	double xcoord[];
	double ycoord[];

	ArrayList<double[]> grid;

	public Solver(WindFarmLayoutEvaluator evaluator) {
		wfle = evaluator;
		rand = new Random();
		grid = new ArrayList<double[]>();

		// set up any parameter here, e.g pop size, cross_rate etc.
		num_individuals = 50; // change this to anything you want

	}

	public void run_cw() {

		/************ set up grid for scenario chosen ***************************/
		// do not change or remove this section of code
		// centers must be > 8*R apart and avoid obstacles

		double interval = 8.001 * wfle.getTurbineRadius();

		for (double x = 0.0; x < wfle.getFarmWidth(); x += interval) {
			for (double y = 0.0; y < wfle.getFarmHeight(); y += interval) {
				boolean valid = true;
				for (int o = 0; o < wfle.getObstacles().length; o++) {
					double[] obs = wfle.getObstacles()[o];
					if (x > obs[0] && y > obs[1] && x < obs[2] && y < obs[3]) {
						valid = false;
					}
				}

				if (valid) {
					double[] point = { x, y };
					grid.add(point);
				}
			}
		}

		/************ initialize a population: *****************************/

		// the variable grid.size() denotes the
		// maximum number of turbines for the given scenario

		individuals = new boolean[num_individuals][grid.size()];
		fits = new double[num_individuals];

		for (int p = 0; p < num_individuals; p++) {
			for (int i = 0; i < grid.size(); i++) {
				individuals[p][i] = rand.nextBoolean();
			}
		}
		

		/****** evaluate initial population *************************/

		// this populates the fit[] array with the fitness values for each solution
		evaluate();

		// loop through fit[] and print the lowest fitness
		double lowestFit = fits[0];
		int bestFitId = 0;
		boolean[] bestSolution = new boolean[grid.size()];
		
		for(int j=0; j<num_individuals; j++) {
			double individualFitness = fits[j];
			
			if(individualFitness<lowestFit) {
				bestFitId = j;
				lowestFit = fits[j];
				//System.out.println("New fitness" + lowestFit);
			}
		}
		//Set the best solution found equal to the lowest value found in array
		for(int i=0; i<grid.size();i++) {
			bestSolution[i] = individuals[bestFitId][i];
		}

		/**** PUT YOUR OPTIMISER CODE HERE ***********/
		//Simulated Annealing
		int currentTemp = 1000;
		int finalTemp = 0; 
		int iterStart = 0;
		int maxIter = 10000;
		double coolingRate = 0.004;
		double simAnnealFit = 0;
		int acceptWorse = 0;
		int eval = 0;
		ArrayList<Double> list = new ArrayList<Double>();
		final String FNAME = "C:\\Users\\Liam Keogh\\Documents\\practice310.txt";

		//Arrays to store values
		boolean[] current = new boolean[grid.size()];
		boolean[] simAnneal = new boolean[grid.size()];

		
		//Print out the current best solution before simulated annealing commences
		System.out.println(lowestFit + ":Lowest fitness from array provided to algorithm");
		while (eval < 2000) {
			//Copy current solution
			for (int i = 0; i < grid.size(); i++) {
				current[i] = individuals[bestFitId][i];
				simAnneal[i] = individuals[bestFitId][i];
			}

			//Pick two positions on the grid
			int gridPos1 = (int) (Math.random() * grid.size());
			int gridPos2 = (int) (Math.random() * grid.size());

			//Ensure that gridPos1 and gridPos2 are different
			while (gridPos1 == gridPos2) {
				gridPos2 = (int) (Math.random() * grid.size());
			}
			
			//While the current solution is not at gridPos1, generate a random grid position
			while(!current[gridPos1]) {
				gridPos1=(int) (Math.random() * grid.size());
			}
			
			//While the algorithm is at gridPos2, generate a random grid position
			while(simAnneal[gridPos2]) {
				gridPos2 = (int) (Math.random() * grid.size());
			}
			
			
			//Swap the positions of gridPos1 and gridPos2
			boolean swap = current[gridPos1];
			simAnneal[gridPos1] = current[gridPos2];
			simAnneal[gridPos2] = swap;
			list.add(simAnnealFit);
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(FNAME))){
				for(Double line:list) {
					bw.write(line + "\n");
					bw.newLine();
				}
				bw.close();
			} catch(IOException e) {
				e.printStackTrace();
			}

			
			//Calculate energy of both solutions
			simAnnealFit = evaluate_individual(simAnneal);
			double currentBestFit = fits[bestFitId];
			System.out.println(eval + " " +simAnnealFit);
						
			//Keep track of best solution by only replacing it if a better fitness
			//value is found by the algorithm
			if(simAnnealFit <= currentBestFit)
			{
				for(int i=0; i<grid.size();i++) {
					individuals[bestFitId][i] = simAnneal[i];
					fits[bestFitId] = simAnnealFit;
				} 
				
			}
			else {
				//Decide if a worse solution should be accepted and update it as the fitness
				//value for the current solution
				double difference = simAnnealFit - currentBestFit;
				double random = Math.random();
				double prob = Math.exp(-(double)Math.abs(difference)/currentTemp);
				
				//If successful then replace current solution only
				if(prob > random)
				{
					for(int i=0; i<grid.size();i++) {
						individuals[bestFitId][i] = current[i];
						fits[bestFitId] = currentBestFit;
					}
					acceptWorse++;
				}
			}
			
			//Cool the system
			currentTemp *= 1-coolingRate;
			iterStart++;
			eval = wfle.getNumberOfEvaluation();
		}
		System.out.println(simAnnealFit + " :Final fitness from Simulated Annealing");
		System.out.println(acceptWorse + " Worse solutions accepeted");
		System.out.println(bestFitId + " Best fitness after SA");
		
		
		/*
		//Hill Climber for last 500 iterations
		int hcIterations = 500;
		double hillClimberFit = 0;
		boolean[] currentSA = new boolean[grid.size()];
		boolean[] hillClimber = new boolean[grid.size()];
		int generation = 0;
		
		System.out.println("Made it to the hill climber");
		System.out.println(bestFitId + " Best fitness provided to HC");
		for(int i=0;i<hcIterations;i++) {
			
			for (int j = 0; j < grid.size(); j++) {
				currentSA[j] = individuals[bestFitId][j];
				hillClimber[j] = individuals[bestFitId][j];
			}
			
			//Pick two positions on the grid
			int gridPos1 = (int) (Math.random() * grid.size());
			int gridPos2 = (int) (Math.random() * grid.size());

			//Ensure that gridPos1 and gridPos2 are different
			while (gridPos1 == gridPos2) {
				gridPos2 = (int) (Math.random() * grid.size());
			}
			
			//While the current solution is not at gridPos1, generate a random grid position
			while(!current[gridPos1]) {
				gridPos1=(int) (Math.random() * grid.size());
			}
			
			//While the algorithm is at gridPos2, generate a random grid position
			while(hillClimber[gridPos2]) {
				gridPos2 = (int) (Math.random() * grid.size());
			}
			
			
			//Swap the positions of gridPos1 and gridPos2
			boolean swap = currentSA[gridPos1];
			hillClimber[gridPos1] = currentSA[gridPos2];
			hillClimber[gridPos2] = swap;
			
			//Calculate energy of both solutions
			hillClimberFit = evaluate_individual(hillClimber);
			double currentBestFit = fits[bestFitId];
			
			if(hillClimberFit <= currentBestFit)
			{
				for(int j=0; j<grid.size();j++) {
					individuals[bestFitId][j] = hillClimber[j];
					fits[bestFitId] = hillClimberFit;
				} 
				
			}
			generation++;
		}
		
		System.out.println(hillClimberFit + " :Final fitness from Hill Climber after " + generation + " generations");
		System.out.println(hillClimberFit + " vs " + simAnnealFit);*/
		
		
	}
	
	// evaluate a single chromosome
	private double evaluate_individual(boolean[] child) {

		int nturbines = 0;
		for (int i = 0; i < grid.size(); i++) {
			if (child[i]) {
				nturbines++;
			}
		}

		double[][] layout = new double[nturbines][2];
		int l_i = 0;
		for (int i = 0; i < grid.size(); i++) {
			if (child[i]) {
				layout[l_i][0] = grid.get(i)[0];
				layout[l_i][1] = grid.get(i)[1];
				l_i++;
			}
		}

		double coe;
		if (wfle.checkConstraint(layout)) {
			wfle.evaluate(layout);
			coe = wfle.getEnergyCost();
			//System.out.println("layout valid");
		} else {
			coe = Double.MAX_VALUE;
		}

		return coe;

	}

	// evaluates the whole population
	private void evaluate() {
		double minfit = Double.MAX_VALUE;
		for (int p = 0; p < num_individuals; p++) {
			int nturbines = 0;
			for (int i = 0; i < grid.size(); i++) {
				if (individuals[p][i]) {
					nturbines++;
				}
			}

			double[][] layout = new double[nturbines][2];
			int l_i = 0;
			for (int i = 0; i < grid.size(); i++) {
				if (individuals[p][i]) {
					layout[l_i][0] = grid.get(i)[0];
					layout[l_i][1] = grid.get(i)[1];
					l_i++;
				}
			}

			double coe;
			if (wfle.checkConstraint(layout)) {
				wfle.evaluate(layout);
				coe = wfle.getEnergyCost();
			} else {
				coe = Double.MAX_VALUE;
			}

			fits[p] = coe;
			if (fits[p] < minfit) {
				minfit = fits[p];
			}
		}
		//System.out.println(minfit + "Minimum fitness");
	}

}
