package valgrindpp.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import valgrindpp.tester.*;

public class ValgrindConfiguration {
	/** Change the following **/
	
	/* BoundedBuffer Example */
	public String DefinitionsDirectory = "BoundedBuffer";
	public Tester GetTester() throws Exception { return new BoundedBufferTester(new FileInputStream(GetTraceFilePath())); }
	public String[] ExecutionCommand = new String[0];
	public Environment TestingEnvironment = Environment.Local;
	
	/* MutexLru Example */
//	public String DefinitionsDirectory = "MutexLru";
//	public Tester GetTester() throws Exception { return new MutexLruTester(new FileInputStream(GetTraceFilePath())); }
//	public String[] ExecutionCommand = new String[]{"./lru-mutex-wrapped", "-c", "2"};
//	public Environment TestingEnvironment = Environment.Local;
	
	/** Avoid Changing Below **/
	public String DefinitionsRootDirectory = "/Definitions";
	public String DefinitionsFile = "WrapperDefinitions";
	
	public String DockerContainerName = "grader-container";
	public String DockerImageName = "nalingaddis/valgrind";
	public String ValgrindDockerPath = "/valgrind/include/valgrind.h";
	
	public String Compiler = "gcc";
	public String CompilerFlags = "-pthread";
	public String DefaultExecName = "WrappedStudentCode";
	public String GetDefaultExecPath() { return Paths.get(ProjectDirectory, DefaultExecName).toString(); }
	
	public String WrapperCFile = "ValgrindWrapper.c";
	public String GetWrapperCFilePath() { return Paths.get(ProjectDirectory, WrapperCFile).toString(); }
	
	public String WrapperObjFile = "ValgrindWrapper.o";
	public String GetWrapperObjFilePath() { return Paths.get(ProjectDirectory, WrapperObjFile).toString(); }

	public String TraceFile = "Traces";
	public String GetTraceFilePath() { return Paths.get(ProjectDirectory, TraceFile).toString(); }
	
	public boolean UseMakefile() {
		File[] files = new File(ProjectDirectory).listFiles();
		
		for (File file: files) {
			if (file.getName().equals("Makefile")) {
				return true;
			}
		}
		
		return false;
	}

	public String InputsDirectory = "Inputs"; 
	
	public boolean PostTestClean = true;
	
	public String ProjectDirectory;
	public String DockerExec;
	public ValgrindConfiguration(String projectDirectory, String dockerExec) throws Exception {
		this.ProjectDirectory = projectDirectory;
		
		if (dockerExec == null || dockerExec.isEmpty()) {
			this.DockerExec = FindDockerExec();
		} else {
			this.DockerExec = dockerExec;
		}
	}
	
	private String FindDockerExec() throws Exception {
		boolean isWindows = System.getProperty("os.name").contains("Windows");
		
		String[] command = { isWindows ? "where" : "which", "docker" };
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(command);
			Process process = pb.start();
			
			InputStream stream = process.getInputStream();
			String output = new String(stream.readAllBytes());
			String[] results = output.split("\n");
			
			if (isWindows) {
				for (String result: results) {
					if (result.trim().endsWith(".exe")) {
						return result.trim();
					}
				}
				
				throw new Exception("No executable found");
			} else {
				return results[0].trim();
			}
		} catch (Exception ex) {
			throw new Exception("Failed to find docker executable, please provide path to docker as the second command line argument.");
		}
	}

	public InputStream GetDefinitionsStream() {
		String definitionsPath = Paths.get(DefinitionsRootDirectory, DefinitionsDirectory, DefinitionsFile).toString();
		return ValgrindConfiguration.class.getResourceAsStream(definitionsPath.replace('\\', '/'));
	}

	public InputStream[] GetTestInputs() {
		try {
			File inputsDirectory = new File(Paths.get(DefinitionsRootDirectory, DefinitionsDirectory, "Inputs").toString());
			List<InputStream> streams = new ArrayList<InputStream>();
			
			for (File input: inputsDirectory.listFiles()) {
				streams.add(new FileInputStream(input));
			}
			
			return streams.toArray(new InputStream[0]);
		} catch (Exception ex){
			return new InputStream[0];
		}
	}
	
	public enum Environment {
		Local,
		GradeScope
	}
}
