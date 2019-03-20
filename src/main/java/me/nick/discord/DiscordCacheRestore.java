package me.nick.discord;

import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Created by Nick on 2019-03-20 15:59.
 */

public class DiscordCacheRestore {

    // To lazy to make a GUI so enjoy this... Maybe soon ¯\_(ツ)_/¯

    // Your discord cache direction.
    // 0 = Discord
    // 1 = Discord Canary
    // 2 = cacheDir (Custom location to the cache direction)
    private final int discordOption = 1;

    //You can only set the location this when "discordOption" is set to 2
    private File cacheDir = new File("C:\\Users\\Gebruiker\\AppData\\Roaming\\discordcanary\\Cache");

    // The location where we can store the cache (Document must exist otherwise it will not work)
    private File displayDir = new File("D:\\DiscordCache");

    // You may be want to set repair to true if you did something like this https://www.youtube.com/watch?v=Pst8SorCPKo
    // You may ask why? Well the command (ren * *.png) renamed every file in the cache direction to (name).png
    // This means that discord cannot find the original file and it will re-download the image again since the one without .png doesn't exist anymore and that means you have the image 2x (The one with .png and another one without .png) unless you don't see the image/person/emote/... again.
    // This will delete the useless file (The one with an file extension)
    private final boolean repair = true;

    private final Logger logger = Logger.getLogger("DiscordCacheRestore");

    public static void main(String[] args) {
        DiscordCacheRestore discordCacheRestore = new DiscordCacheRestore();
        discordCacheRestore.load();
        discordCacheRestore.start();
    }

    private void load() {
        switch (discordOption) {
            case 0:
                cacheDir = new File(System.getProperty("user.home") + "\\discord\\Cache");
                break;
            case 1:
                cacheDir = new File(System.getProperty("user.home") + "\\discordcanary\\Cache");
                break;
        }
    }

    private void start() {
        if (cacheDir == null || displayDir == null || !cacheDir.isDirectory() || !displayDir.isDirectory()) return;

        for (File file : Objects.requireNonNull(cacheDir.listFiles())) {
            if (file == null || file.isDirectory() || file.getName().contains("index") || file.getName().contains("data"))
                continue;
            Tika tika = new Tika();
            try {
                if (tika.detect(file) == null) continue;
                String fileType = tika.detect(file);
                File newFile = new File(displayDir.getPath() + "\\" + file.getName().split("\\.")[0] + "." + fileType.split("/")[1]);
                if (newFile.exists()) {
                    logger.info(newFile.getPath() + " Already exist.");
                    continue;
                }
                Files.copy(file.toPath(), newFile.toPath());
                logger.info("\nName: " + file.getName() + " - Type: " + fileType + " - Path: " + file.getPath() + "\n" + file.getName() + " " + ((newFile.exists()) ? "Successfully" : "Unsuccessfully") + " moved to " + newFile.getName() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (repair) {
            // Doing the for loop again cuz I'm lazy
            for (File file : Objects.requireNonNull(cacheDir.listFiles())) {
                if (file == null || file.isDirectory() || file.getName().contains("index") || file.getName().contains("data"))
                    continue;
                redoFile(file);
            }
        }
    }

    /**
     * this method removes the extension type the file and if it already exist it will remove the file
     * It will replace the file to its original and when it already exist it will delete the file
     *
     * @param discordCacheFile image from the discord cache folder
     */
    private void redoFile(File discordCacheFile) {
        if (discordCacheFile.isFile() && discordCacheFile.getName().contains(".")) {
            File repairedFile = new File(discordCacheFile.getPath().split("\\.")[0]);
            if (repairedFile.exists()) {
                boolean deleted = discordCacheFile.delete();
                if (deleted) {
                    logger.info(repairedFile.getPath() + " Has been deleted! (Already exist)");
                } else {
                    logger.warning(repairedFile.getPath() + " Cannot be deleted.");
                }
            } else {
                boolean renameFile = discordCacheFile.renameTo(repairedFile);
                if (renameFile) {
                    logger.info(repairedFile.getPath() + " Has been renamed successfully!");
                } else {
                    logger.warning(repairedFile.getPath() + " Has been renamed Unsuccessfully.");
                }
            }
        }
    }
}
