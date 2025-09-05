package com.dev.hotelbooking.constants;

public class Constants {
    private Constants() {
        throw new UnsupportedOperationException("Utility class!");
    }

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String PHOTO_DIRECTORY = System.getProperty("user.home") + "/Downloads/uploads/";
}

