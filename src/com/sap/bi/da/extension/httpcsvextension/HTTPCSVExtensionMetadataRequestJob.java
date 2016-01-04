package com.sap.bi.da.extension.httpcsvextension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.bi.da.extension.sdk.DAException;
import com.sap.bi.da.extension.sdk.IDAEAcquisitionState;
import com.sap.bi.da.extension.sdk.IDAEEnvironment;
import com.sap.bi.da.extension.sdk.IDAEMetadataAcquisitionJob;
import com.sap.bi.da.extension.sdk.IDAEProgress;
import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public class HTTPCSVExtensionMetadataRequestJob implements IDAEMetadataAcquisitionJob {
	public static File dataFile;
	
	IDAEAcquisitionState acquisitionState;
	IDAEEnvironment environment;
    

    HTTPCSVExtensionMetadataRequestJob (IDAEEnvironment environment, IDAEAcquisitionState acquisitionState) {
        this.acquisitionState = acquisitionState;
        this.environment = environment;
    }
    
    public String csvMetadata = "";
    
    private List<String> tableHeader = new ArrayList<>();
    private HashMap<String, String> tableHeaderDataTypes = new HashMap<>();
    

    @Override
    public String execute(IDAEProgress callback) throws DAException {
    	 try {
			JSONObject infoJSON = new JSONObject(acquisitionState.getInfo());
			String url = infoJSON.getString("url");
			String requestType = infoJSON.getString("requesttype");
			String username = infoJSON.getString("username");
			String password = infoJSON.getString("password");
			String body = infoJSON.getString("body");
			
			// Use below code if there are HTTPS certificate issues with API 
			// Uncomment code only if absolutely necessary
			
			// Create a trust manager that does not validate certificate chains
//			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
//			    public X509Certificate[] getAcceptedIssuers(){return null;}
//			    public void checkClientTrusted(X509Certificate[] certs, String authType){}
//			    public void checkServerTrusted(X509Certificate[] certs, String authType){}
//			}};
//			
//			// Install the all-trusting trust manager
//			try {
//			    SSLContext sc = SSLContext.getInstance("TLS");
//			    sc.init(null, trustAllCerts, new SecureRandom());
//			    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//			} catch (Exception e) {
//			    
//			}
			
			URL myurl = new URL(url);
		    HttpURLConnection con = (HttpURLConnection)myurl.openConnection();
		    
		    con.setRequestMethod(requestType);
			con.setRequestProperty("Content-Type", "text/csv");
			con.setRequestProperty("Accept", "*/*");
		    
		    //basic auth
			String authString = username + ":" + password;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			con.setRequestProperty("Authorization", "Basic "+authStringEnc);
		    
		    InputStream in = con.getInputStream();
		    String encoding = con.getContentEncoding();
		    encoding = encoding == null ? "UTF-8" : encoding;
		    String responseBody = IOUtils.toString(in, encoding);
    		in.close();
    		
            InputStream stream = new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8));
            
            CsvParserSettings settings = new CsvParserSettings();
            RowListProcessor rowProcessor = new RowListProcessor();
            settings.setRowProcessor(rowProcessor);
            settings.setLineSeparatorDetectionEnabled(true);

            settings.setHeaderExtractionEnabled(true);
//          settings.selectFields("Header1", "Header2");
            
            CsvParser parser = new CsvParser(settings);
            
            parser.parse(newReader(stream));
            tableHeader = new ArrayList<String>(Arrays.asList(rowProcessor.getHeaders()));
            tableHeader.removeAll(Collections.singleton(null));
            
            List<String[]> rows = rowProcessor.getRows();
            
            File csvFile = new File("");
			dataFile = File.createTempFile(HTTPCSVExtension.EXTENSION_ID, ".csv", environment.getTemporaryDirectory());
			
            FileOutputStream csvResult = new FileOutputStream(dataFile);
            Writer outputWriter = new OutputStreamWriter(csvResult);
    		
    		CsvWriterSettings writerSettings = new CsvWriterSettings();
    		CsvWriter writer = new CsvWriter(outputWriter, writerSettings);

    		for (int i = 0; i < rows.size(); i++) {
    	        for(int j = 0; j < rows.get(i).length; j++){
    	        	// TODO rewrite, check csvparser for an option
    	        	if (i == 1) {
						// sample column value rows.get(1)[j];
    	        		// column name tableHeader[j]
    	        		// read value and save it in HashMap as Number, String or Boolean
    	        		tableHeaderDataTypes.put(tableHeader.get(j), NumberUtils.isNumber(rows.get(1)[j]) ? "Number" : "String");
					}
    	        	
    				writer.writeValue(rows.get(i)[j]);
    			}
    	        writer.writeValuesToRow();
    		}
    		writer.close();
		    
    		generateMetadataString();
			
    		return csvMetadata;
         } catch (Exception e) {
             throw new DAException("HTTP CSV Extension acquisition failed" + e.toString(), e);
         }
    }
    
    private void generateMetadataString(){
    	csvMetadata = "";

    	ObjectMapper mapper = new ObjectMapper();

        // Version.
        JsonNode rootNode = mapper.createObjectNode();
        ((ObjectNode) rootNode).put("version", "1.0");
        JsonNode columns = mapper.createArrayNode();

        // Columns.
        ((ObjectNode) rootNode).put("columns", columns);
        Iterator<String> columnIter = tableHeader.iterator();
        Pattern patternId = Pattern.compile("[^a-zA-Z0-9_]+", Pattern.CASE_INSENSITIVE);
        while (columnIter.hasNext()) {
            // Define the column metadata.
            String colName = columnIter.next();
            String myColName = colName.replaceAll("\"", ""); // Remove double quotes.
            
            String colDataType = tableHeaderDataTypes.get(colName);
            
            if (colDataType != null && colDataType.length() > 0) {
                Boolean isMeasure = colDataType == "Number";
                String analyticalType = (isMeasure ? "measure" : "dimension");
                String id = myColName.replaceAll(patternId.pattern(), "_");
                
                JsonNode colNode = mapper.createObjectNode();
                ((ObjectNode) colNode).put("name", myColName);
                ((ObjectNode) colNode).put("id", id);
                ((ObjectNode) colNode).put("type", colDataType);
                ((ObjectNode) colNode).put("analyticalType", analyticalType);
                if (isMeasure) {
                    ((ObjectNode) colNode).put("aggregationFunction", "NONE");
                }

                // Add the column metadata.
                ((ArrayNode) columns).add(colNode);
            }
        }

        // save JSON metadata.
        csvMetadata = rootNode.toString();
    }

    @Override
    public void cancel() {
    	// Cancel is currently not supported
    }

    @Override
    public void cleanup() {
    	// Called once acquisition is complete
    }
    
    public static Reader newReader(InputStream input) {
		return newReader(input, (Charset) null);
	}

	public static Reader newReader(InputStream input, String encoding) {
		return newReader(input, Charset.forName(encoding));
	}

	public static Reader newReader(InputStream input, Charset encoding) {
		if (encoding != null) {
			return new InputStreamReader(input, encoding);
		} else {
			return new InputStreamReader(input);
		}
	}
    
    public static Reader newReader(File file) {
		return newReader(file, (Charset) null);
	}

	public static Reader newReader(File file, String encoding) {
		return newReader(file, Charset.forName(encoding));
	}

	public static Reader newReader(File file, Charset encoding) {
		FileInputStream input;
		try {
			input = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}

		return newReader(input, encoding);
	}
	
}

