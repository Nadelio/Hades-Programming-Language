package src.util;

import java.io.File;

public class HadesFileStream {
    private String path;
    private boolean mode;
    private File raw;
    private String content;

    public HadesFileStream(String path){
        this.path = path;
        this.mode = false; // default mode is read
        this.raw = new File(path);
        this.content = this.initializeFileContent();
    }

    public HadesFileStream(String path, boolean mode) {
        this.path = path;
        this.mode = mode;
        this.raw = new File(path);
        this.content = this.initializeFileContent();
    }

    public HadesFileStream(File raw) {
        this.path = raw.getPath();
        this.mode = false; // default mode is read
        this.raw = raw;
        this.content = this.initializeFileContent();
    }

    public HadesFileStream(File raw, boolean mode) {
        this.path = raw.getPath();
        this.mode = mode;
        this.raw = raw;
        this.content = this.initializeFileContent();
    }

    private String initializeFileContent() {
        String content = "";
        try (java.util.Scanner sc = new java.util.Scanner(this.raw)) {
            while (sc.hasNextLine()) { content += sc.nextLine() + " "; }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("\u001B[31mGiven file not found.\u001B[0m");
            System.exit(1);
        }
        return content;
    }

    public void writeContentToFile(String content) {
        try (java.io.FileWriter writer = new java.io.FileWriter(this.raw)) {
            writer.write(content);
        } catch (java.io.IOException e) {
            System.out.println("\u001B[31mError writing to file.\u001B[0m");
            System.exit(1);
        }
    }

    public void appendContentToFile(String content) {
        try (java.io.FileWriter writer = new java.io.FileWriter(this.raw, true)) {
            writer.write(content);
        } catch (java.io.IOException e) {
            System.out.println("\u001B[31mError writing to file.\u001B[0m");
            System.exit(1);
        }
    }

    public void writeCharAtPosition(int position, char character) {
        String newContent = this.content.substring(0, position) + character + this.content.substring(position + 1);
        this.writeContentToFile(newContent);
        this.content = newContent;
    }

    public void insertCharAtPosition(int position, char character){
        String newContent = this.content.substring(0, position) + character + this.content.substring(position);
        this.writeContentToFile(newContent);
        this.content = newContent;
    }

    public void deleteCharAtPosition(int position) {
        String newContent = this.content.substring(0, position) + this.content.substring(position + 1);
        this.writeContentToFile(newContent);
        this.content = newContent;
    }

    public void deleteLastChar(){
        String newContent = this.content.substring(0, this.content.length() - 1);
        this.writeContentToFile(newContent);
        this.content = newContent;
    }

    public void writeNumberAtPosition(int position, int number) {
        String newContent = this.content.substring(0, position) + number + this.content.substring(position + 1);
        this.writeContentToFile(newContent);
        this.content = newContent;
    }

    public void insertNumberAtPosition(int position, int number){
        String newContent = this.content.substring(0, position) + number + this.content.substring(position);
        this.writeContentToFile(newContent);
        this.content = newContent;
    }

    public char readCharAtPosition(int position){ return this.content.charAt(position); }

    /**
     * @return contents of the file as a {@link String}
     */
    public String getContent() { return this.content; }
    public String getPath() { return this.path; }

    /**
     * @return {@code true} if the file is in write mode, {@code false} if in read mode
     */
    public boolean getMode() { return this.mode; }
    public File getRaw() { return this.raw; }
}
