//@author A0087048X

package mhs.src.storage.persistence.task;

/**
 * TaskCategory
 * 
 * Task Category enumerated type<br>
 * 
 * Task Categories:<br>
 * 1. Floating <br>
 * 2. Timed <br>
 * 3. Deadline
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
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