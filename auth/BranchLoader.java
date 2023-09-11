package auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BranchLoader {

    private Path filePath;

    public BranchLoader(String path) {
        this.filePath = Paths.get(path);
    }

    public Map<String, Branch> loadBranchesFromFile() {
        Map<String, Branch> branches = new HashMap<>();

        try {
            List<String> lines = Files.readAllLines(filePath);

            for(String line : lines) {
                String[] parts = line.split(",");

                if(parts.length == 2) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();

                    branches.put(id, new Branch(id, name));
                }
            }

        } catch(IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        return branches;
    }

//    public static void main(String[] args) {
//        BranchLoader loader = new BranchLoader("branches.txt");
//        Map<String, Branch> branches = loader.loadBranchesFromFile();
//
//        // Test print
//        for (Map.Entry<String, Branch> entry : branches.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue().getBranchName());
//        }
//    }

}