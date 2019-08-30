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

package fr.ubordeaux.architecture.export.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public class ArchitectureExportUtils {
    final static public String PACKAGE_CLASS = "package-info";

    static public Map<JavaPackage, Set<JavaAnnotation>> getPackagesFromClasses(JavaClasses classes) {
        Map<JavaPackage, Set<JavaAnnotation>> annotations = new HashMap<>();
        for (JavaClass importedClass : classes) {
            if (!annotations.containsKey(importedClass.getPackage())) {
                annotations.put(importedClass.getPackage(), new HashSet<>());
                try {
                    JavaClass packageClass = importedClass.getPackage().getClassWithSimpleName(PACKAGE_CLASS);
                    for (JavaAnnotation annotation : packageClass.getAnnotations()) {
                        annotations.get(importedClass.getPackage()).add(annotation);
                    }
                } catch (IllegalArgumentException e) {}
            }
        }
        return annotations;
    }

    static public void exportArchitecture(String path, String filename, boolean minimal, boolean fromPackages, boolean asXml) {
        JavaClasses classes = new ClassFileImporter().importPath(path);
        if (!fromPackages) {
            if (!asXml) {
                ArchitectureJsonExport.exportArchitectureFromClassesAsJson(filename, classes, minimal);
            }
            else {
                ArchitectureXmlExport.exportArchitectureFromClassesAsXml(filename, classes, minimal);
            }
        }
        else {
            if (!asXml) {
                ArchitectureJsonExport.exportArchitectureFromPackagesAsJson(filename, getPackagesFromClasses(classes), minimal);
            }
            else {
                ArchitectureXmlExport.exportArchitectureFromPackagesAsXml(filename, getPackagesFromClasses(classes), minimal);
            }
        }
    }
}
