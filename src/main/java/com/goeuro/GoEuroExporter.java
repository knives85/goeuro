package com.goeuro;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.client.ClientBuilder;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * GoEuroExporter main
 *
 */
public class GoEuroExporter
{
    private static final String JSON_ENDPOINT = "http://api.goeuro.com/api/v2/position/suggest/en/";
    private static final String CSV_SEPARATOR = ",";
    private static final String JSON_FIELD_ID = "_id";
    private static final String JSON_FIELD_NAME = "name";
    private static final String JSON_FIELD_TYPE = "type";
    private static final String JSON_FIELD_LAT = "latitude";
    private static final String JSON_FIELD_LONG = "longitude";
    private static final String JSON_FIELD_GEO_POS = "geo_position";

    public static void main( String[] args )
    {
        String fileName = System.currentTimeMillis() + "_goeuro_result.csv";
        BufferedWriter writer = null;

        try {

            //check if the user specified the parameter
            if (args.length == 0) {
                System.out.println("Please, specify the query string (es: java -jar myjar.jar QUERYSTRING)");
                return;
            }

            //invoke the endpoint
            System.out.println("Searching " + args[0]);
            String responseEntity = ClientBuilder.newClient()
                    .target(JSON_ENDPOINT).path(args[0])
                    .request()
                    .get(String.class);


            if (responseEntity == null) {
                throw new GoEuroException("Error invoking the endpoint, please retry");
            }

            //parse the response and prepare the file content
            System.out.println("Response received, parsing the results...");
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode resultsList = objectMapper.readTree(responseEntity);
            if (!resultsList.isArray()) {
                throw new GoEuroException("Error parsing the endpoint response, unexpected response format.");
            }

            if (resultsList.size() == 0) {
                System.out.println("No results found for " + args[0]);
                return;
            }

            System.out.println("Creating the output file...");
            Iterator<JsonNode> resultsIterator = resultsList.getElements();
            writer = new BufferedWriter(new FileWriter(fileName));

            while (resultsIterator.hasNext()) {
                JsonNode currResult = resultsIterator.next();
                writer.write("\"" + currResult.get(JSON_FIELD_ID).asText() + "\"" + CSV_SEPARATOR);
                writer.write("\"" + currResult.get(JSON_FIELD_NAME).asText() + "\"" + CSV_SEPARATOR);
                writer.write("\"" + currResult.get(JSON_FIELD_TYPE).asText() + "\"" + CSV_SEPARATOR);
                writer.write("\"" + currResult.get(JSON_FIELD_GEO_POS).get(JSON_FIELD_LAT).asText() + "\"" + CSV_SEPARATOR);
                writer.write("\"" + currResult.get(JSON_FIELD_GEO_POS).get(JSON_FIELD_LONG).asText() + "\"" );
                writer.newLine();
            }

            //fine, tell the user where to find the result
            System.out.println("Process Complete.");
            System.out.println("Result in " + fileName);

        } catch (GoEuroException goe) {
            System.out.println(goe);
        } catch (IOException ioe) {
            System.out.println("Error occurred while parsing the result and creating the file.");
        } catch (Exception e) {
            System.out.println("Unexpected error, please retry.");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {}
        }
    }
}
class GoEuroException extends Exception {
    public GoEuroException(String message) {
        super(message);
    }
}