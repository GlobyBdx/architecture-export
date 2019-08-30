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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClassList;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaPackage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ArchitectureJsonExport {
    @SuppressWarnings("unchecked")
    static private JSONArray exportAnnotationsAsJson(Set<JavaAnnotation> annotations) {
        JSONArray jsonAnnotations = new JSONArray();
        for (JavaAnnotation annotation : annotations) {
            jsonAnnotations.add(annotation.getRawType().getFullName());
        }
        return jsonAnnotations;
    }

    @SuppressWarnings("unchecked")
    static private JSONArray exportModifiersAsJson(Set<JavaModifier> modifiers) {
        JSONArray jsonModifiers = new JSONArray();
        for (JavaModifier modifier : modifiers) {
            jsonModifiers.add(modifier.name());
        }
        return jsonModifiers;
    }

    @SuppressWarnings("unchecked")
    static private JSONObject exportFieldsAsJson(Set<JavaField> fields, boolean minimal) {
        JSONObject jsonFields = new JSONObject();
        for (JavaField field : fields) {
            JSONObject jsonField = new JSONObject();
            jsonField.put("annotations", exportAnnotationsAsJson(field.getAnnotations()));
            if (!minimal) {
                jsonField.put("modifiers", exportModifiersAsJson(field.getModifiers()));
                jsonField.put("type", field.getRawType().getFullName());
            }
            jsonFields.put(field.getFullName(), jsonField);
        }
        return jsonFields;
    }

    @SuppressWarnings("unchecked")
    static private JSONArray exportParameterTypesAsJson(JavaClassList parameterTypes) {
        JSONArray jsonParameterTypes = new JSONArray();
        for (JavaClass parameterType : parameterTypes) {
            jsonParameterTypes.add(parameterType.getFullName());
        }
        return jsonParameterTypes;
    }

    @SuppressWarnings("unchecked")
    static private JSONObject exportMethodsAsJson(Set<JavaMethod> methods, boolean minimal) {
        JSONObject jsonMethods = new JSONObject();
        for (JavaMethod method : methods) {
            JSONObject jsonMethod = new JSONObject();
            jsonMethod.put("annotations", exportAnnotationsAsJson(method.getAnnotations()));
            if (!minimal) {
                jsonMethod.put("modifiers", exportModifiersAsJson(method.getModifiers()));
                jsonMethod.put("parameter_types", exportParameterTypesAsJson(method.getRawParameterTypes()));
                jsonMethod.put("return_type", method.getRawReturnType().getFullName());
            }
            jsonMethods.put(method.getFullName(), jsonMethod);
        }
        return jsonMethods;
    }

    @SuppressWarnings("unchecked")
    static private JSONObject exportClassesAsJson(Set<JavaClass> classes, boolean minimal) {
        JSONObject jsonClasses = new JSONObject();
        for (JavaClass exportedClass : classes) {
            JSONObject jsonClass = new JSONObject();
            jsonClass.put("annotations", exportAnnotationsAsJson(exportedClass.getAnnotations()));
            jsonClass.put("fields", exportFieldsAsJson(exportedClass.getFields(), minimal));
            jsonClass.put("methods", exportMethodsAsJson(exportedClass.getMethods(), minimal));
            if (!minimal) {
                jsonClass.put("modifiers", exportModifiersAsJson(exportedClass.getModifiers()));
            }
            jsonClasses.put(exportedClass.getFullName(), jsonClass);
        }
        return jsonClasses;
    }

    @SuppressWarnings("unchecked")
    static private JSONObject exportPackagesAsJson(Map<JavaPackage, Set<JavaAnnotation>> packages, boolean minimal) {
        JSONObject jsonPackages = new JSONObject();
        for (JavaPackage exportedPackage : packages.keySet()) {
            JSONObject jsonPackage = new JSONObject();
            jsonPackage.put("annotations", exportAnnotationsAsJson(packages.get(exportedPackage)));
            if (!minimal) {
                jsonPackage.put("classes", exportClassesAsJson(exportedPackage.getClasses(), minimal));
            }
            jsonPackages.put(exportedPackage.getName(), jsonPackage);
        }
        return jsonPackages;
    }

    static private void exportAsJson(File file, JSONObject jsonObject) {
        try {
            Writer writer = new FileWriter(file);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(jsonObject.toJSONString())));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    static public void exportArchitectureFromPackagesAsJson(String filename, Map<JavaPackage, Set<JavaAnnotation>> packages, boolean minimal) {
        JSONObject jsonPackagesArchitecture = new JSONObject();
        jsonPackagesArchitecture.put("packages", exportPackagesAsJson(packages, minimal));
        exportAsJson(new File(filename), jsonPackagesArchitecture);
    }

    @SuppressWarnings("unchecked")
    static public void exportArchitectureFromClassesAsJson(String filename, JavaClasses classes, boolean minimal) {
        JSONObject jsonClassesArchitecture = new JSONObject();
        jsonClassesArchitecture.put("classes", exportClassesAsJson(Sets.newHashSet(classes.toArray(new JavaClass[0])), minimal));
        exportAsJson(new File(filename), jsonClassesArchitecture);
    }
}
