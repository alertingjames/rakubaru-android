package com.app.rakubaru.classes;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonFile {
    File data = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
    File file = new File(data, "traces.txt");

    private String content = "";

    public String readFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                content = br.readLine();
                br.close();
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            e.printStackTrace();
        }
        return content;
    }

    public void writeToFile(String content) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile() throws IOException {
        if(!file.exists()){
            file.createNewFile();
        }else{
            System.out.println("File already exists");
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}



























