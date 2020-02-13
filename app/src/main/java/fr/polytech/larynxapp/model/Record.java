package fr.polytech.larynxapp.model;

/**
 * Created by XU Jiaoqiang on 2018/3/20.
 */

/**
 * Class representing a record in the database.
 *
 * WARNING : this class only represent entries of the database, any modification to the objects will NOT affect the database
 */
public class Record {
	/**
	 * The name of the record.
	 */
    private String name;
	
	/**
	 * The path to the record.
	 */
	private String path;

	/**
	 * The jitter corresponding to the record
	 */
	private double jitter;

	/**
	 * The shimmer corresponding to the record
	 */
	private double shimmer;

	/**
	 * The fundamental frequency corresponding to the record
	 */
	private double f0;

	/**
     * Record default builder.
	 *
	 * Name and path will be empty.
     */
    public Record(){
        name = "";
        path = "";
        jitter = 0.0;
        shimmer = 0.0;
        f0 = 0.0;
    }

	/**
	 * Constructor with 5 parameters.
	 *
	 * @param name the name of the record
	 * @param path the path to the record
	 */
	public Record(String name, String path){
		this.name = name;
		this.path = path;
	}

	/**
     * Constructor with 5 parameters.
	 *
     * @param name the name of the record
     * @param path the path to the record
	 * @param jitter the jitter corresponding to the record
	 * @param shimmer the shimmer corresponding to the record
     */
    public Record(String name, String path, double jitter, double shimmer, double f0){
        this.name = name;
        this.path = path;
        this.jitter = jitter;
        this.shimmer = shimmer;
        this.f0 = f0;
    }

	/**

	 * Getter for the name of the record.
	 *
	 * @return the name of the record
	 */
	public String getName() {
        return name;
    }
	
	/**
	 * Setter for the name of the record.
	 *
	 * @param name the new name for the record
	 */
	public void setName( String name ) {
        this.name = name;
    }
	
	/**
	 * Getter for the path of the record.
	 *
	 * @return the path to the record
	 */
	public String getPath() {
        return path;
    }
	
	/**
	 * Setter for the path of the record.
	 *
	 * @param path new path to the record
	 */
	public void setPath( String path ) {
        this.path = path;
    }

	/**
	 * Getter for the jitter of the record
	 *
	 * @return the jitter of the record
	 */
	public double getJitter() {
		return jitter;
	}

	/**
	 * Setter for the jitter of the record
	 * @param jitter the jitter of the record
	 */
	public void setJitter(double jitter) {
		this.jitter = jitter;
	}

	/**
	 * Getter for the shimmer of the record
	 * @return the shimmer of the record
	 */
	public double getShimmer() {
		return shimmer;
	}

	/**
	 * Setter for the shimmer of the record
	 * @param shimmer the shimmer of the record
	 */
	public void setShimmer(double shimmer) {
		this.shimmer = shimmer;
	}

	/**
	 * Getter for the f0 of the record
	 * @return the fundamental frequency of the record
	 */
	public double getF0() {
		return f0;
	}

	/**
	 * Setter for the f0 of the record
	 * @param f0 the fundamental frequency of the record
	 */
	public void setF0(double f0) {
		this.f0 = f0;
	}

	/**
	 * ToString method
	 */
	@Override
	public String toString() { return name;}
}
