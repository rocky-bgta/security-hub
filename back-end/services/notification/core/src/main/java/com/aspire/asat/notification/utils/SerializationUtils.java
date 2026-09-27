package com.aspire.asat.notification.utils;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Random;

@UtilityClass
public final class SerializationUtils {

    public static <T> String serialize(T objectToBeSerialized) {
        byte[] byteArray = toByteStream(objectToBeSerialized);
        return toBase64(byteArray);
    }

    public static <T> T deserialize(String base64String, Class<T> clazz) {
        byte[] byteArray = toByteArray(base64String);
        return toObject(byteArray, clazz);
    }

    public static <T> byte[] toByteStream(T objectToBeSerialized) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream);
            out.writeObject(objectToBeSerialized);
            out.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new byte[0];
    }

    public static <T> T toObject(byte[] byteArray, Class<T> clazz) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            ObjectInputStream ois = new ObjectInputStream(bais);
            T obj = clazz.cast(ois.readObject());
            ois.close();
            return obj;
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String toBase64(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    public static byte[] toByteArray(String base64String) {
        return Base64.getUrlDecoder().decode(base64String);
    }

    public static String toByteArrayToString(String base64String) {
        return new String(Base64.getUrlDecoder().decode(base64String));
    }

    public static String camelCaseToTitle(String str) {
        try {
            String newStr = str.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
            newStr = newStr.substring(0, 1).toUpperCase() + newStr.substring(1).toLowerCase();
            return newStr;
        } catch (Exception ignored) {
            return str;
        }
    }

    public static String generateRandomString(Integer size) {
        final int leftLimit = 48; // numeral '0'
        final int rightLimit = 122; // letter 'z'
        final int targetStringLength = size;
        final Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
