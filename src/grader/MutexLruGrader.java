package grader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MutexLruGrader extends AbstractGrader {

	public MutexLruGrader(String studentDir, String filename) throws Exception {
		super(studentDir, filename);
	}

	@Override
	public List<Test> grade() {
		List<Test> tests = new ArrayList<Test>();
		
		String[] testnames = {
				"Blocks on valid condition variables",
				"Conditional variable waiters awoken",
				"Broadcast (not signal) condition variables",
				"Lock acquired after shutdown"
		};
		
		boolean usingCondVars = blockOnConditionVariables();
		tests.add(new Test(testnames[0], usingCondVars));
		tests.add(new Test(testnames[1], !usingCondVars ? false : releaseConditionedWaiters()));
		tests.add(new Test(testnames[2], !usingCondVars ? false : broadcastVsSignal()));
		
//		tests.add(new Test(testnames[3], lockAfterShutdown()));
		
		return tests;
	}
	
	private boolean blockOnConditionVariables() {
				
		Set<String> seen = new HashSet<String>(), inits = new HashSet<String>();
		
		for(Trace trace: traces) {
			if(trace.fnname.equals("pthread_cond_init")) {
				seen.add(trace.arguments[0]);
				inits.add(trace.arguments[0]);
			}
			
			if(trace.fnname.equals("pthread_cond_wait")) {
				if(!inits.contains(trace.arguments[0])) return false;
				return true;
			}
		}
		
		return seen.size() == 0;
	}
	
	private boolean releaseConditionedWaiters() {
		
		Set<String> waiters = new HashSet<String>(), seen = new HashSet<String>();
		for(Trace trace: traces) {
			if(trace.fnname.equals("pthread_cond_wait")) {				
				if(!seen.contains(trace.arguments[0])) 
						waiters.add(trace.arguments[0]);
				seen.add(trace.arguments[0]);
			} 
			
			if(trace.fnname.equals("pthread_cond_broadcast") 
				|| trace.fnname.equals("pthread_cond_signal")){
				waiters.remove(trace.arguments[0]);
			}
		}
		
		return waiters.size() == 0;
	}
	
	private boolean broadcastVsSignal() {
		
		for(Trace trace: traces) {
			if(trace.fnname.equals("pthread_cond_signal")) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean lockAfterShutdown() {		
		Long shutdownThread = (long) -1;
		String lock = "";
		
		for(Trace trace: traces) {
			if(trace.fnname.equals("pthread_mutex_init")) {
				lock = trace.arguments[0];
			}
			if(trace.fnname.equals("shutdown_threads")) {
				shutdownThread = trace.thread;
			}
			if(trace.fnname.equals("pthread_mutex_lock") 
					&& shutdownThread == trace.thread 
					&& lock.equals(trace.arguments[0])) {
				return true;
			}
		}
		
		return false;
	}
}
