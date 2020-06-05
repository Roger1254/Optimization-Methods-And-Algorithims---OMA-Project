import java.io.*;
import java.util.*;

public class Workspace {
	
	private LinkedList<Index> indexes;
	private LinkedList<Configuration> configurations;
	private LinkedList<Query> queries;
	private Double memory;
	
	public Workspace() {
		this.indexes = new LinkedList<Index>();
		this.configurations = new LinkedList<Configuration>();
		this.queries = new LinkedList<Query>();
	}

	public LinkedList<Index> getIndexes() {
		return indexes;
	}

	public LinkedList<Configuration> getConfigurations() {
		return configurations;
	}

	public LinkedList<Query> getQueries() {
		return queries;
	}
	
	public Double getMemory() {
		return memory;
	}
	
	/*
	 * Metodo che legge da file l'istanza del problema
	 */
	public String loadInstance(String filename) throws IOException {
		
		try {
			
			FileReader r = new FileReader(filename);
			BufferedReader br = new BufferedReader(r);
			
			String line = br.readLine(); 
			
			line = line.replace("N_QUERIES: ", "");
			int numQueries = Integer.parseInt(line);
			
			for (int i=1; i<=numQueries; i++)
				this.queries.add(new Query(i));
			
			System.out.println("Queries number: " + numQueries);
//			System.out.println("Queries list");
//			System.out.println(this.getQueries());
			
			line = br.readLine().replace("N_INDEXES: ","");
			int numIndexes = Integer.parseInt(line);
			
			for (int i=1; i<=numIndexes; i++)
				this.indexes.add(new Index(i));
			
			System.out.println("Indexes number: " + numIndexes);
//			System.out.println("Indexes list");
//			System.out.println(this.getIndexes());
			
			line = br.readLine().replace("N_CONFIGURATIONS: ","");
			int numConfigurations = Integer.parseInt(line);
			
			for (int i=1; i<=numConfigurations; i++)
				this.configurations.add(new Configuration(i));
			
			System.out.println("Configurations number: " + numConfigurations);
//			System.out.println("Configurations list");
//			System.out.println(this.getConfigurations());
			
			line = br.readLine().replace("MEMORY: ","");
			memory = Double.parseDouble(line);
			br.readLine();
			
			System.out.println("Memory: " + this.memory);
			
			String[] str;
			
			for (int k=0; k<numConfigurations; k++) {
				line = br.readLine();
				str = line.split(" ");
				for (int j=0; j<numIndexes; j++)
					if (Integer.parseInt(str[j])==1)
						configurations.get(k).addIndex(indexes.get(j));			
			}
			
			line = br.readLine();
			for (int i=0; i<numIndexes; i++)
				indexes.get(i).setFixedCost(Double.parseDouble(br.readLine()));
			
			line = br.readLine();
			for (int i=0; i<numIndexes; i++)
				indexes.get(i).setMemoryOccupation(Double.parseDouble(br.readLine()));
			
			line = br.readLine();
			for (int k=0; k<numConfigurations; k++) {
				line = br.readLine();
				str = line.split(" ");
				for (int j=0; j<numQueries; j++)
					if (Integer.parseInt(str[j])!=0) {
						queries.get(j).addConfigurationGain(configurations.get(k), Double.parseDouble(str[j]));
						configurations.get(k).addQueryGain(queries.get(j), Double.parseDouble(str[j]));
					}			
			}
			
			br.close();
			
        }
		
        catch (IOException e) {
            System.out.println("Error reading file '" + filename + "'");
            e.printStackTrace();
        }
		
		return filename.replace(".odbdp", "_OMAAL_group08.sol");
	}
	
	/*
	 * Metodo che scrive su file la soluzione
	 */
	public void writeSolution(Chromosome chr, String filename)  throws IOException {
		
		try {
			
			PrintWriter w = new PrintWriter(filename);
			
			for (int i=1; i<=configurations.size(); i++) {
				String line = "";
				for (int j=0; j<queries.size(); j++) {
					if (chr.getUsedConfigurations()[j] == i)
						line += "1 ";
					else
						line += "0 ";
				}
				w.println(line);
			}

			w.flush();
			w.close();
			
		}
		
		catch (Exception e) { 
			e.printStackTrace();
		} 
		
	}
	
	/*
	 * Metodo che scrive su file i dati della soluzione
	 */
	public void writeSolutionData(Chromosome chr, String filename) throws IOException {
		
		try {
			
			PrintWriter w = new PrintWriter(filename);
			
			w.println("Objective function value:");
			w.println(chr.getObjFunctionValue());
			
			w.println("Memory:");
			w.println(chr.getUsedMemory() + "/" + memory);
			
			w.println();
			for (int i=0; i<queries.size(); i++)
				w.println(chr.getUsedConfigurations()[i]);
			
			w.flush();
			w.close();
			
		}
		
		catch (Exception e) { 
			e.printStackTrace();;
		}
		
	}
	
	/*
	 * Metodo che scrive su file la migliore soluzione in assoluto e aggiorna i suoi dati
	 */
	public void writeBestSolution(Chromosome chr, String filename) throws IOException {
		
		try {
			
			// legge il file con i dati della migliore soluzione in assoluto (se non esiste, viene lanciata l'eccezione)
			FileReader r = new FileReader("bestData_" + filename); 
			BufferedReader br = new BufferedReader(r);
			
			String line = br.readLine();
			line = br.readLine();
			double best = Double.parseDouble(line); // salvo il valore della miglior objective function ottenuta finora
			
			System.out.println("Previous best: " + best + " New best: " + chr.getObjFunctionValue());
			br.close();
			
			if (chr.getObjFunctionValue() > best) { // se il nuovo valore è migliore del precedente, sovrascrivo il file
				this.writeSolution(chr, "best_" + filename);
				this.writeSolutionData(chr, "bestData_" + filename);
				System.out.println("File updated: best_" + filename + ". Best OFV: " + chr.getObjFunctionValue());
			}
			
		} 
		
		catch (Exception e) { // se il file bestData_instanceXX.txt non esiste, viene creato
			this.writeSolution(chr, "best_" + filename);
			this.writeSolutionData(chr, "bestData_" + filename);
			System.out.println("File created: best_" + filename + ". Best OFV: " + chr.getObjFunctionValue());
		}
		
	}
	
	/*
	 * Metodo che crea una soluzione iniziale
	 */
	public Chromosome buildInitialSolution() {
		
		LocalSearch ls = new LocalSearch();
		ls.buildStructure(indexes, configurations, queries, memory);
		ls.buildInitialSolution();
		ls.localSearch();
		return ls.generateChromosome();
	}
	
//	/*
//	 * Implementazione local search
//	 */
//	public LinkedList<Chromosome> localSearch(LinkedList<Chromosome> chrList) {
//		
//		LinkedList<Chromosome> improvedList = new LinkedList<Chromosome>();
//		
//		for (Chromosome chr : chrList) {
//	
//			LocalSearch ls = new LocalSearch();
//			ls.buildStructure(indexes, configurations, queries, memory);
//			ls.buildWorld(chr);
//			ls.reassemble();
//			ls.refining();
//			ls.localSearch();
//			Chromosome improvedChr = ls.generateChromosome();
//			improvedList.add(improvedChr);
//	
//		}
//		
//		return improvedList;
//	}
	
	/*
	 * Implementazione local search parallela
	 */
	public LinkedList<Chromosome> localSearch(LinkedList<Chromosome> chrList) {
		
		LinkedList<Chromosome> improvedList = new LinkedList<Chromosome>();
		LinkedList<Thread> threadHandler = new LinkedList<Thread>();
		
		for (Chromosome chr : chrList) {
			threadHandler.push(new MyThread(indexes,configurations,queries,memory,chr,improvedList));
			threadHandler.getFirst().start();
		}
		
		for (Thread t : threadHandler) {
			try	{ 
				t.join();
        	}
        	catch (Exception e) { 
        		e.printStackTrace();
        	}
		}
		
		return improvedList;
	}
	
	/*
	 * Metodo che seleziona il mix tra migliori genitori, migliori figli, figli random e nuove soluzioni
	 */
	public LinkedList<Chromosome> selection(LinkedList<Chromosome> childrenList, LinkedList<Chromosome> parentList,
			int bestParents, int bestChildren, int randomChildren, int newInitialSolutions) {
		
		Random rand = new Random();
		
		Collections.sort(parentList);
		Collections.sort(childrenList);
		
		LinkedList<Chromosome> bestChrList = new LinkedList<Chromosome>();
		
		// aggiungo i migliori genitori
		for (int k=0; k<bestParents; k++)
			bestChrList.add(parentList.pop());
		
		// aggiungo i migliori figli
		for (int k=0; k<bestChildren; k++)
			bestChrList.add(childrenList.pop());
		
		Collections.shuffle(childrenList,rand);
		
		// aggiungo altri figli random
		for (int k=0; k<randomChildren; k++)
			bestChrList.add(childrenList.pop());
		
		// aggiungo nuove soluzioni
		for (int k=0; k<newInitialSolutions; k++)
			bestChrList.add(this.buildInitialSolution());
		
		return bestChrList;
	}

	/*
	 * Metodo che esegue la genetica 
	 * 1. riceve una lista di cromosomi genitori (in numero pari)
	 * 2. rimescola la lista dei genitori
	 * 3. accoppia il cromosoma in posizione 2i con quello in posizione 2i+1
	 * 4. passa le coppie alla funzione generateChildren che ritorna una lista di due cromosomi, corrispondenti ai due nuovi figli
	 */
	public LinkedList<Chromosome> genetic(LinkedList<Chromosome> chrList) {
		
		Random rand = new Random();
		Collections.shuffle(chrList, rand);
		LinkedList<Chromosome> geneticResultList = new LinkedList<Chromosome>();
		
		for (int i=0; i<chrList.size()/2; i++)
			geneticResultList.addAll(generateChildren(chrList.get(2*i), chrList.get(2*i+1)));
		
		return geneticResultList;
	}
	
	/*
	 * Metodo che genera casualmente due tagli e crea due nuovi cromosomi mescolando i due genitori
	 */
	public LinkedList<Chromosome> generateChildren(Chromosome chr1, Chromosome chr2) {
		
		LinkedList<Chromosome> chrList = new LinkedList<Chromosome>();
		
		if (chr1.getObjFunctionValue() == chr2.getObjFunctionValue()) { // se i due genitori sono (probabilmente) uguali
			chrList.add(this.buildInitialSolution());
			chrList.add(this.buildInitialSolution());
			return chrList; // restituisco due figli casuali
		}
		
		Random rand = new Random();
		Chromosome chr3 = new Chromosome(queries.size());
		Chromosome chr4 = new Chromosome(queries.size());
		
		int cut1 = rand.nextInt(queries.size());
		int cut2 = rand.nextInt(queries.size());
		
		while (cut1 == cut2) // evito che siano uguali re-estraendo cut2
			cut2 = rand.nextInt(queries.size());
		
		if (cut1 > cut2) { // voglio che cut1 sia il più piccolo
			int tmp = cut1;
			cut1 = cut2;
			cut2 = tmp;
		}
		
		// divido il ciclo for in "prima di cut1", "tra cut1 e cut2" e "dopo cut2"
		
		for (int i=0; i<cut1; i++) { 
			chr3.setUsedConfigurations(i,chr1.getUsedConfigurations()[i]);
			chr4.setUsedConfigurations(i,chr2.getUsedConfigurations()[i]);
		}
		for (int i=cut1; i<cut2; i++) {
			chr3.setUsedConfigurations(i,chr2.getUsedConfigurations()[i]);
			chr4.setUsedConfigurations(i,chr1.getUsedConfigurations()[i]);
		}
		for (int i=cut2; i<queries.size(); i++) {
			chr3.setUsedConfigurations(i,chr1.getUsedConfigurations()[i]);
			chr4.setUsedConfigurations(i,chr2.getUsedConfigurations()[i]);
		}
		
		chrList.add(chr3);
		chrList.add(chr4);
			
		return chrList;
	}
	
}
