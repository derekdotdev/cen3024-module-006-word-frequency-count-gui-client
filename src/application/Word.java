package application;

/** Word Class objects are created from HashMap<String, Integer> (key=word, value=frequency) and 
 * implements Comparable<Word> so ArrayList<Word> can be sorted and overrides toString() to print results to console. 
 * @author derekdileo */
public class Word implements Comparable<Word> {

	// Local variables
	private String word;
	private int frequency;

	// Constructor
	public Word(String key, int value) {
		super();
		this.word = key;
		this.frequency = value;
	}
	
	// Getters
	public String getKey() {
		return word;
	}

	public int getValue() {
		return frequency;
	}

	/**Implemented Method that allows Word objects to be compared
	 * in order to allow ArrayList<Word> to be sorted by value (frequency). */
	@Override
	public int compareTo(Word word) {
		return this.frequency - word.getValue();
	}

	/** Method which takes index value (in sorted list) for final output of values to console and GUI
	 * @param index is index location in ArrayList<Word> sorted by frequency (value)  
	 * @return returns a String to be printed to console and GUI */
	public String toString(int index) {
		return "\n" + (index + 1) + ") Word: " + word + "\t\tFrequency: " + frequency;
	}

}
