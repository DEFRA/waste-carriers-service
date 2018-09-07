package uk.gov.ea.wastecarrier.services.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class ResourceHelper {

    private ClassLoader loader;

    private static Logger log = Logger.getLogger(ResourceHelper.class.getName());

    public ResourceHelper() {
        this.loader = this.getClass().getClassLoader();
    }

    public String openResourceFile(String path, String name) {

        String content = null;

        InputStream inputStream = loader.getResourceAsStream(pathToResource(path, name));

        if (inputStream != null) content = resourceToString(inputStream);

        return content;
    }

    private String pathToResource(String path, String name) {
        return String.format(
                "%s/%s",
                path,
                name.replaceAll(" ", "")
        );
    }

    private String resourceToString(InputStream inputStream) {

        StringBuilder content = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();
            inputStream.close();
        } catch (IOException ex) {
            log.severe("ResourceHelper - error converting resource to string: " + ex.getMessage());
        }

        return content.toString();
    }
}
