package valgrindpp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import valgrindpp.tester.*;
import valgrindpp.tester.implementations.*;

public class ValgrindConfiguration {
	public enum Environment {
		Local,
		GradeScope
	}
	
	/** Uncomment one example below to see different testing scenarios **/
	
	/* MutexLru Example */
	// Demonstrates how to assess projects with a Makefile and custom execution command
	
	public String DefinitionsDirectory = "MutexLru";
	public Tester GetTester() throws Exception { return new MutexLruTester(new FileInputStream(GetTraceFilePath())); }
	public String[] ExecutionCommand = new String[]{"./lru-mutex-wrapped", "-c", "2"};
	
	/* ProducerConsumer Example */
	// Demonstrates how to provide input to a function (see Definitions/Inputs directory) and use default project compilation
	
//	public String DefinitionsDirectory = "ProducerConsumer";
//	public Tester GetTester() throws Exception { return new ProducerConsumerTester(new FileInputStream(GetTraceFilePath())); }
//	public String[] ExecutionCommand = new String[0];
	
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

	public String InputsDirectory = "Inputs"; 
	public boolean PostTestClean = true;
	
	public String GradeScopeSubmissionDirectory = "/autograder/submission";
	public String GradeScopeResultsFilePath = "/autograder/results/results.json";
	
	public String ResultsFile = "results.json";
	public String GetLocalResultsFilePath() { return Paths.get(ProjectDirectory, ResultsFile).toString(); }
	
	public String ProjectDirectory;
	public String DockerExec;
	public Environment TestingEnvironment;
	
	public ValgrindConfiguration(String projectDirectory, String dockerExec) throws Exception {
		if (Files.exists(Paths.get(projectDirectory))) {
			this.ProjectDirectory = new File(projectDirectory).getAbsolutePath();
		} else if(Files.exists(Paths.get(System.getProperty("user.dir"), projectDirectory))) {
			this.ProjectDirectory = Paths.get(System.getProperty("user.dir"), projectDirectory).toString();
		} else {
			throw new Exception("Invalid project directory: " + projectDirectory);
		}		
		
		if (dockerExec == null || dockerExec.isEmpty()) {
			this.DockerExec = FindDockerExec();
		} else {
			this.DockerExec = dockerExec;
		}
		
		this.TestingEnvironment = Environment.Local;
	}
	
	public ValgrindConfiguration() {
		this.TestingEnvironment = Environment.GradeScope;
		this.ProjectDirectory = GradeScopeSubmissionDirectory;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Testing Environment: " + this.TestingEnvironment.toString());
		
		return sb.toString();
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
		return getResourceAsStream(definitionsPath);
	}

	public InputStream[] GetTestInputs() {
		try {
			List<InputStream> streams = new ArrayList<InputStream>();
	
			String inputsDirectory = Paths.get(DefinitionsRootDirectory, DefinitionsDirectory, "Inputs").toString();
			
			for (String inputFile: getResourceFiles(inputsDirectory)) {
				String inputFilePath = Paths.get(inputsDirectory, inputFile).toString();
				streams.add(getResourceAsStream(inputFilePath));
			}
			
			return streams.toArray(new InputStream[0]);
		} catch (Exception ex){
			return new InputStream[0];
		}
	}
	
	private List<String> getResourceFiles(String path) throws IOException {
		
	    List<String> filenames = new ArrayList<>();

	    try (
	            InputStream in = getResourceAsStream(path);
	            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
	        String resource;

	        while ((resource = br.readLine()) != null) {
	            filenames.add(resource);
	        }
	    }

	    return filenames;
	}
	
	private InputStream getResourceAsStream(String resource) {
		resource = resource.replace('\\', '/');
		return ValgrindConfiguration.class.getResourceAsStream(resource);
	}
	
	public boolean UseMakefile() {
		File[] files = new File(ProjectDirectory).listFiles();
		
		for (File file: files) {
			if (file.getName().equals("Makefile")) {
				return true;
			}
		}
		
		return false;
	}
}
