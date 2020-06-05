import java.io.IOException;
import java.util.LinkedList;

public class Test {
	
	public static void main(String[] args) throws IOException {

		// Da riga di comando:
		if ((args.length != 3 && args.length != 4) || !args[1].contains("-t"))  {
			System.out.println("Error in input parameters: Insert 'instanceXX.odbdp -t time benchmark'");
			System.exit(-1);
		}
		String filename = args[0]; // Nome del file
		long availableTime = Long.parseLong(args[2]); // Tempo disponibile in secondi
		double benchmark;
		if (args.length == 4)
			benchmark = Double.parseDouble(args[3]);
		else
			benchmark = Double.MAX_VALUE;
		
		// Input:
//		String filename = "instance01.odbdp"; // Nome del file
//		long availableTime = 300; // Tempo disponibile in secondi
//		double benchmark = Double.MAX_VALUE;
		
		long startTime = System.currentTimeMillis();
		
		Workspace s = new Workspace();
		System.out.println("Loading file: " + filename);
		System.out.println();
		filename = s.loadInstance(filename);
		System.out.println();
		
		double bestObjectiveFunctionValue = (double) 0;
		double tempBestObjectiveFunctionValue = (double) 0; // per gestire i restart
		Chromosome bestSolution = new Chromosome(s.getQueries().size());
		
		// Parametri algoritmo genetico
		final int N = 4;
		final int N_RESTART_METHOD = 50;
		final int N_POPULATION = 8*N;
		final int N_BEST_PARENTS = N;
		final int N_BEST_CHILDREN = 2*N;
		final int N_RANDOM_CHILDREN = 4*N;
		final int N_NEW_INITIAL_SOLUTIONS = N;
		
		// Costruzione popolazione iniziale
		
		LinkedList<Chromosome> chrList = new LinkedList<Chromosome>();
		for (int i=0; i < N_POPULATION; i++)
			chrList.add(s.buildInitialSolution());
		
		for (Chromosome chr: chrList) {
			if (chr.getObjFunctionValue() > bestObjectiveFunctionValue) {
				bestObjectiveFunctionValue = chr.getObjFunctionValue();
				bestSolution = chr;
				s.writeSolution(bestSolution, filename);
				System.out.println("Initial population. New best objective function value: " + bestObjectiveFunctionValue);
			}
		}
		
		LinkedList<Chromosome> improvedList = new LinkedList<Chromosome>();
		improvedList.addAll(chrList);
		
		// Algoritmo genetico
		
		int restartMethod = 0;
		int iteration = 0;
		
		while (System.currentTimeMillis() - startTime < 1000*availableTime) {
			
			if (restartMethod >= N_RESTART_METHOD) { // se sono bloccato, ricomincio aggiungendo tutte soluzioni nuove
						
				System.out.println("Stuck Algorithm: Restart at iteration " + iteration);
				restartMethod = 0;
				improvedList = s.selection(improvedList, improvedList, 0, 0, 0, N_POPULATION);
				tempBestObjectiveFunctionValue = 0;
			}

			LinkedList<Chromosome> parentList = new LinkedList<Chromosome>(improvedList);
			chrList = s.genetic(improvedList);
			
			improvedList = s.localSearch(chrList);
			improvedList = s.selection(improvedList, parentList,
					N_BEST_PARENTS,N_BEST_CHILDREN,N_RANDOM_CHILDREN,N_NEW_INITIAL_SOLUTIONS);
			
			restartMethod++;
			iteration++;
			
			for (Chromosome chr : improvedList) {
				if (chr.getObjFunctionValue() > bestObjectiveFunctionValue) {
					bestObjectiveFunctionValue = chr.getObjFunctionValue();
					bestSolution = chr;
					s.writeSolution(bestSolution, filename);
					System.out.println("Iteration " + iteration + ". New best objective function value: " + bestObjectiveFunctionValue);
					restartMethod = 0;
				}
				if (chr.getObjFunctionValue() > tempBestObjectiveFunctionValue){ // per gestire i restart
					tempBestObjectiveFunctionValue = chr.getObjFunctionValue();
					restartMethod = 0;
				}
			}
			
			if (bestObjectiveFunctionValue == benchmark)
				break;
			
		}
		
		System.out.println();
		System.out.println("Algorithm stopped at iteration " + iteration + " after " + (System.currentTimeMillis()-startTime) + " milli-seconds");
		System.out.println("Best objective function value: " + bestObjectiveFunctionValue);
		System.out.println("Used memory: " + bestSolution.getUsedMemory() + "/" + s.getMemory());
		s.writeBestSolution(bestSolution, filename);
		
	}
	
}
