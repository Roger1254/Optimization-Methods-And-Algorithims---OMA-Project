import java.util.LinkedList;

public class MyThread extends Thread {
	
	private LinkedList<Index> indexes;
	private LinkedList<Configuration> configurations;
	private LinkedList<Query> queries;
	private Double memory;
	private Chromosome chr;
	private LinkedList<Chromosome> chrList;
	
	public MyThread(LinkedList<Index> indexes, LinkedList<Configuration> configurations, LinkedList<Query> queries, 
			Double memory, Chromosome chr, LinkedList<Chromosome> chrList) {
		this.indexes = indexes;
		this.configurations = configurations;
		this.queries = queries;
		this.memory = memory;
		this.chrList = chrList;
		this.chr = chr;
	}

	@Override
	public void run() {
		
		LocalSearch ls = new LocalSearch();
		ls.buildStructure(indexes, configurations, queries, memory);
		ls.buildWorld(chr);
		
		ls.reassemble();
		ls.refining();
		
		ls.localSearch();
		Chromosome improvedChr = ls.generateChromosome();
		
		synchronized(chrList) {
			chrList.add(improvedChr);
		}
		
	}

}
