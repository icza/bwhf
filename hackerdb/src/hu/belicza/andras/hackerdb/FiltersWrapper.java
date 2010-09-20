package hu.belicza.andras.hackerdb;

/**
 * Wrapper class to hold the filter parameters.
 * 
 * @author Andras Belicza
 */
public class FiltersWrapper {
	
	public String    name;
	public boolean[] gateways;
	public boolean[] gameEngines;
	public String    mapName;
	public int       minReportCount;
	public String    reportedWithKey;
	public String    sortByValue;
	public boolean   ascendantSorting;
	public int       page;
	public int       pageSize;
	public String    stepDirection;
	
}
