package valgrindpp.tester;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import valgrindpp.tester.Trace.TraceParsingException;

public abstract class AbstractTester implements Tester {
	protected List<Trace> traces;
	protected List<String> stdout;
	
	public AbstractTester(InputStream traceStream) throws Exception {
		traces = new ArrayList<Trace>();
		stdout = new ArrayList<String>();
		
		Scanner scanner = new Scanner(traceStream);
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			try {
				traces.add(new Trace(line));
			} catch (TraceParsingException e) {
				stdout.add(line);
			}
		}
		
		scanner.close();
	}
	
	public abstract List<Test> test();
}
