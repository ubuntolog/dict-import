package top.alexcloud;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

public class Import {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Import.class);

    private static final String optList[] = new String[] {"a", "d", "f", "s"};

    private static void usage(Options options) {
        new HelpFormatter().printHelp("Usage: dict-import [OPTIONS]", options);
        System.exit(1);
    }

    public static List<String> getDictFileList(String sourceFolder) {
        File folder = new File(sourceFolder);
        File[] listOfFiles = folder.listFiles();
        List<String> folderList = new ArrayList<>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                folderList.add(listOfFiles[i].getName());
            }
        }
        return folderList;
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(optList[0], true, "file extension for annotations");
        options.addOption(optList[1], true, "file extension for dictionaries");
        options.addOption(optList[2], true, "folder with the data");
        options.addOption(optList[3], true, "sqlite database name");

        CommandLine commandLine = null;
        try {
            commandLine = new GnuParser().parse(options, args);
        } catch (ParseException e) {
            log.error("You have provided incorrect parameters");
            log.error(e.getMessage());
            System.exit(1);
        }

        for (int i = 0; i < optList.length; i++) {
            if (!commandLine.hasOption(optList[i])) {
                usage(options);
            }
        }

        String annExtension = commandLine.getOptionValue(optList[0]);
        String dictExtension = commandLine.getOptionValue(optList[1]);
        String dataFolder = commandLine.getOptionValue(optList[2]);

        List<String> dataFiles = getDictFileList(dataFolder);

        Database db = new Database("test.db");
        try {
            db.createNewDatabase();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (String fileName: dataFiles) {
            String fileExtention = FilenameUtils.getExtension(Paths.get(dataFolder, fileName).toString());
            String fileBaseName = FilenameUtils.getBaseName(Paths.get(dataFolder, fileName).toString());
            System.out.println(fileExtention);
            if (fileExtention.toLowerCase().equals(dictExtension)) {
                System.out.println("Dict " + fileName);

                File annFile = new File(Paths.get(dataFolder, fileBaseName + "." + annExtension).toString());
                if (!annFile.exists()) {
                    log.info(fileBaseName + " does not have an annotation file, skipping this dictionary");
                    continue;
                }
            }
        }

    }
}
