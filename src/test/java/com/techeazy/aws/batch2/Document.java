package com.managedoc.model;

public class Document {

String documentName;

String docPath;

String type;

Boolean isPublic;

String accessibleBy; // Hidden

String LifeOfDoc;

public static void main(String[] args) {


Document doc = new Document();

doc.documentName = "MyDoc1";

doc.docPath = "/tmp";

System.out.println('Hello World ".toUpperCase());

System.out.println("Doc tiltle + doc.documentName);
}

}