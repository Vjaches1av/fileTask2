import java.io.*;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Main {
    private static int saveCounter = 0;

    private static void saveGame(GameProgress obj, File savePath) {
        final File file = new File(savePath, "save" + ++saveCounter + ".dat");

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file, false))) {
            out.writeObject(obj);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения файла " + file.getName());
        }
    }

    private static void zipFiles(File savePath, File... files) {
        if (files != null && files.length != 0) {
            File zipFile = new File(savePath, "archive.zip");
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile, false), Charset.forName("cp866"));
                 BufferedOutputStream fileOut = new BufferedOutputStream(zipOut)) {
                for (File f : files) {
                    if (f.isFile()) {
                        int data;
                        byte[] b = new byte[128];
                        try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(f))) {
                            zipOut.putNextEntry(new ZipEntry(f.getName()));
                            while ((data = fileIn.read(b)) != -1)
                                fileOut.write(b, 0, data);
                            fileOut.flush();
                            zipOut.closeEntry();
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Ошибка записи файлов в формат zip");
                deleteFiles(zipFile);
            }
        }
    }

    private static void deleteFiles(File... files) {
        if (files != null && files.length != 0) {
            for (File f : files) {
                if (f.exists() && !f.delete()) {
                    System.err.println("Ошибка при удалении файла " + f.getName());
                }
            }
        }
    }

    public static void main(String[] args) {
        final GameProgress[] progress = {
                new GameProgress(100, 30, 1, 0),
                new GameProgress(50, 15, 5, 150),
                new GameProgress(1, 1, 10, 300)
        };

        try (final Scanner scanner = new Scanner((System.in))) {
            System.out.print("Укажите путь, куда сохранять файлы: ");
            final File savePath = new File(scanner.nextLine());
            if ((savePath.exists() || savePath.mkdirs()) && savePath.isDirectory()) {
                for (GameProgress gameProgress : progress) {
                    saveGame(gameProgress, savePath);
                }
                FilenameFilter filter = (dir, name) -> name.endsWith(".dat");
                zipFiles(savePath, savePath.listFiles(filter));
                deleteFiles(savePath.listFiles(filter));
            }
        }
    }
}