package valgrindpp.codegen;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import valgrindpp.main.ValgrindConfiguration;

public class WrapperGenerator {
	private ValgrindConfiguration configuration; 
	private List<String> imports;
	private List<Function> functions;
	
	public WrapperGenerator(ValgrindConfiguration configuration) {
		this.configuration = configuration;
		imports = new ArrayList<String>();
		functions = new ArrayList<Function>();
	}
	
	public void generateWrapperFile() throws Exception {
		this.parse();
		
		File file = new File(configuration.GetWrapperCFilePath());
		
		if(file.exists()) {
			file.delete();
		}
		
		if(!file.createNewFile()) {
			throw new WrapperGeneratorException("File name '" + file.getName() + "' already exists");
		}
		
		FileWriter writer = new FileWriter(file);
		
		writer.write("#include \"" + configuration.ValgrindDockerPath + "\"\n");
		
		for(String s: imports) {
			writer.write(s);
			writer.write("\n");
		}
		
		writer.write("\n");
		
		writer.write("void trace(const char* format, ...) {\n"
				+ "    printf(\"%ld - Thread: %lu - \",\n"
				+ "        time(NULL),\n"
				+ "        pthread_self()\n"
				+ "    );\n"
				+ "\n"
				+ "    va_list args;\n"
				+ "    va_start(args, format);\n"
				+ "    vprintf(format, args);\n"
				+ "\n"
				+ "    printf(\"\\n\");\n"
				+ "}\n\n");
		
		for(Function func: functions) {
			writer.write(func.toCString());
			writer.write("\n\n");
		}
		
		
		writer.close();
	}
	
	public void parse() throws Exception {
		Scanner scanner = new Scanner(configuration.GetDefinitionsStream());
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.startsWith("//") || line.isBlank()) continue;
			
			if(line.startsWith("#include")) {
				this.imports.add(line);
			} else {
				this.functions.add(new Function(line));
			}
		}
		
		scanner.close();
	}

	
	public class WrapperGeneratorException extends Exception {
		private static final long serialVersionUID = 1L;

		public WrapperGeneratorException(String message) {
			super(message);
		}
	}
}
