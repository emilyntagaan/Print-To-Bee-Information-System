// package com.println.config;

// import java.io.File;
// import java.io.FileInputStream;
// import java.io.IOException;
// import java.util.Properties;

// public class DBConfig {

//     public static Properties load() {
//         Properties props = new Properties();

//         // Try relative path first
//         String relativePath = "config/dbconfig.properties";
//         File file = new File(relativePath);

//         // If file does NOT exist, try parent paths (VSCode run location issue)
//         if (!file.exists()) {
//             // Try one folder up
//             file = new File("../config/dbconfig.properties");

//             // Try two folders up
//             if (!file.exists()) {
//                 file = new File("../../config/dbconfig.properties");
//             }
//         }

//         // Final check
//         if (!file.exists()) {
//             System.err.println("ERROR: Config file not found.");
//             System.err.println("Looked for: " + file.getAbsolutePath());
//             return props; // empty
//         }

//         try (FileInputStream fis = new FileInputStream(file)) {
//             props.load(fis);
//         } catch (IOException e) {
//             e.printStackTrace();
//         }

//         return props;
//     }
// }
