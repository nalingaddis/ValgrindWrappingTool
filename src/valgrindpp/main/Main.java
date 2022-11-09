package valgrindpp.main;

import java.util.ArrayList;
import java.util.List;

import valgrindpp.codegen.WrapperGenerator;
import valgrindpp.main.ValgrindConfiguration.Environment;
import valgrindpp.tester.*;

public class Main {	
	public static void main(String[] args) {
		// SwingUtilities.invokeLater(new App());
		
		try {
			String projectDirectory = args[0];
			String dockerExec = null;
			
			if (args.length > 1) {
				dockerExec = args[1];
			}
			
			ValgrindConfiguration configuration = new ValgrindConfiguration(projectDirectory, dockerExec);
			List<Test> tests = testProject(configuration);
			
			for (Test test: tests) {
				System.out.println("Test " + test.name + ": " + (test.passed ? "Passed" : "Failed"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static List<Test> testProject(ValgrindConfiguration configuration) {
		try {
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		List<Test> error = new ArrayList<Test>();
		error.add(new Test("Error: Check console", false));
		return error;
	}
}
