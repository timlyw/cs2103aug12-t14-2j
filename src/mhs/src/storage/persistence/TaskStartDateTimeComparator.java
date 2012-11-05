//@author A0087048X
package mhs.src.storage.persistence;

import java.util.Comparator;

import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;

/**
 * TaskStartDateTimeComparator
 * 
 * Comparator class for Task startDateTime which compares task by startDateTime
 * 
 * Comparator Logic 
 * - floating task first, followed by timed/deadline tasks 
 * - timed/deadline tasks are sorted by earliest startDateTime first
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
class TaskStartDateTimeComparator implements Comparator<Task> {
	@Override
	public int compare(Task o1, Task o2) {

		// Comparision when Floating Task(s) are involved
		if (o1.getTaskCategory() == TaskCategory.FLOATING
				&& o2.getTaskCategory() == TaskCategory.FLOATING) {
			return 0;
		}
		if (o1.getTaskCategory() == TaskCategory.FLOATING
				&& o2.getTaskCategory() != TaskCategory.FLOATING) {
			return -1;
		}
		if (o1.getTaskCategory() != TaskCategory.FLOATING
				&& o2.getTaskCategory() == TaskCategory.FLOATING) {
			return 1;
		}

		// Compare Timed and Deadline Tasks
		if (o1.getStartDateTime().isBefore(o2.getStartDateTime())) {
			return -1;
		}
		if (o1.getStartDateTime().isAfter(o2.getStartDateTime())) {
			return 1;
		}

		return 0;
	}
}