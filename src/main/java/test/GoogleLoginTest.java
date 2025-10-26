package test;

import com.google.api.client.auth.oauth2.Credential;
import controller.GoogleLoginManager;

public class GoogleLoginTest {
    public static void main(String[] args) {
        try {
            GoogleLoginManager manager = new GoogleLoginManager();
            Credential credential = manager.authorize();
            System.out.println("Access Token: " + credential.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}