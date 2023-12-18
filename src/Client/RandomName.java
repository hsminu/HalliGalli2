package Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class RandomName {
    String filePath = "names.txt";
    List<String> names;
    public RandomName(){
        try {
            names = Files.readAllLines(Paths.get(filePath));
            names.replaceAll(String::trim);
        } catch (IOException e){
            System.out.println(e.toString());
        }
    }

    String getRandomName(){
        Random rand = new Random();
        return names.get(rand.nextInt(names.size()));
    }

}
