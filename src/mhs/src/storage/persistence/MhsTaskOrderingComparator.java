//@author A0087048X
package mhs.src.storage.persistence;

import java.util.Comparator;

import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;

/**
 * MhsTaskOrderingComparator
 * 
 * Comparator class for ordering MHS Tasks
 * 
 * Comparator Logic:<br>
 * - floating task first, followed by timed/deadline tasks<br>
 * - timed/deadline tasks are sorted by earliest startDateTime first<br>
 * - tasks are sorted by isDone if other conditions are equal
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
class MhsTaskOrderingComparator implements Comparator<Task> {
	@Override
	public int compare(Task task1, Task task2) {

		// Comparision when Floating Task(s) are involved
		if (isBothFloatingTasks(task1, task2)) {
			// Comparision for done
			if (task1IsDoneAndTask2IsNotDone(task1, task2)) {
				return 1;
			}
			if (task1IsNotDoneAndTask2IsDone(task1, task2)) {
				return -1;
			}
			return 0;
		}
		if (task1isFloatingAndTask2IsNonFloating(task1, task2)) {
			return -1;
		}
		if (task1IsFloatingAndTask2IsNonFloating(task1, task2)) {
			return 1;
		}

		// Compare Timed and Deadline Tasks
		if (task1StartsBeforeTask2(task1, task2)) {
			return -1;
		}
		if (task1StartsAfterTask2(task1, task2)) {
			return 1;
		}
		if (hasSameStartTime(task1, task2)) {
			// Comparision for done
			if (task1IsDoneAndTask2IsNotDone(task1, task2)) {
				return 1;
			}
			if (task1IsNotDoneAndTask2IsDone(task1, task2)) {
				return -1;
			}
			return 0;
		}
		return 0;
	}

	protected boolean task1IsNotDoneAndTask2IsDone(Task task1, Task task2) {
		return !task1.isDone() && task2.isDone();
	}

	protected boolean task1IsDoneAndTask2IsNotDone(Task task1, Task task2) {
		return task1.isDone() && !task2.isDone();
	}

	protected boolean hasSameStartTime(Task task1, Task task2) {
		return task1.getStartDateTime().equals(task2.getStartDateTime());
	}

	protected boolean task1StartsAfterTask2(Task task1, Task task2) {
		return task1.getStartDateTime().isAfter(task2.getStartDateTime());
	}

	protected boolean task1StartsBeforeTask2(Task task1, Task task2) {
		return task1.getStartDateTime().isBefore(task2.getStartDateTime());
	}

	protected boolean task1IsFloatingAndTask2IsNonFloating(Task task1,
			Task task2) {
		return task1.getTaskCategory() != TaskCategory.FLOATING
				&& task2.getTaskCategory() == TaskCategory.FLOATING;
	}

	protected boolean task1isFloatingAndTask2IsNonFloating(Task task1,
			Task task2) {
		return task1.getTaskCategory() == TaskCategory.FLOATING
				&& task2.getTaskCategory() != TaskCategory.FLOATING;
	}

	protected boolean isBothFloatingTasks(Task o1, Task o2) {
		return o1.getTaskCategory() == TaskCategory.FLOATING
				&& o2.getTaskCategory() == TaskCategory.FLOATING;
	}
}
