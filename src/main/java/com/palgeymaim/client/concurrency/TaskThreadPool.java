package com.palgeymaim.client.concurrency;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.concurrent.Task;

public class TaskThreadPool {
	
	private ExecutorService executorService;

	public TaskThreadPool() {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}
	
	public <T> void submitTask(Task<T> task) {
		executorService.submit(task);
	}
	
	public <T> Future<?> submit(Runnable task) {
		return executorService.submit(task);
	}
	
	public <T> List<Future<T>> invokeAll(List<Callable<T>> tasks) throws InterruptedException {
		return executorService.invokeAll(tasks);
	}	

}
