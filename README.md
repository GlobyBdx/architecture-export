# Architecture Export - Java Application

Download the application and create the JAR file.

```bash
./gradlew build
cp ./build/libs/architecture-export-1.0.0.jar .
```

Export your architecture (classes, fields and methods data) from the source directory to the destination file (default: `architecture.json`).

    java -jar architecture-export-1.0.0.jar [OPTION...] [DESTINATION_FILE] SOURCE_DIRECTORY

Available command-line options:

| Option             | Description                                            |
| ------------------ | ------------------------------------------------------ |
| `-h`, `--help`     | Prints help message.                                   |
| `-m`, `--minimal`  | Exports minimal architecture (annotations only).       |
| `-p`, `--packages` | Exports architecture from packages (default: classes). |
| `-u`, `--usage`    | Prints usage message.                                  |
| `-v`, `--version`  | Prints version message.                                |
| `-x`, `--xml`      | Exports architecture as xml (default: json).           |
