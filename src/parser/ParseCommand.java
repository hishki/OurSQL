package parser;

import java.util.Scanner;
import java.util.regex.Pattern;

public class ParseCommand {
	
	static Pattern DELIMS = Pattern.compile("[,\\(\\);\\=\\s]+"); 
	
	public ParserTypes parseCommand(String command){
		Scanner scanner = new Scanner(command);
		for(CommandTypes commandType :CommandTypes.values()){
			if(scanner.findInLine(commandType.getCommandText()) != null)
			{
				ParserTypes parserType = null;
				try {
					Class<?> c = commandType.getCommandClass();
					parserType = (ParserTypes) c.newInstance();
					parserType.setCommand(command);
					parserType.parse();
				} catch (InstantiationException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				scanner.close();
				return parserType;
				
			}
		}
		scanner.close();
		return null;
	}
	
}
