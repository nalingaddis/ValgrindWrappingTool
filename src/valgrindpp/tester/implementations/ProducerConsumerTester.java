package valgrindpp.tester.implementations;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import valgrindpp.tester.AbstractTester;
import valgrindpp.tester.Test;
import valgrindpp.tester.Trace;

public class ProducerConsumerTester extends AbstractTester {

	public ProducerConsumerTester(InputStream traceStream) throws Exception {
		super(traceStream);
	}

	public List<Test> test() {
		List<Test> tests = new ArrayList<Test>();
		
		tests.add(new Test("Called pthread_mutex_init", calledFunction("pthread_mutex_init")));
		tests.add(new Test("Called pthread_mutex_lock", calledFunction("pthread_mutex_unlock")));
		tests.add(new Test("Called pthread_mutex_unlock", calledFunction("pthread_mutex_unlock")));
		
		return tests;
	}
	
	private boolean calledFunction(String funcName) {
		for(Trace trace: traces) {
			if (trace.fnname.equals(funcName)) {
				return true;
			}
		}
		
		return false;
	}
}
