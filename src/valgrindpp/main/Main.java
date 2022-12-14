package valgrindpp.main;

import java.util.List;

import valgrindpp.codegen.WrapperGenerator;
import valgrindpp.main.ValgrindConfiguration.Environment;
import valgrindpp.tester.Test;

public class Main {	
	public static void main(String[] args) {		
		try {
			String projectDirectory = null;
			String dockerExec = null;
			ValgrindConfiguration configuration = null;
			
			if (args.length == 0) {
				configuration = new ValgrindConfiguration();
			} else {
				projectDirectory = args[0];
				
				if (args.length >= 2) {
					dockerExec = args[1];
				}
				
				configuration = new ValgrindConfiguration(projectDirectory, dockerExec);
			}
			
			List<Test> tests = testProject(configuration);
			
			CommandExecutor executor = new CommandExecutor(configuration);
			executor.WriteResults(tests);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static List<Test> testProject(ValgrindConfiguration configuration) throws Exception {
		CommandExecutor executor = new CommandExecutor(configuration);
		
		if (configuration.TestingEnvironment == Environment.Local) {
			executor.deleteContainer();
			executor.createContainer();
		}
		
		WrapperGenerator wrapperGenerator = new WrapperGenerator(configuration);
		wrapperGenerator.generateWrapperFile();
		
		executor.compileWrapper();
		
		if (configuration.UseMakefile()){
			executor.make();
		} else {
			executor.compileStudentCode();
		}
		
		executor.executeWrappedProject();
		
		List<Test> results = configuration.GetTester().test();
		
		if (configuration.PostTestClean) {
			executor.deleteWrapperCFile();
			executor.deleteWrapperObjFile();
			executor.deleteTraces();

			if (configuration.UseMakefile()) {
				executor.makeClean();
			} else {
				executor.deleteBinary();
			}
		}
		
		return results;
	}
}
