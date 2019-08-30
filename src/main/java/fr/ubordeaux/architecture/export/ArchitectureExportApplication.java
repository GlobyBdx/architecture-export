/*
 * Copyright 2019 Benoit Faget. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * @author Benoit Faget
 * @version 1.0.0
 */

package fr.ubordeaux.architecture.export;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import fr.ubordeaux.architecture.export.utils.ArchitectureExportUtils;

public class ArchitectureExportApplication {
    final static public String APPLICATION_NAME = "architecture-export";
    final static public String APPLICATION_VERSION = "1.0.0";

    final static public String DEFAULT_FILENAME = "architecture";
    final static public String JSON_EXTENSION = ".json";
    final static public String XML_EXTENSION = ".xml";

    static private int printVersion() {
        System.out.println("Version: " + APPLICATION_VERSION);
        return 0;
    }

    static private int printUsage() {
        System.out.println("Usage: java -jar " + APPLICATION_NAME + "-" + APPLICATION_VERSION + ".jar [OPTION...] [DESTINATION_FILE] SOURCE_DIRECTORY");
        return 0;
    }

    static private int printDescription() {
        System.out.println("Exports architecture (packages, classes, fields and methods metadata) from source directory to destination file (default: 'architecture.json').");
        return 0;
    }

    static private int printHelp() {
        printUsage();
        printDescription();
        System.out.println("Available command-line options:");
        System.out.println("-h, --help      Prints help message.");
        System.out.println("-m, --minimal   Exports minimal architecture (names and annotations only).");
        System.out.println("-p, --packages  Exports architecture from packages (default: classes).");
        System.out.println("-u, --usage     Prints usage message.");
        System.out.println("-v, --version   Prints version message.");
        System.out.println("-x, --xml       Exports architecture as xml (default: json).");
        return 0;
    }

    static private int printError(String option, String path) {
        if (option != null) {
            System.out.println("ERROR: Unknown command-line option '" + option + "'.");
        }
        if (path != null) {
            System.out.println("ERROR: Not found source directory '" + path + "'.");
        }
        printHelp();
        return 1;
    }

    static private void askConfirmation(String filename) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("WARNING: Already existing destination file '" + filename + "'; overwrite? (y/n)");
            String input = scanner.nextLine().toLowerCase();
            if (input.compareTo("y") == 0 || input.compareTo("yes") == 0) {
                break;
            }
            if (input.compareTo("n") == 0 || input.compareTo("no") == 0) {
                scanner.close();
                System.exit(0);
            }
        }
        scanner.close();
    }

    static public void main(String[] arguments) {
        if (arguments.length == 0) {
            System.exit(printError(null, null));
        }
        int length = arguments.length;
        for (int index = 0; index < length; ++index) {
            if (arguments[index].compareTo("-h") == 0 || arguments[index].compareTo("--help") == 0) {
                System.exit(printHelp());
            }
            if (arguments[index].compareTo("-u") == 0 || arguments[index].compareTo("--usage") == 0) {
                System.exit(printUsage());
            }
            if (arguments[index].compareTo("-v") == 0 || arguments[index].compareTo("--version") == 0) {
                System.exit(printVersion());
            }
        }
        String path = arguments[--length];
        String filename = (length > 0 && !arguments[length - 1].startsWith("-")) ? arguments[--length] : DEFAULT_FILENAME;
        boolean minimal = false, fromPackages = false, asXml = false;
        for (int index = 0; index < length; ++index) {
            if (arguments[index].compareTo("-m") == 0 || arguments[index].compareTo("--minimal") == 0) {
                minimal = true;
            }
            else if (arguments[index].compareTo("-p") == 0 || arguments[index].compareTo("--packages") == 0) {
                fromPackages = true;
            }
            else if (arguments[index].compareTo("-x") == 0 || arguments[index].compareTo("--xml") == 0) {
                asXml = true;
            }
            else {
                System.exit(printError(arguments[index], null));
            }
        }
        filename += (!asXml) ? JSON_EXTENSION : XML_EXTENSION;
        if (!Files.isDirectory(Paths.get(path))) {
            System.exit(printError(null, path));
        }
        if (Files.isRegularFile(Paths.get(filename))) {
            askConfirmation(filename);
        }
        ArchitectureExportUtils.exportArchitecture(path, filename, minimal, fromPackages, asXml);
    }
}
