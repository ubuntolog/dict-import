# dict-import
A simple command line tool to import dictionaries to a SQLite database.

## Build
To create a JAR file:
~~~~
mvn clean package
~~~~

## Usage
~~~~
java -jar JAR_FILE -a ANNOTATION_EXTENTION -d DICTIONARY_EXTENSION -f DATA_FOLDER -s DATABASE_FILE
~~~~