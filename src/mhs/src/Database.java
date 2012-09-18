package mhs.src;

import java.io.IOException;
import java.util.List;


import org.joda.time.DateTime;

public class Database {

	public Database() {
		initalizeDatabase();
		// syncronize local and web databases
		syncronizeDatabases();
	}

	private void initalizeDatabase() {
		// TODO
	}

	private void syncronizeDatabases() {
		// TODO
	}

	public List<Task> query(int taskId) {
		// TODO
		// fetch records
		// perform query operations on recordset
		// return list of tasks
		return null;
	}

	public List<Task> query(String taskName) {
		// TODO
		// fetch records
		// perform query operations on recordset
		// return list of tasks
		return null;
	}

	public List<Task> query(TaskCategory taskCategory) {
		// TODO
		// fetch records
		// perform query operations on recordset
		// return list of tasks
		return null;
	}

	public List<Task> query(String taskName, TaskCategory taskCategory,
			DateTime startTime, DateTime endTime) {
		// TODO
		// fetch records
		// perform query operations on recordset
		// return list of tasks
		return null;
	}

	public String add(Task task) throws IOException {
		// TODO
		return null;
	}

	public String delete(int taskId) throws IOException {
		// TODO
		return null;
	}

	public String update(Task task) throws IOException {
		// TODO
		return null;
	}
}