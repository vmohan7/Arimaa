package ArimaaEngineInterface;

//import java.io.*;
//import java.util.*;
//import ai_util.*;

public class ArimaaEngineInterfaceCommand {
	public static final String AEI = "aei";
	
	String command;
	String[] arguments;
	public ArimaaEngineInterfaceCommand() {
	}
	public ArimaaEngineInterfaceCommand(String line) {
		arguments = line.split(" ");
		command = arguments[0];
	}
	public String getRestOfCommand() {
		String result="";
		for (int i=1;i<arguments.length;i++) {
			result+=arguments[i]+" ";
		}
		return result.trim();
	}
}