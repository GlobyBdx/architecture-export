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
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.google.common.collect.Sets;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClassList;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaPackage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ArchitectureXmlExport {
    static private Element createTextNodeElement(Document document, String name, String text) {
        Element element = document.createElement(name);
        Node node = document.createTextNode(text);
        element.appendChild(node);
        return element;
    }

    static private Element exportAnnotationsAsXml(Document document, Set<JavaAnnotation> annotations) {
        Element xmlAnnotations = document.createElement("annotations");
        for (JavaAnnotation annotation : annotations) {
            xmlAnnotations.appendChild(createTextNodeElement(document, "annotation", annotation.getRawType().getFullName()));
        }
        return xmlAnnotations;
    }

    static private Element exportModifiersAsXml(Document document, Set<JavaModifier> modifiers) {
        Element xmlModifiers = document.createElement("modifiers");
        for (JavaModifier modifier : modifiers) {
            xmlModifiers.appendChild(createTextNodeElement(document, "modifier", modifier.name()));
        }
        return xmlModifiers;
    }

    static private Element exportFieldsAsXml(Document document, Set<JavaField> fields, boolean minimal) {
        Element xmlFields = document.createElement("fields");
        for (JavaField field : fields) {
            Element xmlField = document.createElement("field");
            xmlField.appendChild(createTextNodeElement(document, "name", field.getFullName()));
            xmlField.appendChild(exportAnnotationsAsXml(document, field.getAnnotations()));
            if (!minimal) {
                xmlField.appendChild(exportModifiersAsXml(document, field.getModifiers()));
                xmlField.appendChild(createTextNodeElement(document, "type", field.getRawType().getFullName()));
            }
            xmlFields.appendChild(xmlField);
        }
        return xmlFields;
    }

    static private Element exportParameterTypesAsXml(Document document, JavaClassList parameterTypes) {
        Element xmlParameterTypes = document.createElement("parameter_types");
        for (JavaClass parameterType : parameterTypes) {
            xmlParameterTypes.appendChild(createTextNodeElement(document, "parameter_type", parameterType.getFullName()));
        }
        return xmlParameterTypes;
    }

    static private Element exportMethodsAsXml(Document document, Set<JavaMethod> methods, boolean minimal) {
        Element xmlMethods = document.createElement("methods");
        for (JavaMethod method : methods) {
            Element xmlMethod = document.createElement("method");
            xmlMethod.appendChild(createTextNodeElement(document, "name", method.getFullName()));
            xmlMethod.appendChild(exportAnnotationsAsXml(document, method.getAnnotations()));
            if (!minimal) {
                xmlMethod.appendChild(exportModifiersAsXml(document, method.getModifiers()));
                xmlMethod.appendChild(exportParameterTypesAsXml(document, method.getRawParameterTypes()));
                xmlMethod.appendChild(createTextNodeElement(document, "return_type", method.getRawReturnType().getFullName()));
            }
            xmlMethods.appendChild(xmlMethod);
        }
        return xmlMethods;
    }

    static private Element exportClassesAsXml(Document document, Set<JavaClass> classes, boolean minimal) {
        Element xmlClasses = document.createElement("classes");
        for (JavaClass exportedClass : classes) {
            Element xmlClass = document.createElement("class");
            xmlClass.appendChild(createTextNodeElement(document, "name", exportedClass.getFullName()));
            xmlClass.appendChild(exportAnnotationsAsXml(document, exportedClass.getAnnotations()));
            xmlClass.appendChild(exportFieldsAsXml(document, exportedClass.getFields(), minimal));
            xmlClass.appendChild(exportMethodsAsXml(document, exportedClass.getMethods(), minimal));
            if (!minimal) {
                xmlClass.appendChild(exportModifiersAsXml(document, exportedClass.getModifiers()));
            }
            xmlClasses.appendChild(xmlClass);
        }
        return xmlClasses;
    }

    static private Element exportPackagesAsXml(Document document, Map<JavaPackage, Set<JavaAnnotation>> packages, boolean minimal) {
        Element xmlPackages = document.createElement("packages");
        for (JavaPackage exportedPackage : packages.keySet()) {
            Element xmlPackage = document.createElement("package");
            xmlPackage.appendChild(createTextNodeElement(document, "name", exportedPackage.getName()));
            xmlPackage.appendChild(exportAnnotationsAsXml(document, packages.get(exportedPackage)));
            if (!minimal) {
                xmlPackage.appendChild(exportClassesAsXml(document, exportedPackage.getClasses(), minimal));
            }
            xmlPackages.appendChild(xmlPackage);
        }
        return xmlPackages;
    }

    static public void exportAsXml(File file, Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(file));
        } catch (TransformerException | TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }
    }

    static public void exportArchitectureFromPackagesAsXml(String filename, Map<JavaPackage, Set<JavaAnnotation>> packages, boolean minimal) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            document.appendChild(exportPackagesAsXml(document, packages, minimal));
            exportAsXml(new File(filename), document);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    static public void exportArchitectureFromClassesAsXml(String filename, JavaClasses classes, boolean minimal) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            document.appendChild(exportClassesAsXml(document, Sets.newHashSet(Arrays.copyOf(classes.toArray(), classes.size(), JavaClass[].class)), minimal));
            exportAsXml(new File(filename), document);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}
