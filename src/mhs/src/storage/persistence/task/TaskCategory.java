//@author A0087048X

package mhs.src.storage.persistence.task;

/**
 * TaskCategory
 * 
 * Task Category enumerated type
 * 
 * Task Categories:
 * 1. Floating
 * 2. Timed
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