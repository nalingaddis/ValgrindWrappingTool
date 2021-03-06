package valgrindpp.codegen;

import java.io.InputStream;
import java.util.Scanner;

public class Parser {
	private String filename;
	
	public Parser(String filename) {
		this.filename = filename;
	}
	
	public Wrapper parse() throws Exception {
		InputStream stream = Parser.class.getResourceAsStream("/"+filename);
		Scanner scanner = new Scanner(stream);
		
		Wrapper wrapper = new Wrapper();
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.startsWith("//") || line.isBlank()) continue;
			
			if(line.startsWith("#include")) {
				wrapper.imports.add(line);
			} else {
				wrapper.functions.add(new Function(line));
			}
		}
		
		scanner.close();
		
		return wrapper;
	}
}
