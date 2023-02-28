package acme.storefront;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Map;
import jodd.json.*;

public class Configutron{
    private static String deploymentPath = "/home/ec2-user/";
    private static String homeDir = System.getProperty("user.home");
    private static String[] locations = {"/dev/demo_public/Infratron/ansible/", "/demotron_public/Infratron/ansible/", "/code/demotron_public/Infratron/ansible/", "/newrelic/demotron/demotron_public/Infratron/ansible/"};
    private static String configFileName = "env_vars.json";

    // TODO - load file only once if not already loaded
    public static String getValue(String key){
        File file = loadFile();
        if(file == null){
            return "";
        }
        String jsonString = praseJsonFile(file);

        String value = null;
        if(jsonString != null && !jsonString.equals("")) {
            JsonParser jsonParser = new JsonParser();
            Map map = jsonParser.parse(jsonString);
            value =  (String) map.get(key);
        }
        return value;
    }

    private static File loadFile(){
        // Test for the deployed file location
        String fullPath = deploymentPath + configFileName;
        File file = new File(fullPath);
        if(file.exists() && !file.isDirectory()) {
            return file;
        }

        // Test for dev locations
        for (String possiblePath: locations){
            fullPath = homeDir + possiblePath + configFileName;
            file = new File(fullPath);
            if(file.exists() && !file.isDirectory()) {
                return file;
            }
        }

        return null;
    }
    
    private static String praseJsonFile(File file){
      StringBuilder sb = new StringBuilder();

      try (Scanner scanner = new Scanner(file)) {
        while (scanner.hasNext()){
          sb.append(scanner.nextLine());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      return sb.toString();
    }



}
