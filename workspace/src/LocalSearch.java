import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class LocalSearch {
	
	private LinkedList<Index> indexes;
	private LinkedList<Configuration> configurations;
	private LinkedList<Query> queries;
	private Double memory;
	private Double usedMemory;
	private Double objFunctionValue;
	
	private static int seed = 0;
	
	public LocalSearch() {
		this.indexes = new LinkedList<Index>();
		this.configurations = new LinkedList<Configuration>();
		this.queries = new LinkedList<Query>();
		this.usedMemory = (double) 0;
		this.objFunctionValue = (double) 0;
	}
	
	/*
	 * Costruisce la copia della struttura dati da utilizzare per la Local Search
	 */
	public void buildStructure(LinkedList<Index> indexes, LinkedList<Configuration> configurations, LinkedList<Query> queries, 
			Double memory) {

		for (Query q : queries)
			this.queries.add(new Query(q.getId()));
		
		for (Index i : indexes)
			this.indexes.add(new Index(i.getId()));
		
		for (Configuration c : configurations)
			this.configurations.add(new Configuration(c.getId()));
		
		this.memory = memory;
		
		// associa correttamente configurazioni e indici
		for (Configuration c : configurations) {
			LinkedList<Index> indexList = c.getIndexList();
			for (Index i : indexList) {
				Index index = this.indexes.get(i.getId()-1);
				this.configurations.get(c.getId()-1).addIndex(index);
			}
		}
		
		for (Index i : indexes)
			this.indexes.get(i.getId()-1).setFixedCost(i.getFixedCost());
		
		for (Index i : indexes)
			this.indexes.get(i.getId()-1).setMemoryOccupation(i.getMemoryOccupation());
		
		// riempie le mappe Configuration-Gain in ogni Query e Query-Gain in ogni Configuration
		for (Configuration c : configurations) {
			HashMap<Query, Double> queryGainMap = c.getQueryGainMap();
			LinkedList<Query> queryList = new LinkedList<Query>(queryGainMap.keySet());
			for (Query q : queryList) {
				Double gain = queryGainMap.get(q);
				this.queries.get(q.getId()-1).addConfigurationGain(this.configurations.get(c.getId()-1), gain);
				this.configurations.get(c.getId()-1).addQueryGain(this.queries.get(q.getId()-1), gain);
			}
		}
		
	}
	
	/*
	 * Metodo che ostruisce le associazioni Query-Configuration a partire da un Chomosome 
	 * Registra contemporaneamente le variazioni di memoria e valore della objective function
	 */
	public void buildWorld(Chromosome chr) {
		for (Query q : queries) {
			int configId = chr.getUsedConfigurations()[q.getId()-1];
			if (configId != 0) {
				Configuration config = configurations.get(configId-1);
				NetGain gain = q.associateTo(config);
				usedMemory += gain.getMemory();
				objFunctionValue += gain.getGain();
			}
		}
	}

	public Double getUsedMemory() {
		return usedMemory;
	}

	public Double getObjFunctionValue() {
		return objFunctionValue;
	}
	
//	/*
//	 * Metodo che genera soluzione iniziale
//	 */
//	public void buildInitialSolution() {
//		
//		Random rand = new Random(seed++);
//		
//		LinkedList<Query> queriesList = new LinkedList<Query>(this.queries);
//		Collections.shuffle(queriesList,rand); // mescola la lista delle queries
//		
//		for (Query query : queriesList) {
//			
//			Configuration bestConfig = query.findBestConfiguration(); // trova la miglior configurazione per quella query
//			if (bestConfig == null)
//				continue;
//			
//			if (bestConfig.testTurnOn().getMemory() + usedMemory > memory) // testa l'accesione della bestConfig
//				return; // se abbiamo ecceduto di memoria, il metodo termina
//			
//			else // possiamo attivare la configurazione
//				for (Query qTemp : bestConfig.getQueryGainMap().keySet()) // associa alla configurazione scelta tutte le query possibili
//					if (qTemp.getServingConfigurationId() == 0) { // se la query non è già servita da altra configurazione
//						NetGain netGain = qTemp.associateTo(bestConfig);
//						usedMemory += netGain.getMemory();
//						objFunctionValue += netGain.getGain();
//					}
//			
//		}
//		
//	}
	
	/*
	 * Metodo che genera soluzione iniziale
	 */
	public void buildInitialSolution() {
		
		Random rand = new Random(seed++);
		
		LinkedList<Configuration> configurationsList = new LinkedList<Configuration>(this.configurations);
		Collections.shuffle(configurationsList,rand); // mescola la lista delle configurazioni
		
		for (Configuration c : configurationsList) {
			
			if (c.testTurnOn().getMemory() + usedMemory > memory) // testa l'accesione della configurazione
				return; // se abbiamo ecceduto di memoria, il metodo termina
			
			for (Query qTemp : c.getQueryGainMap().keySet()) { // associa alla configurazione scelta tutte le query possibili
				if (qTemp.getServingConfigurationId() == 0) { // se la query non è già servita da altra configurazione
					NetGain netGain = qTemp.associateTo(c);
					usedMemory += netGain.getMemory();
					objFunctionValue += netGain.getGain();	
				}
			}
			
		}

	}	
	
	/*
	 * Implementazione metodo localSearch
	 */
	public void localSearch() {
		
		Random rand = new Random();
		
		int iteration = 0; // counter per il numero di iterazioni dell'algoritmo
		int maxIterations = (int) 8*queries.size();
		
		int stuckSearch = 0; // counter per il numero di passi per cui l'algoritmo è rimasto bloccato
		int maxStuckSearch = (int) queries.size();
		
		LinkedList<Query> tabuList = new LinkedList<Query>();
		int tabuListSize = (int) Math.min(7, queries.size()/8);
		
		while (iteration < maxIterations && stuckSearch < maxStuckSearch) {
			
			Query query = queries.get(rand.nextInt(queries.size())); // estrae una query random
			if (tabuList.contains(query)) // se si trova nella tabuList, ne scelgo un'altra
				continue;
			
			Configuration newConfig;
			if (query.getServingConfigurationId() != 0)
				newConfig = configurations.get(query.getServingConfigurationId()-1); // configurazione che attualmente serve la query
			else
				newConfig = null;
			
			NetGain ngDissociate = query.dissociateFrom(); // eseguo dissociazione
			usedMemory += ngDissociate.getMemory();
			objFunctionValue += ngDissociate.getGain();
			
			if (ngDissociate.getGain() > 0) { // se la configurazione di default migliora la situazione
				stuckSearch = 0;
				tabuList.addLast(query); // aggiunge query alla tabuList (in coda)
				if (tabuList.size() > tabuListSize) // se ci sono abbastanza elementi nella lista tabu, libera il primo (in testa)
					tabuList.removeFirst();
				iteration += 1;
				continue; // passo alla prossima iterazione del while
				
			}
			
			LinkedList<Configuration> shuffledList = new LinkedList<Configuration>(query.getConfigurationGainMap().keySet());
			Collections.shuffle(shuffledList, rand); // rimescolamento casuale della lista delle possibili configurazioni
			
			boolean flag = false; // flag per verificare se ho trovato un collegamento migliore
			for (Configuration c : shuffledList) {
				NetGain ngAssociate = query.testAssociateTo(c); // test di associazione con la configurazione
				if (ngDissociate.getGain() + ngAssociate.getGain() > 0 && 
						ngAssociate.getMemory() + this.usedMemory <= this.memory) { // se gain positivo e ci sto di memoria
					newConfig = c;
					flag = true;
					break;
				}
			}
			
			if (newConfig != null) {
				NetGain ngAssociate = query.associateTo(newConfig);
				usedMemory += ngAssociate.getMemory();
				objFunctionValue += ngAssociate.getGain();
			}
			
			if (flag) // se c'è stato un cambiamento di collegamento
				stuckSearch = 0;
			else
				stuckSearch += 1;
			
			tabuList.addLast(query); // aggiunge query alla tabuList (in coda)
			if (tabuList.size() > tabuListSize) // se ci sono abbastanza elementi nella lista tabu, libera il primo (in testa)
				tabuList.removeFirst();
			
			iteration += 1;
			
		}
		
		// ciclo finale di raffinamento
		for (Query q : queries) {
			Configuration bestConfig = q.findBestActiveConfiguration();
			if (bestConfig != null && bestConfig.getId() != q.getServingConfigurationId()) {
				NetGain ng = q.dissociateFrom();
				ng.add(q.associateTo(bestConfig));
				usedMemory += ng.getMemory();
				objFunctionValue += ng.getGain();
			}
		}
		
	}

	/*
	 * Genera un nuovo Chromosome relativo allo stato attuale della LocalSearch
	 */
	public Chromosome generateChromosome() {
		Chromosome chr = new Chromosome(queries.size());
		for (Query q : queries)
			chr.setUsedConfigurations(q.getId()-1,q.getServingConfigurationId());
		chr.setObjFunctionValue(objFunctionValue);
		chr.setUsedMemory(usedMemory);
		return chr;
	}
	
	/*
	 * Metodo che effettua tutti i collegamenti possibili dopo una genetica
	 */
	public void reassemble() {
		
		Random rand = new Random();
		
		LinkedList<Query> queriesList = new LinkedList<Query>(this.queries);
		Collections.shuffle(queriesList,rand); // mescola la lista delle queries
		
		for (Query query : queriesList) {
			
			if (query.getServingConfigurationId() != 0) // se la query è già servita procedo oltre
				continue;
			
			Configuration bestConfig = query.findBestActiveConfiguration(); // trova la migliore configurazione attiva o attivabile a costo di memoria nullo
			if (bestConfig == null)
				continue;
			
			// associo la miglior configurazione alla query attuale
			NetGain ng = query.associateTo(bestConfig);
			usedMemory += ng.getMemory();
			objFunctionValue += ng.getGain();
			
//			// associo tutte le query spente possibili
//			for (Query qTemp : bestConfig.getQueryGainMap().keySet()) // associa alla configurazione scelta tutte le query possibili
//				if (qTemp.getServingConfigurationId() == 0) { // se la query non è già servita da altra configurazione
//					NetGain ng = qTemp.associateTo(bestConfig);
//					usedMemory += ng.getMemory();
//					objFunctionValue += ng.getGain();
//				}
			
		}
		
	}
	
	/*
	 * Metodo di raffinamento della soluzione infeasible di memoria
	 */
	public void refining() {
		
		SortedMap<Double,Configuration> sortedConfigurations = new TreeMap<Double,Configuration>();
		
		for (Configuration config : configurations) { // ciclo sulle configurazioni e le inserisco nella SortedMap
			
			if (!config.isActive()) // se non attiva passo oltre
				continue;
			
			double gainTemp = (double) 0;
			for (Query qTemp : config.getServingQueries())
				gainTemp += qTemp.getConfigurationGainMap().get(config);
			
			while (sortedConfigurations.containsKey(gainTemp))
				gainTemp += 1;
			sortedConfigurations.put(gainTemp, config);
			
		}
		
		while (usedMemory > memory) {
			
			Configuration worstConfig = sortedConfigurations.remove(sortedConfigurations.firstKey());
			
			for (Query qTemp : worstConfig.getQueryGainMap().keySet()) // spegne la configurazione peggiore disassociando tutte le query associate
				if (qTemp.getServingConfigurationId() == worstConfig.getId()) {
					NetGain ng = qTemp.dissociateFrom();
					usedMemory += ng.getMemory();
					objFunctionValue += ng.getGain();
				}
			
		}
		
	}
	
}
