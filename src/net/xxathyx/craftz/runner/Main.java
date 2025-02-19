package net.xxathyx.craftz.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.xxathyx.craftz.runner.logging.Logger;
import net.xxathyx.craftz.runner.updater.Updater;

public class Main {
	
	private static final Logger logger = new Logger(new File(Updater.getLogsFolder(), (Updater.getLogsFolder().listFiles() != null ? (Updater.getLogsFolder().listFiles().length-1) : "0") + ".log"));
	
    private static long failed = 0L;
    private static int libraries = 9;
    
	public static void main(String[] args) {
		
		try {
			Updater updater = new Updater();
			updater.createDirectory();
			
			File pidFile = updater.getPidFile();
			
			if(pidFile.exists()) {
				Scanner in = new Scanner(new FileReader(pidFile));
				
				StringBuilder stringBuilder = new StringBuilder();
				String out;
				
				while(in.hasNext()) stringBuilder.append(in.next());
				in.close();
				out = stringBuilder.toString();
							
		        String os = System.getProperty("os.name").toLowerCase();
		        
	            ProcessBuilder processBuilder;
	            
	            if(os.contains("win")) {
	                processBuilder = new ProcessBuilder("taskkill", "/PID", out, "/F");
	            }else processBuilder = new ProcessBuilder("kill", "-9", out);

		        processBuilder.start();
			}
			
			if(!pidFile.exists()) {
				pidFile.getParentFile().mkdirs();
				pidFile.createNewFile();
			}
			
		    FileWriter writer = new FileWriter(pidFile);
		    writer.write(String.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]));
		    writer.close();
			
			URI uri = Main.class.getResource("resources/javafx-sdk.zip").toURI();
			File sdkZip = new File(Updater.getGameFolder() + "/javafx-sdk.zip");
			
			if(updater.getLibrariesFolder().listFiles().length<libraries) {
				if("jar".equals(uri.getScheme())) {
				    for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
				        if (provider.getScheme().equalsIgnoreCase("jar")) {
				            try {
				                provider.getFileSystem(uri);
				            }catch (FileSystemNotFoundException e) {
				                provider.newFileSystem(uri, Collections.emptyMap());
				            }
				        }
				    }
				}
				Path source = Paths.get(uri);		
				Files.copy(source, sdkZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
		        File dest = updater.getUpdaterFolder();

		        byte[] buffer = new byte[1024];
		        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sdkZip))) {
					ZipEntry zipEntry = zis.getNextEntry();
					
					while (zipEntry != null) {
					    File newFile = newFile(dest, zipEntry);
					    if (zipEntry.isDirectory()) {
					        if (!newFile.isDirectory() && !newFile.mkdirs()) {
					            throw new IOException("Failed to create directory " + newFile);
					        }
					    }else {
					        File parent = newFile.getParentFile();
					        if (!parent.isDirectory() && !parent.mkdirs()) {
					            throw new IOException("Failed to create directory " + parent);
					        }
					        
					        FileOutputStream fos = new FileOutputStream(newFile);
					        int len;
					        while ((len = zis.read(buffer)) > 0) {
					            fos.write(buffer, 0, len);
					        }
					        fos.close();
					    }
					    zipEntry = zis.getNextEntry();
					}
					zis.closeEntry();
					zis.close();
				}
		        sdkZip.delete();
			}
			
	        if(!updater.isUpdated()) {
				updater.download();
			}else {
				updater.launch();
			}
			
		}catch (IOException | URISyntaxException e) {
			
			e.printStackTrace();
			logger.log(e.toString());
			
			
			Timer timer = new Timer();
		    timer.schedule(new TimerTask() {
		    	
				@Override
				public void run() {
					failed=failed+5000L;
					if(failed>30000L)failed=5000L;
					main(null);
					JOptionPane.showMessageDialog(new JFrame(), "Next try in " + failed/1000L + " seconds\n"
							+ "\n1. Please check you internet connection\n"
							+ "2. Check our download server status\n"
							+ "\nMore help on https://craftz.net/support", "Failed installation", JOptionPane.ERROR_MESSAGE);
				}		    	
		    }, failed);
		}
	}
	
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		
	    File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if(!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }
	    return destFile;
	}
}