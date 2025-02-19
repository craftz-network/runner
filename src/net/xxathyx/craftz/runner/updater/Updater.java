package net.xxathyx.craftz.runner.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;

import net.xxathyx.craftz.runner.logging.Logger;

public class Updater {
	
	private static final String operatingSystem = java.lang.System.getProperty("os.name").toLowerCase();
	
    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS, OTHER;
    };
	
	private static final Logger logger = new Logger(new File(Updater.getLogsFolder(), (Updater.getLogsFolder().listFiles() != null ? (Updater.getLogsFolder().listFiles().length-1) : "0") + ".log"));
    
	private static String fs = File.separator;
	public long onlineLength = 0;
	
	public Updater() throws IOException {
        URL url = new URL("https://craftz.net/download/bootstrap.jar");
        URLConnection urlConnection = url.openConnection();
        onlineLength = urlConnection.getContentLengthLong();
	}
	
	public static File getGameFolder() {
		return new File((getOS().equals(OS.WINDOWS) ? System.getenv("APPDATA") : System.getProperty("user.home")) + fs + ".craftz" + fs);
	}
	
	public File getUpdaterFolder() {
		return new File(getGameFolder() + fs + "updater" + fs);
	}
	
	public File getPidFile() {
		return new File(getUpdaterFolder(), "pid.txt");
	}
	
	public static File getLogsFolder() {
		return new File(getGameFolder() + fs + "updater" + fs + "logs" + fs);
	}
	
	public File getBootstrapFile() {
		return new File(getUpdaterFolder() + fs + "bootstrap.jar");
	}
	
	public File getLibrariesFolder() {
		return new File(getGameFolder() + fs + "updater" + fs + "lib" + fs);
	}
	
	public void createDirectory() {
		File gameFolder = getGameFolder();
		if(!gameFolder.exists()) gameFolder.mkdir();
		
		File updaterFolder = new File(gameFolder + fs + "updater" + fs);
		if(!updaterFolder.exists()) updaterFolder.mkdir();
		
		File logsFolder = getLogsFolder();
		if(!logsFolder.exists()) logsFolder.mkdir();
		
		File libFolder = getLibrariesFolder();
		if(!libFolder.exists()) libFolder.mkdir();
	}
	
	public boolean isUpdated() throws IOException {
		
		File bootstrapFile = getBootstrapFile();
		if(!bootstrapFile.exists()) return false;
        
		if(onlineLength == bootstrapFile.length()) return true;
        
		return false;
	}
	
	public void download() throws IOException {
	    
	    String archive = "https://craftz.net/download/bootstrap.jar";  
	    File jar = getBootstrapFile();
	    
	    URL url = new URL(archive);  
	    URLConnection connection = url.openConnection();  
	    InputStream inputStream = connection.getInputStream();  
	    FileOutputStream outputStream = new FileOutputStream(jar);
	    
	    final byte[] bt = new byte[1024];
	    int len;
	    while((len = inputStream.read(bt)) != -1) {
	    	outputStream.write(bt, 0, len);
	    }
	    outputStream.close();
	    logger.log("Bootstrap updated");
	    launch();
	}
	
	public String getLibPath(File file) {
		String separator = "";
		if(System.getProperty("os.name").contains("Windows")) separator = "\"";
		return separator + file.getPath() + separator;
	}
	
	public void launch() throws IOException {
		
		Process process;
		
		if(Double.parseDouble(System.getProperty("java.class.version")) <= 52) {
		    logger.log("Launching bootstrap using " + "java " + "-jar " + getBootstrapFile().getPath());
			process = new ProcessBuilder(
					"java",
					"-jar",
					getBootstrapFile().getPath()
			).start();
		}else {
		    logger.log("Launching bootstrap using " + "java " + "--module-path " + getLibPath(getLibrariesFolder()) + " --add-modules" + " javafx.controls,javafx.fxml" + " -jar " + getBootstrapFile().getPath());
			process = new ProcessBuilder(
					"java",
					"--module-path",
					getLibPath(getLibrariesFolder()),
					"--add-modules",
					"javafx.controls,javafx.fxml",
					"-jar",
					getBootstrapFile().getPath()
			).start();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		logger.log(builder.toString());
		System.exit(0);
	}
	
	public static String time() {
		LocalDateTime date  = LocalDateTime.now();
		return "["+date.getDayOfMonth()+"/"+date.getMonthValue()+"/"+date.getYear()+" "+date.getHour()+":"+date.getMinute()+":"+date.getSecond()+"] ";
	}
    
    public static OS getOS() {
    	        
        if(operatingSystem.contains("win")) { return OS.WINDOWS;
        }else if (operatingSystem.contains("nix") || operatingSystem.contains("nux") || operatingSystem.contains("aix")) {return OS.LINUX;
        }else if (operatingSystem.contains("mac")) {return OS.MAC;
        }else if (operatingSystem.contains("sunos")) {return OS.SOLARIS;}
        return OS.OTHER;
    }
}