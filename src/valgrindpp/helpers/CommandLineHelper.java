package valgrindpp.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class CommandLineHelper {
	
	public static int execute(String[] command) throws Exception {
		return execute(command, false, null);
	}
	
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
//			input.transferTo(process.getOutputStream());
			System.setIn(input);
			input.close();
		}
		
		return process.waitFor();
	}
	
	public static int executeInDocker(String[] command) throws Exception {
		return executeInDocker(command, false, null);
	}
	
	public static int executeInDocker(String[] command, boolean silent, InputStream input) throws Exception {
		String[] dockerCommand = {
				DockerHelper.DOCKER_PATH,
				"exec",
				DockerHelper.CONTAINER_NAME,
				"sh",
				"-c",
				String.join(" ", command)
		};
		
		return execute(dockerCommand, silent, input);
	}
	
	public static void delete(Path filepath) throws Exception {
		try {
		    Files.delete(filepath);
		} catch (NoSuchFileException x) {
		    System.err.format("%s: no such" + " file or directory%n", filepath);
		} catch (DirectoryNotEmptyException x) {
		    System.err.format("%s not empty%n", filepath);
		} catch (IOException x) {
		    // File permission problems are caught here.
		    System.err.println(x);
		}
	}
	
	public static int deleteInDocker(Path filepath) throws Exception {
		String[] command = {
				"rm",
				"-f",
				filepath.toString()
		};
		
		return executeInDocker(command);
	}
}
