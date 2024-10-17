package com.dhl.tools.fortify.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    public static void extractZipFile(String zipFilePath, String destDirectory) throws IOException {
        File dir = new File(destDirectory);
        // Create destination directory if it doesn't exist
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        try (InputStream is = new FileInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(is)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(destDirectory, entry.getName());
                
                if (entry.isDirectory()) {
                    // Create directory if entry is a directory
                    file.mkdirs();
                } else {
                    // Create directories if they do not exist
                    file.getParentFile().mkdirs();
                    
                    // Write file contents
                    try (OutputStream os = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
    
	public static void deleteFilesAndFolders() {
		String[] filesToDelete = { "attachments.xml", "audit.fvdl", "audit.fvdl.mac", "audit.properties", "audit.xml",
				"filtertemplate.xml", "metatable", "VERSION", "output.xml", "audit.fvdl.old", "audit.fvdl.old.mac",
				"iidmigration.properties" };
		String[] foldersToDelete = { "ExternalMetadata", "src-archive", "src-xrefdata" };

		for (String file : filesToDelete) {
			try {
				Files.deleteIfExists(Paths.get(file));
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}

		for (String folder : foldersToDelete) {
			try {
				Files.walk(Paths.get(folder)).sorted((a, b) -> b.compareTo(a)).map(java.nio.file.Path::toFile)
						.forEach(File::delete);
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}

	public static void deleteFilesAndFolders(String workDir) {
		try {
			Files.walk(Paths.get(workDir)).sorted((a, b) -> b.compareTo(a)).map(java.nio.file.Path::toFile)
					.forEach(File::delete);
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	public static void main(String[] args) {
		deleteFilesAndFolders("__del__");
	}
}