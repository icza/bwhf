package hu.belicza.andras.hackerdb;

/**
 * Wrapper class to hold the filter parameters.
 * 
 * @author Andras Belicza
 */
public class FiltersWrapper {
	
	public String    name;
	public boolean[] gateways;
	public int       minReportCount;
	public String    sortByValue;
	public boolean   descendantSorting;
	public int       page;
	public int       pageSize;
	
}
