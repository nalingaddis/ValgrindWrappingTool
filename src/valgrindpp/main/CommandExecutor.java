package valgrindpp.main;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import valgrindpp.main.ValgrindConfiguration.Environment;
import valgrindpp.tester.Test;

public class CommandExecutor {
	
	ValgrindConfiguration configuration;
	
	public CommandExecutor(ValgrindConfiguration configuration) {
		this.configuration = configuration;
	}
	
	// Command Execution
	public static int execute(String[] command, boolean silent, InputStream input) throws Exception{
		if(!silent) System.out.println("Executing: " + String.join(" ", command));
		
		ProcessBuilder pb = new ProcessBuilder();
		if(silent) {
			pb.command(command);
		} else {
			pb.command(command).inheritIO();
		}
		
		Process process = pb.start();
		
		if(input != null) {
			System.setIn(input);
			input.close();
		}
		
		return process.waitFor();
	}
	
	public int execute(String[] command) throws Exception {
		return execute(command, false, null);
	}
	
	public int executeInDocker(String[] command, boolean silent, InputStream input) throws Exception {
		if (configuration.TestingEnvironment == Environment.Local) {
			String[] dockerCommand = {
					configuration.DockerExec,
					"exec",
					configuration.DockerContainerName,
					"sh",
					"-c",
					String.join(" ", command)
			};
			
			return execute(dockerCommand, silent, input);
		}
		
		return execute(command, silent, input);
	}
	
	public int executeInDocker(String[] command) throws Exception {
		return executeInDocker(command, false, null);
	}
	
	
	// Container Management
	public int createContainer() throws Exception {
		String[] command = new String[]{
				configuration.DockerExec, 
				"run",
				"-it",
				"-d",
				"--mount", 
				"type=bind,src="+configuration.ProjectDirectory+",target=/home",
				"--name",
				configuration.DockerContainerName,
				configuration.DockerImageName
		};
		
		return execute(command);
	}
	
	public int stopContainer() throws Exception {
		String[] command = {
				configuration.DockerExec,
				"stop",
				configuration.DockerContainerName
		};
		
		return execute(command);
	}
	
	public int deleteContainer() throws Exception {
		String[] command = {
				configuration.DockerExec,
				"rm",
				"-f",
				configuration.DockerContainerName
		};
		
		return execute(command);
	}
	
	// Code Compilation
	public int compileWrapper() throws Exception {		
		String[] command = {
				configuration.Compiler, 
				"-c",
				configuration.CompilerFlags, 
				configuration.WrapperCFile, 
				"-o", 
				configuration.WrapperObjFile};
		
		return executeInDocker(command);
	}
	
	public int compileStudentCode() throws Exception {
		File dir = new File(configuration.ProjectDirectory);
		
		List<String> srcFiles = new ArrayList<String>();
		
		for(String filename: dir.list()) {
			if(filename.endsWith(".c") && !filename.equals(configuration.WrapperCFile)) {
				srcFiles.add(filename);
			}
		}
		
		String[] command = new String[srcFiles.size() + 5];
		
		command[0] = configuration.Compiler;
		command[1] = configuration.CompilerFlags;
		command[2] = configuration.WrapperObjFile;
		command[3] = "-o";
		command[4] = configuration.DefaultExecName;
		
		for(int i=0; i<srcFiles.size(); i++) {
			command[i+5] = srcFiles.get(i);
		}

		return executeInDocker(command);
	}
	
	public int make() throws Exception {
		String[] command = {
				"make",
				"wrapped"
		};
		
		return executeInDocker(command);
	}
	
	// directory clean up
	public void deleteWrapperCFile() throws Exception {
		delete(configuration.GetWrapperCFilePath());
	}
	
	public void deleteWrapperObjFile() throws Exception {
		delete(configuration.GetWrapperObjFilePath());
	}
	
	public void deleteBinary() throws Exception {
		delete(configuration.GetDefaultExecPath());
	}
	
	public void deleteTraces() throws Exception {
		delete(configuration.GetTraceFilePath());
	}
	
	private void delete(String path) {
		try {
			Paths.get(path).toFile().delete();
		} catch (Exception ex) {
			System.out.println("Failed to delete file: " + path.toString() + " with exception: " + ex.toString());
		}
	}
	
	public int makeClean() throws Exception {
		String[] command = {
				"make",
				"clean"
		};
		
		return executeInDocker(command);
	}
	
	
	// Program Execution
	public void executeWrappedProject() throws Exception {
		String[] command;
		if (configuration.ExecutionCommand == null || configuration.ExecutionCommand.length == 0) {
			command = new String[] {
					"valgrind",
					"--trace-children=yes",
					"./"+configuration.DefaultExecName,
					">",
					configuration.TraceFile
			};
		} else {
			command = new String[configuration.ExecutionCommand.length + 4];
			
			command[0] = "valgrind";
			command[1] = "--trace-children=yes";
			for(int i=0; i<configuration.ExecutionCommand.length; i++) {
				command[i+2] = configuration.ExecutionCommand[i];
			}
			command[configuration.ExecutionCommand.length+2] = ">";
			command[configuration.ExecutionCommand.length+3] = configuration.TraceFile;
		}
		
		InputStream[] testInputs = configuration.GetTestInputs();
		if (testInputs.length == 0) {
			executeInDocker(command);
		} else {
			for (InputStream stream: testInputs) {
				executeInDocker(command, false, stream);
			}
		}
	}
	
	// Generate Results
	public void WriteResults(List<Test> tests) throws Exception {
		switch (configuration.TestingEnvironment) {
		case Local: 
			WriteResultsToConsole(tests);
			WriteResultsToFile(tests, configuration.GetLocalResultsFilePath());
			break;
		case GradeScope:
			WriteResultsToFile(tests, configuration.GradeScopeResultsFilePath);
			break;
		}
	}
	
	private void WriteResultsToConsole(List<Test> tests) {
		System.out.println("/********** Test Results **********/");
		for (Test test: tests) {
			System.out.println(test.consoleView());
		}
	}
	
	private void WriteResultsToFile(List<Test> tests, String filePath) throws Exception {
		FileWriter writer = getNewFileWriter(filePath);
		
		writer.write(" { \"tests\" : [ ");
		
		for (int i = 0; i < tests.size(); i++) {
			writer.write(tests.get(i).gradescopeView());
			
			if (i != tests.size()-1) {
				writer.write(", ");
			}
		}
		
		writer.write(" ] } ");
		
		writer.close();
	}
	
	public static FileWriter getNewFileWriter(String filePath) throws Exception {
		File file = new File(filePath);
		
		if(file.exists()) {
			file.delete();
		}
		
		if(!file.createNewFile()) {
			throw new Exception("File name '" + file.getName() + "' already exists");
		}
		
		return new FileWriter(file);
	}
}
