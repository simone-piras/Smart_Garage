package controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.GoogleLoginException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

/*
 System.out utilizzato per l'interazione utente durante l'autenticazione OAuth.
 In un flusso di autenticazione interattivo, l'output diretto all'utente è appropriato.
 */
@SuppressWarnings("java:S106")
public class GoogleLoginManager {

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of("profile", "email");

    public Credential authorize() throws GoogleLoginException {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            InputStream in = GoogleLoginManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new GoogleLoginException("File credentials.json non trovato");
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(Paths.get("tokens").toFile());
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(dataStoreFactory)
                    .setAccessType("offline")
                    .build();

            String userId = "user_" + System.currentTimeMillis();
            /*facendo così ogni volta viene salvato un nuovo token e dopo tanti login la cartella tokens si riempirà di credenziali inutilizzate
            nel caso si volesse evitare ciò bisognerebbe distinguere se ci si vuole loggare con un nuovo account oppure no, se si allora si utilizza
            System.currentTimeMillis, altrimenti si utilizzerà il token già salvato nella cartella
             */

            Credential credential = flow.loadCredential(userId);

            // Verifica se il token è valido
            if (credential == null || credential.getAccessToken() == null || !credential.refreshToken()) {

                System.out.println("Token scaduto o revocato. Avvio nuovo login Google...");
                credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(userId);
            }

            return credential;
        } catch (Exception e) {
            throw new GoogleLoginException("Errore durante l'autorizzazione Google: " + e.getMessage(), e);
        }
    }

    // Metodo alternativo per ottenere l'email tramite chiamata HTTP
    public String getEmailFromGoogle() throws GoogleLoginException {
        try {
            Credential credential = authorize();

            HttpRequestFactory requestFactory = GoogleNetHttpTransport.newTrustedTransport()
                    .createRequestFactory(credential);

            HttpRequest request = requestFactory.buildGetRequest(
                    new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo"));
            request.getHeaders().setAccept("application/json");

            HttpResponse response = request.execute();
            String json = response.parseAsString();

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return jsonObject.get("email").getAsString();
        } catch (Exception e) {
            throw new GoogleLoginException("Errore durante il recupero dell'email da Google: " + e.getMessage(), e);
        }
    }
}