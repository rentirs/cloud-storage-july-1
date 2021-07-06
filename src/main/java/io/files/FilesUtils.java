package io.files;

import java.io.File;
import java.time.Instant;

public class FilesUtils {
    public static void main(String[] args) {
        File file = new File("img.png");
        System.out.println(file.exists());
        Instant instant = Instant.ofEpochMilli(file.lastModified());
        String t = instant.toString();
        System.out.println(file.getName() + "\t" + file.length() / 1000 + "Mb\t" + t);
        File copy = new File("copy.png");
        System.out.println(copy.exists());
        System.out.println(copy.getName());
        System.out.println(copy.listFiles());
        File dir = new File("Folder");
        dir.deleteOnExit();

        /*byte[] buffer = new byte[256];
        try (FileInputStream is = new FileInputStream(file);
             FileOutputStream os = new FileOutputStream(copy)) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
