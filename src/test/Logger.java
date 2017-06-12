package test;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
//	static String logPath = "H:/contarctFile/logs/contract.log";
//	static String logPath = "H:/contarctFile/logs/20170525收入合同全部附件.log";
//	static String logPath = "H:/contarctFile/logs/2017052506支出合同.log";
	static String logPath = "H:/contarctFile/logs/othercontract.log";
	public static void log(String info, int level) {
		if (level == 1) {
			log(info);
		} else {
			
		}
	}
	public static void log(String info) {
		FileWriter fw;
		try {
			fw = new FileWriter(logPath, true);
			System.out.println(info);
			fw.write(info); 
			fw.write("\n\r\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	public static void logStackTrace(StackTraceElement[] stackTraceElements) {
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			log(stackTraceElement.toString());
		}
		
	}
}
