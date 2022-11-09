package valgrindpp.tester;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SimpleTester extends AbstractTester {

	public SimpleTester(InputStream traceStream) throws Exception {
		super(traceStream);
	}

	public List<Test> test() {
		List<Test> tests = new ArrayList<Test>();
		
		tests.add(countFuncCall("pthread_create", 2));
		tests.add(countFuncCall("pthread_join", 2));
		tests.add(countFuncCall("pthread_mutex_lock", 20));
		tests.add(countFuncCall("pthread_mutex_unlock", 20));
				
		return tests;
	}
	
	private Test countFuncCall(String fnname, int requiredCount) {
		int count = 0; 
		
		for(Trace trace: traces) {
			if(trace.fnname.equals(fnname)) {
				count ++;
			}
		}
		
		return new Test("Called " + fnname, count >= requiredCount);
	}
}
