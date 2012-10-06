package mhs.src;

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