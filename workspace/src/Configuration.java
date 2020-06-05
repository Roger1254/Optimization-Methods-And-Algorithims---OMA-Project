import java.util.HashMap;
import java.util.LinkedList;

public class Configuration {

	private int id;
	private LinkedList<Index> indexList;
	private HashMap<Query, Double> queryGainMap;
	private LinkedList<Query> servingQueries; // lista delle query che la configurazione sta attualmente servendo
	
	public Configuration(int configurationId) {
		this.id = configurationId;
		this.indexList = new LinkedList<Index>();
		this.queryGainMap = new HashMap<Query, Double>();
		this.servingQueries = new LinkedList<Query>();
	}

	public int getId() {
		return id;
	}

	public LinkedList<Index> getIndexList() {
		return indexList;
	}
	
	public void addIndex(Index i) {
		this.indexList.add(i);
	}
	
	public HashMap<Query, Double> getQueryGainMap() {
		return queryGainMap;
	}

	public void addQueryGain(Query q, double gain) {
		this.queryGainMap.put(q,gain);
	}
	
	public LinkedList<Query> getServingQueries() {
		return servingQueries;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Configuration other = (Configuration) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public String toString() {
		return id + "";
	}
	
	/*
	 * Metodo per verificare se la configurazione è attiva
	 */
	public boolean isActive() {
		return servingQueries.size() > 0;
	}
	
	/*
	 * Metodo con cui una query chiede alla configurazione di attivarsi
	 * Restituisce il NetGain della sua (eventuale) accensione
	 * 1. aggiunge sempre la query alla lista delle query che questa configurazione sta servendo
	 * 2. se la configurazione non era già attiva, attiva tutti i suoi indici e calcola il NetGain risultante dall'attivazione
	 */
	public NetGain turnOn(Query q) {
		NetGain netGain = new NetGain();
		if (this.isActive() == false)
			for (Index i : indexList)
				netGain.add(i.turnOnBy(this));
		servingQueries.add(q);
		return netGain;
	}
	
	/*
	 * Metodo con cui si chiede alla configurazione quale sarebbe il NetGain se si attivasse
	 * Simile al precedente, non aggiunge la query alla lista delle query che questa configurazione sta servendo
	 */
	public NetGain testTurnOn() {
		NetGain netGain = new NetGain();
		if (this.isActive() == false)
			for (Index i : indexList)
				netGain.add(i.testTurnOnBy());
		return netGain;
	}
	
	/*
	 * Metodo con cui una query prova a spegnere una configurazione
	 * 1. rimuove la query dalla lista delle query servite
	 * 2. se la configurazione non serve più alcuna query, si spegne:
	 * 		- rimuove la configurazione dalle liste degli indici
	 * 		- ritorna un NetGain indicante variazione di memoria e di costo derivanti dall'eventuale spegnimento degli indici.
	 */
	public NetGain turnOff(Query q) {
		NetGain netGain = new NetGain();
		servingQueries.remove(q);
		if (isActive() == false)
			for (Index i : indexList)
				netGain.add(i.turnOffBy(this));
		return netGain;
	}
	
	/*
	 * Metodo con cui si chiede alla configurazione quale sarebbe il NetGain se si spegnesse
	 * Simile al precedente, non rimuove la query alla lista delle query che questa configurazione sta servendo
	 */
	public NetGain testTurnOff() {
		NetGain netGain = new NetGain();
		if (servingQueries.size() == 1)
			for (Index i : indexList)
				netGain.add(i.testTurnOffBy());
		return netGain;
	}
	
}
