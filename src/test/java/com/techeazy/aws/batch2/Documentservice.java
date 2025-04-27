package com.managedoc.service;

import java.io.IOException;

import java.nio.file.Files:

import java.nio.file.Path;

import java.nio.file.Paths;

import java.nio.file.StandardCopyOption;

I public static void main(String[] args) {

if (args.length != 2) {

System.out.println("Usage: java UserFileSaver <username> <file_path>");

return;

public class DocumentService {

}

String username = args[0];

String filePath = args[1];

Path sourcePath = Paths.get(filePath); {

if (!Files.exists (sourcePath))

System.err.println("File does not exist: filePath);

return;

Path userDir Paths.get("/tmp", username);

try {

if (!Files.exists(userDir)) {

}

Files.createDirectories(userDir);

System.out.println("Created directory: " + userDir);

Path targetPath = userDir.resolve(sourcePath.getFileName());

Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

System.out.println("File copied to: "+targetPath);

} catch (IOException e) {

System.err.println("Error: "+e.getMessage());

}

} 
}