package io.github.srinss01.generatorbot.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.github.srinss01.generatorbot.auth.hwid.HWID;
import lombok.NoArgsConstructor;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;

public class ActivationStatus {
    private static final String URL = "https://keyauth.win/api/1.1/";
    private static final String OWNER_ID = "nv8jWRD50g"; // You can find out the owner id in the profile settings keyauth.com
    private static final String APP_NAME = "TestSellix"; // Application name
    private static final String VERSION = "1.0"; // Application version
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static String sessionID;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ActivationStatus.class);
    private static final boolean headless = GraphicsEnvironment.isHeadless();

    public static void init() throws RuntimeException {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { X509TrustManagerImpl.instance() };

            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            Unirest.setHttpClient(httpclient);

            var response = Unirest.post(URL)
                    .field("type", "init")
                    .field("ownerid", OWNER_ID)
                    .field("name", APP_NAME)
                    .field("ver", VERSION)
                    .asString();
            String body = response.getBody();
            if (body.equalsIgnoreCase("KeyAuth_Invalid")) {
                throw new RuntimeException("Invalid keyauth credentials");
            }
            ResponseMap responseMap = GSON.fromJson(body, ResponseMap.class);
            boolean success = responseMap.getBoolean("success");
            if (success) {
                sessionID = responseMap.getString("sessionid");
            } else {
                String message = responseMap.getString("message");
                LOGGER.error("Error: {}", message);
                if (!headless) {
                    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
                }
                System.exit(1);
            }
        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage());
            if (!headless) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1);
        }
    }
    public static boolean check(String activationKey) throws UnirestException {
        String hwid = HWID.getHWID();
        var response = Unirest.post(URL)
                .field("type", "license")
                .field("ownerid", OWNER_ID)
                .field("name", APP_NAME)
                .field("key", activationKey)
                .field("hwid", hwid)
                .field("sessionid", sessionID).asString();
        String body = response.getBody();
        ResponseMap responseMap = GSON.fromJson(body, ResponseMap.class);
        return responseMap.getBoolean("success");
    }

    @NoArgsConstructor(staticName = "instance")
    private static class X509TrustManagerImpl implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class ResponseMap extends HashMap<Object, Object> {
        public String getString(Object key) {
            return get(key).toString();
        }

        public boolean getBoolean(Object key) {
            return Boolean.parseBoolean(getString(key));
        }
    }
}
