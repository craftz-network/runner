package net.xxathyx.craftz.runner.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {
	
	private File file;
	
	public Logger(File file) {
		this.file = file;
	}
	
	public void log(String data) {
	    try {
			if(file.exists()) file.createNewFile();
		    FileWriter writer = new FileWriter(file, true);
			writer.write(time()+": "+data+"\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public String time() {
		LocalDateTime date  = LocalDateTime.now();
		return "["+date.getDayOfMonth()+"/"+date.getMonthValue()+"/"+date.getYear()+" "+date.getHour()+":"+date.getMinute()+":"+date.getSecond()+"]";
	}
}