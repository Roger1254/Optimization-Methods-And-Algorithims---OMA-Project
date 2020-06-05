import java.util.*;

public class Query {

	private int id;
	private HashMap<Configuration, Double> configurationGainMap;
	private Configuration servingConfiguration; // la configurazione UNICA che serve questa query
	
	public Query(int queryId) {
		this.id = queryId;
		this.configurationGainMap = new HashMap<Configuration, Double>();
		this.servingConfiguration = null;
	}
	
	public int getId() {
		return id;
	}

	public HashMap<Configuration, Double> getConfigurationGainMap() {
		return configurationGainMap;
	}

	public void addConfigurationGain(Configuration c, double gain) {
		this.configurationGainMap.put(c,gain);
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
		Query other = (Query) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public String toString() {
		return id + "";
	}
	
	/*
	 * Metodo che ritorna l'Id della configurazione che sta servendo la query (0 se la query non è associata ad alcuna configurazione)
	 */
	public int getServingConfigurationId() {
		if (servingConfiguration == null) 
			return 0;
		else 
			return servingConfiguration.getId();
	}
	
	/*
	 * Metodo che associa la query a una configurazione:
	 * 1. Chiede alla configurazione di attivarsi, e ottiene il NetGain risultante
	 * 2. Aggiunge al NetGain il guadagno dovuto all'accensione del link
	 * 3. Imposta che la query è servita a quella configurazione
	 */
	public NetGain associateTo(Configuration config) {
		NetGain netGain = config.turnOn(this);
		netGain.add(+ configurationGainMap.get(config)); // segno +: aumento della objective function
		this.servingConfiguration = config;
		return netGain;
	}
	
	/*
	 * Metodo che testa l'associazione della query a una configurazione
	 * Simile al precedente, usa testTurnOn invece di turnOn
	 */
	public NetGain testAssociateTo(Configuration config) {
		NetGain netGain = config.testTurnOn();
		netGain.add(+ configurationGainMap.get(config)); // segno +: aumento della objective function
		return netGain;
	}
	
	/*
	 * Metodo che disassocia la query dalla sua configurazione attuale (se associata a qualcuna)
	 * 1. Chiede alla configurazione di spegnersi, e ottiene il NetGain risultante
	 * 2. Aggiunge al NetGain la perdita dovuta allo spegnimento del link
	 * 3. Imposta che questa query non è servita da alcuna configurazione
	 */
	public NetGain dissociateFrom() {
		NetGain netGain = new NetGain();
		if (servingConfiguration != null) {
			netGain = servingConfiguration.turnOff(this);
			netGain.add(- configurationGainMap.get(servingConfiguration)); // segno -: diminuizione della objective function
			this.servingConfiguration = null;
		}
		return netGain;
	}
	
	/*
	 * Metodo che testa il disassociamento della query dalla sua configurazione attuale
	 * Simile al precedente, usa testTurnOff invece di turnOff
	 */
	public NetGain testDissociateFrom() {
		NetGain netGain = new NetGain();
		if (servingConfiguration != null) {
			netGain = servingConfiguration.testTurnOff();
			netGain.add(- configurationGainMap.get(servingConfiguration)); // segno -: diminuizione della objective function
		}
		return netGain;
	}
	
	/*
	 * Metodo che trova la migliore configurazione disponibile in base gain del link
	 */
	public Configuration findBestConfiguration() {
		Double M = (double) 0;
		Configuration result = null;
		for (Configuration c : configurationGainMap.keySet())
			if (configurationGainMap.get(c) > M) {
				M = configurationGainMap.get(c);
				result = c;
			}
		return result;
	}
	
	/*
	 * Metodo che trova la migliore configurazione disponibile in base gain del link tra quelle attive o attivabili a costo nullo 
	 */
	public Configuration findBestActiveConfiguration() {
		Double M = (double) 0;
		Configuration result = null;
		for (Configuration c : configurationGainMap.keySet())
			if (c.testTurnOn().getMemory() == 0 && configurationGainMap.get(c) > M) {
				M = configurationGainMap.get(c);
				result = c;
			}
		return result;
	}
	
}
