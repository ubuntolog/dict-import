package top.alexcloud;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
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

    public static String readAnnotation(String fileName) throws IOException {
        String content;
        BufferedReader br = null;
        try {
            String line = null;
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), StandardCharsets.UTF_16));
            while ((line = br.readLine()) != null) {
                sb.append(line);

                sb.append(System.lineSeparator());
            }
            content = sb.toString();

        } finally {
            if (br != null) {
                br.close();
            }
        }
        return content;
    }

    public static void importDictionary(String fileName, String description, String dbFileName, Database db) throws IOException {
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), StandardCharsets.UTF_16));
            Integer counter = 0;
            int lastId = 0;
            String currentWord = "";
            String currentMeaning = "";
            String currentTextContent = "";

            String dictName = "";
            String srcLanguage = "";
            String targetLanguage = "";

            while ((line = br.readLine()) != null) {
                counter++;

                if (counter.equals(1)) {
                    dictName = line;
                    dictName = dictName.replaceAll("(?i)#NAME", "");
                    dictName = dictName.trim();
                    dictName = dictName.substring(1, dictName.length()-1);
                    log.info("Name: " + dictName);
                }

                if (counter.equals(2)) {
                    srcLanguage = line;
                    srcLanguage = srcLanguage.replaceAll("(?i)#INDEX_LANGUAGE", "");
                    srcLanguage = srcLanguage.trim();
                    srcLanguage = srcLanguage.substring(1, srcLanguage.length()-1);
                    log.info("Source language: " + srcLanguage);
                }

                if (counter.equals(3)) {
                    targetLanguage = line;
                    targetLanguage = targetLanguage.replaceAll("(?i)#CONTENTS_LANGUAGE", "");
                    targetLanguage = targetLanguage.trim();
                    targetLanguage = targetLanguage.substring(1, targetLanguage.length()-1);
                    log.info("Target language: " + targetLanguage);

                    db.insertDictionary(dictName, description, srcLanguage, targetLanguage);
                    lastId = db.getDictLastId();
                }

                if (counter > 3) {

                    if (Character.isWhitespace(line.charAt(0))) {
                        line = line.trim();
                        currentMeaning = currentMeaning + "\n" + line;
                        currentTextContent = currentMeaning.replaceAll("\\[.*?\\]", "").trim();
                    } else {
                        if ((currentWord.length()>0) && (currentMeaning.length()>0)) {
                            db.insertEntry(lastId, currentWord, currentMeaning, currentTextContent);
                            currentWord = "";
                            currentMeaning = "";
                            currentTextContent = "";
                        }
                        currentWord = line;
                    }
                }
            }
            if ((currentWord.length()>0) && (currentMeaning.length()>0)) {
                db.insertEntry(lastId, currentWord, currentMeaning, currentTextContent);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
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
        String dbFileName = commandLine.getOptionValue(optList[3]);

        Database db = new Database(dbFileName);
        db.createTable( "CREATE TABLE IF NOT EXISTS dictionary (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name varchar(50) NOT NULL,\n"
                + "	description text NOT NULL,\n"
                + "	src_lang varchar(50) NOT NULL,\n"
                + "	target_lang varchar(50) NOT NULL\n"
                + ");");

        db.createTable( "CREATE TABLE IF NOT EXISTS entry (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	dictionary_id integer NOT NULL,\n"
                + "	word varchar(100) NOT NULL,\n"
                + "	meaning text NOT NULL,\n"
                + "	text_content text NOT NULL\n"
                + ");");

        List<String> dataFiles = getDictFileList(dataFolder);
        int dictCounter = 0;
        for (String fileName: dataFiles) {
            dictCounter++;
            log.info("File " + dictCounter + " out of " + dataFiles.size());
            String fileExtention = FilenameUtils.getExtension(Paths.get(dataFolder, fileName).toString());
            String fileBaseName = FilenameUtils.getBaseName(Paths.get(dataFolder, fileName).toString());

            if (fileExtention.toLowerCase().equals(dictExtension)) {
                log.info("Dictionary detected: " + fileName);

                // Skip dictionaries with any description
                String annotationPath = Paths.get(dataFolder, fileBaseName + "." + annExtension).toString();
                File annFile = new File(annotationPath);
                if (!annFile.exists()) {
                    log.info(fileBaseName + " does not have any annotation file, skipping this dictionary");
                    continue;
                }

                String annotationContent = "";
                try {
                    annotationContent = readAnnotation(annotationPath);
                } catch (IOException e) {
                    log.error(e.getMessage());
                }

                try {
                    importDictionary(Paths.get(dataFolder, fileName).toString(), annotationContent, dbFileName, db);
                    log.info("A new dictionary has been imported");
                } catch (IOException e) {
                    log.error(e.getMessage());
                }

            }
        }

    }
}
