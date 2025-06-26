package com.master.voice;

import java.io.*;
import java.util.zip.*;

public class Utils {

    public static void unpackZip(InputStream zipFileStream, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipFileStream));
        try {
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                }
                if (ze.isDirectory()) continue;
                FileOutputStream fout = new FileOutputStream(file);
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }
                fout.close();
            }
        } finally {
            zis.close();
        }
    }
}
