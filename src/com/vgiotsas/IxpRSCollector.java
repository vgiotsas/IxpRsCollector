package com.vgiotsas;

import com.sun.istack.internal.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

/**
 * <h1>IXP Route Server Collector</h1>
 * The IxpRSCollector queries the <a href="https://www.peeringdb.com/">PeringDB</a> and
 * <a href="https://www.euro-ix.net/tools/ixp-service-matrix/">Euro-IX</a> datasets to collect the
 * Autonomous System Numbers (ASNs) used by IXP Route Servers and networks.
 * @author Vasileios Giotsas
 * @version 1.0
 * @since 2017-12-30
 */

public class IxpRSCollector {

    private final String USER_AGENT = "Mozilla/5.0";

    /**
     * The main method that collects and outputs the PeeringDB and Euro-IX data
     * @param args Unused
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        IxpRSCollector parser = new IxpRSCollector();
        String pdb_url = "https://www.peeringdb.com/api/net?info_type=Route%20Server";
        String euroix_url = "https://www.euro-ix.net/csv/ixp-service-matrix";
        ArrayList<String> pdb_response = parser.sendGet(pdb_url);
        ArrayList<String> euroix_response = parser.sendGet(euroix_url);

        JSONObject pdb_data = parser.parseJson(String.join("", pdb_response));
        HashSet<Integer> rs_asns = parser.parsePdbData(pdb_data);
        rs_asns.addAll(parser.parseEuroIXData(euroix_response));

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        FileWriter writer = new FileWriter("RouteServerASNs_"+dateFormat.format(date)+".txt");
        for(Integer asn: rs_asns) {
            writer.write(asn + "\n");
        }
        writer.close();
    }

    /**
     * Issues an HTTP GET request and returns the body of the reply as list of lines
     * @param url The URL to which the GET request is sent
     * @return ArrayList<String> The list of lines in the response body
     * @throws Exception
     */
    private ArrayList<String> sendGet(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        //int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        // StringBuffer response = new StringBuffer();
        ArrayList<String> response = new ArrayList<>();
        while ((inputLine = in.readLine()) != null) {
            response.add(inputLine);
        }
        in.close();

        return response;
    }

    /**
     * Convert JSON string to JSONObject
     * @param jsonString The string that represents a JSON object
     * @return JSONObject The decoded JSON object
     */
    private JSONObject parseJson(String jsonString){
        JSONObject jsonObject = null;
        JSONParser parser = new JSONParser();
        try{
            Object obj = parser.parse(jsonString);
            jsonObject = (JSONObject) obj;
        }catch(ParseException pe){

            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
        }

        return jsonObject;
    }

    /**
     * Parse the PeeringDB JSON response to extract and return the Route Server ASNs
     * @param pdbResponse The JSON response from the PeeringDB API
     * @return HashSet<Integer> The set of Route Server ASNs
     */
    private HashSet<Integer> parsePdbData(@NotNull JSONObject pdbResponse){
        HashSet<Integer> asns = new HashSet<>();
        JSONArray nets_array = (JSONArray) pdbResponse.get("data");
        if (nets_array != null){
            @SuppressWarnings("unchecked")
            Iterator<Object> iterator = nets_array.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if(obj instanceof JSONObject) {
                    JSONObject jsonObj = (JSONObject) obj;

                    Integer asn = (int) (long) jsonObj.get("asn");
                    asns.add(asn);
                }
            }
        }

        return asns;
    }

    /**
     * Parse the Euro-IX CSV file with the IXP data to get the Route Server & IXP ASNs
     * @param euroixResponse The lines of the Euro-IX CSV file
     * @return HashSet<Integer> The set of Route Server and IXP ASNs
     */
    private HashSet<Integer> parseEuroIXData(@NotNull  ArrayList<String> euroixResponse) {
        HashSet<Integer> asns = new HashSet<>();
        for (String line : euroixResponse) {
            String[] lf = line.split(",");
            for (int i=2; i<4; i++) {
                if (lf[i].replaceAll("\\s+","").length() > 0){
                    try{
                        Integer asn = Integer.parseInt(lf[i].replace("AS", ""));
                        if (asn > 0) {
                            asns.add(asn);
                            System.out.println(lf[i]);
                        }
                    }
                    catch (NumberFormatException n) {
                        continue;
                    }
                }
            }
        }

        return asns;
    }
}
