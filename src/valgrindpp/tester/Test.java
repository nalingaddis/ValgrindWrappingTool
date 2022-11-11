package valgrindpp.tester;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;

public class Test {	
	public double score;
	public double max_score;
	public Status status;
	public String name;
	public double number;
	public String output;
	public String[] tags;
	public Visiblity visiblity;
	
	public enum Status {
		passed,
		failed
	}
	
	public enum Visiblity {
		hidden,
		after_dute_date,
		after_published,
		visible
	}
	
	public boolean passed() {
		return this.status == Status.passed;
	}
	
	public Test(
			double score,
			double max_score,
			Status status,
			String name,
			double number,
			String output,
			String[] tags,
			Visiblity visiblity) {
		this.score = score;
		this.max_score = max_score;
		this.status = status;
		this.name = name;
		this.number = number;
		this.output = output;
		this.tags = tags;
		this.visiblity = visiblity;
	}
	
	public Test(String name, boolean passed) {
		this(
			passed ? 1.0 : 0.0,
			1.0,
			passed ? Status.passed : Status.failed,
			name,
			0,
			"",
			new String[0],
			Visiblity.visible);
	}
	
	public Component appView() {
		JLabel view = new JLabel((passed() ? "PASSED - " : "FAILED - ") + name);
		view.setForeground(passed() ? Color.GREEN : Color.RED);
		return view;
	}
	
	public String consoleView() {
		return "name: " + name + ", score: " + score + "/" + max_score + ", output: " + output;
	}
	
	public String gradescopeView() {
		return " { " 
			+ "\"score\": " + toJson(score) + ", "
			+ "\"max_score\": " + toJson(max_score) + ", "
			+ "\"status\": " + toJson(status.name()) + ", "
			+ "\"name\": " + toJson(name) + ", "
			+ "\"number\": " + toJson(number) + ", "
			+ "\"output\": " + toJson(output) + ", "
			+ "\"tags\": " + toJson(tags) + ", "
			+ "\"visiblity\": " + toJson(visiblity.name())
		+ " }";
	}
	
	private String toJson(double d) {
		return Double.toString(d);
	}
	
	private String toJson(String string) {
		return "\"" + string + "\"";
	}
	
	private String toJson(String[] array) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		
		for (int i = 0; i < array.length; i++) {
			sb.append(toJson(array[i]));
			
			if (i != array.length-1) {
				sb.append(", ");
			}
		}
		
		sb.append(']');
		
		return sb.toString();
	}
}