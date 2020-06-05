import java.util.LinkedList;

public class Index {

	private int id;
	private Double fixedCost;
	private Double memoryOccupation;
	private LinkedList<Configuration> activeConfigurations; // lista delle configurazioni ATTIVE che questo indice sta servendo
	
	public Index(int indexId) {
		this.id = indexId;
		this.fixedCost = (double) 0;
		this.memoryOccupation = (double) 0;
		this.activeConfigurations = new LinkedList<Configuration>();
	}

	public int getId() {
		return id;
	}

	public Double getFixedCost() {
		return fixedCost;
	}

	public void setFixedCost(Double fixedCost) {
		this.fixedCost = fixedCost;
	}

	public Double getMemoryOccupation() {
		return memoryOccupation;
	}

	public void setMemoryOccupation(Double memoryOccupation) {
		this.memoryOccupation = memoryOccupation;
	}

	public void addConfiguration(Configuration c) {
		this.activeConfigurations.add(c);
	}
	
	public LinkedList<Configuration> getActiveConfigurations() {
		return activeConfigurations;
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
		Index other = (Index) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public String toString() {
		return id + "";
	}
	
	/*
	 * Metodo che verifica se un indice è attivo o meno, sfruttando la lunghezza della sua lista di configurazioni
	 */
	public boolean isActive() {
		return activeConfigurations.size() > 0;
	}
	
	/*
	 * Metodo che accende l'indice (su richiesta di una configurazione):
	 * 1. se l'indice non era attivo, restituisce un NetGain con la memoria aggiuntiva occupata e il costo aggiuntivo pagato
	 * 2. se l'indice era già attivo, restituisce un NetGain(0,0)
	 * 3. aggiunge la configurazione alla lista delle configurazioni servite dall'indice
	 */
	public NetGain turnOnBy(Configuration c) {
		NetGain ng = new NetGain();
		if (isActive() == false)
			ng.add(new NetGain(+ this.memoryOccupation, - this.fixedCost));
		// this.fixedCost ha segno negativo perché è un costo che si aggiunge
		activeConfigurations.add(c);
		return ng;
	}
	
	/*
	 * Metodo con cui si chiede all'indice quale sarebbe il NetGain se si attivasse
	 * Simile al precedente, non aggiunge la configurazione alla lista delle configurazioni servite dall'indice
	 */
	public NetGain testTurnOnBy() {
		if (isActive() == false)
			return new NetGain(+ this.memoryOccupation, - this.fixedCost);
		// this.fixedCost ha segno negativo perché è un costo che si aggiunge
		else 
			return new NetGain();
	}
	
	/*
	 * Metodo che spegne l'indice (su richiesta di una configurazione):
	 * 1. rimuove la configurazione dall'elenco delle configurazioni
	 * 2. se l'indice era attivo e si è spento, restituisce un NetGain con la memoria non più occupata e il costo rimosso
	 * 3. se l'indice era attivo ma lo è ancora, restituisce un NetGain 0,0
	 */
	public NetGain turnOffBy(Configuration c) {
		activeConfigurations.remove(c);
		if (isActive() == false)
			return new NetGain(- this.memoryOccupation, + this.fixedCost);
		// this.memoryOccupation ha segno negativo perché è memoria guadagnata, this.fixedCost ha segno positivo perché è un costo in meno
		else 
			return new NetGain();
	}
	
	/*
	 * Metodo con cui si chiede all'indice quale sarebbe il NetGain se si attivasse
	 * Simile al precedente, non rimuove la configurazione alla lista delle configurazioni servite dall'indice
	 */
	public NetGain testTurnOffBy() {
		if (this.activeConfigurations.size() == 1)
			return new NetGain(- this.memoryOccupation, + this.fixedCost);
		// this.memoryOccupation ha segno negativo perché è memoria guadagnata, this.fixedCost ha segno positivo perché è un costo in meno
		else 
			return new NetGain();
	}
	
}
