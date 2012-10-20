/**
 * Task Category enumerated type
 * 
 * @author timlyw 
 */

package mhs.src.storage;

public enum TaskCategory {
	FLOATING("FLOATING"), TIMED("TIMED"), DEADLINE("DEADLINE");

	private final String value;

	private TaskCategory(String category) {
		this.value = category;
	}

	public String getValue() {
		return value;
	}
}