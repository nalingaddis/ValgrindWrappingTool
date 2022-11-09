package valgrindpp.tester;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BoundedBufferTester extends AbstractTester {

	public BoundedBufferTester(InputStream traceStream) throws Exception {
		super(traceStream);
	}

	public List<Test> test() {
		Map<Long, Set<String>> waits = new HashMap<Long, Set<String>>();
		Map<Long, Set<String>> posts = new HashMap<Long, Set<String>>();
		
		List<Test> tests = new ArrayList<Test>();
		
		for(Trace trace: traces) {
			Map<Long, Set<String>> map;
			
			if(trace.fnname.equals("sem_wait")) {
				map = waits;
			} else if(trace.fnname.equals("sem_post")) {
				map = posts;
			} else {
				continue;
			}
			
			Set<String> sems = map.get(trace.thread);
			if(sems == null) {
				sems = new HashSet<String>();
			}
			sems.add(trace.arguments[0]);
			map.put(trace.thread, sems);
		}
		
		for(Long thread: waits.keySet()) {
			String testName = thread + " waits on correct number of sems.";
			tests.add(new Test(testName, waits.get(thread).size() < 2));
		}
		
		for(Long thread: waits.keySet()) {
			String testName = thread + " posts on correct number of sems.";
			tests.add(new Test(testName, posts.get(thread).size() < 2));
		}
		
		return tests;
	}
}
