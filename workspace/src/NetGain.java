public class NetGain {
	
	private Double memory;
	private Double gain;
	
	/*
	 * Costruttore senza argomenti
	 */
	public NetGain() {
		this.memory = (double) 0;
		this.gain = (double) 0;
	}
	
	/*
	 * Costruttore con argomenti
	 */
	public NetGain(double memory, double gain) {
		this.memory = memory;
		this.gain = gain;
	}
	
	/*
	 * Costruttore di copia
	 */
	public NetGain(NetGain ng) {
		this.memory = ng.getMemory();
		this.gain = ng.getGain();
	}

	public Double getMemory() {
		return this.memory;
	}
	
	public Double getGain() {
		return this.gain;
	}
	
	/*
	 * Metodo che esegue la somma tra coppie
	 */
	public void add(NetGain ng) {
		this.memory += ng.getMemory();
		this.gain += ng.getGain();
	}
	
	/*
	 * Metodo che aggiunge solo il gain
	 */
	public void add(double gain) {
		this.gain += gain;
	}
	
}
