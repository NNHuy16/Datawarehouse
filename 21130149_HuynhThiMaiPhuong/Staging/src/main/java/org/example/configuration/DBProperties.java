package org.example.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class DBProperties {
    private static Properties properties = new Properties();

    public static String dbControllerHost;
    public static String dbControllerPort;
    public static String dbControllerUsername;
    public static String dbControllerPassword;
    public static String dbController;

    public static String dbStagingHost;
    public static String dbStagingPort;
    public static String dbStagingUsername;
    public static String dbStagingPassword;
    public static String dbStaging;

    public static String email;

//    1. Load file DB.properties
    static {
        File f = null;
        try {
            f = new File(DBProperties.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String propDir = f.getParentFile().getAbsolutePath() + "\\DB.properties";
            File proFile = new File(propDir);
            loadProperties(proFile);
        } catch (IOException | URISyntaxException e) {
            f = new File(System.getProperty("user.dir"));
            String propDir = f.getParentFile().getAbsolutePath() + "\\DB.properties";
            File proFile = new File(propDir);
            try {
                loadProperties(proFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    private static void loadProperties(File file) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            properties.load(in);
        }

        dbControllerHost = properties.getProperty("db_controller.host");
        dbControllerPort = properties.getProperty("db_controller.port");
        dbControllerUsername = properties.getProperty("db_controller.username");
        dbControllerPassword = properties.getProperty("db_controller.password");
        dbController = properties.getProperty("db_controller.name");

        dbStagingHost = properties.getProperty("db_staging.host");
        dbStagingPort = properties.getProperty("db_staging.port");
        dbStagingUsername = properties.getProperty("db_staging.username");
        dbStagingPassword = properties.getProperty("db_staging.password");
        dbStaging = properties.getProperty("db_staging.name");

        email = properties.getProperty("email");
    }
}