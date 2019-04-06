package top.alexcloud;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.*;

public class Import {

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
                String curFileName = listOfFiles[i].getName();
                if (curFileName.endsWith("conllu") == true) {
                    folderList.add(listOfFiles[i].getName());
                }
            }
        }
        return folderList;
    }

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("a", "annotation", false, "file extension for annotations");
        options.addOption("d", "dictionary", false, "file extension for dictionaries");
        options.addOption("f", "folder", false, "folder with dictionaries and annotations");

        CommandLine commandLine = new GnuParser().parse(options, args);

        if (commandLine.getArgs().length < 3) {
            usage(options);
        }
    }
}
