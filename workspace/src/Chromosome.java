public class Chromosome implements Comparable<Chromosome> {

	private int[] usedConfigurations;
	// vettore "stato del sistema": 
	// posizione i (query con Id i+1) -> Id configurazione che serve la query
	private Double usedMemory;
	private Double objFunctionValue;
	
	public Chromosome(int numQueries) {
		usedConfigurations = new int[numQueries];
		for (int i = 0; i < numQueries; i++)
			usedConfigurations[i] = 0;
		usedMemory = (double) 0;
		objFunctionValue = (double) 0;
	}
	
	public Double getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(Double usedMemory) {
		this.usedMemory = usedMemory;
	}
	
	public int[] getUsedConfigurations() {
		return usedConfigurations;
	}

	public void setUsedConfigurations(int query, int usedConfigurationId) {
		this.usedConfigurations[query] = usedConfigurationId;
	}

	public Double getObjFunctionValue() {
		return objFunctionValue;
	}

	public void setObjFunctionValue(Double objFunctionValue) {
		this.objFunctionValue = objFunctionValue;
	}

	@Override
	public int compareTo(Chromosome otherChr) {
		if (this.objFunctionValue > otherChr.objFunctionValue)
			return -1;
		if (this.objFunctionValue < otherChr.objFunctionValue)
			return 1;
		return 0;
	}
	
}
