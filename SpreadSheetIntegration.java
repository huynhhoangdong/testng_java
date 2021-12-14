package helper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpreadSheetIntegration {

    public String ROOT_FORLDER = System.getProperty("user.dir");
    public final String APPLICATION_NAME = "Test Spreadsheet";
    public final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public final String TOKENS_DIRECTORY_PATH = "tokens";
    public final String CREDENTIAL_FILE_PATH = "/Users/hoangdong/eclipse-workspace/Selenium4Project/resources/client_secret.json";

    public final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    public NetHttpTransport HTTP_TRANSPORT = null; //Khai báo HTTP Transport
    public Sheets service; //Khai báo Sheet service
    public String valueInputOption = "USER_ENTERED"; //Khai báo option của input
    public ValueRange response; //Khai báo response
    public String spreadSheetID; //Lưu trữ spread sheet ID của sheet được tạo ra
    public String range = "A:B"; //Khai báo range muốn xử lý trong google sheet

    //Tạo constructure để khởi tạo service và HTTP_TRANSPORT
    public SpreadSheetIntegration() throws IOException, GeneralSecurityException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Function to create an authorized credential object
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object
     * @throws IOException If the credentials.json file can not be found
     */
    public Credential getCredential(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        //Load client secrets
        InputStream in = SpreadSheetIntegration.class.getResourceAsStream(CREDENTIAL_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIAL_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        //Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void createANewSheet() throws IOException {
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                        .setTitle("Sheet Test"));
        spreadsheet = service.spreadsheets().create(spreadsheet)
                .setFields("spreadsheetId")
                .execute();
        spreadSheetID = spreadsheet.getSpreadsheetId(); 
//        spreadSheetID = "1hmMS9DEFo2Z5CAVyFm7KDLJCHX9sqq0wrPoGrFiyAK8"; //Gán giá trị spreadSheetID cho sheet mới tạo
        //In ra link Google Sheet vừa tạo
        System.out.println("http://docs.google.com/spreadsheets/d/" + spreadSheetID);
    }

    public void appendDataToSpreadSheet(Object username, Object password) {
        List<List<Object>> values = Arrays.asList(Arrays.asList(username, password));
        ValueRange body = new ValueRange().setValues(values);
        try {
            AppendValuesResponse result = service.spreadsheets().values().append(spreadSheetID, range, body).setValueInputOption(valueInputOption).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> readDataFromSpreadSheet() throws IOException {
        List<String> dataSet = new ArrayList<String>();
        response = service.spreadsheets().values().get(spreadSheetID, range).execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found");
        } else {
            for (List<Object> row : values) {
                dataSet.add(row.get(0) + " " + row.get(1));
            }
        }
        System.out.println(dataSet);
        return dataSet;
    }
}
