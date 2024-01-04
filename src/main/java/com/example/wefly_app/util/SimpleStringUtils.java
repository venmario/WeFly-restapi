package com.example.wefly_app.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Component
public class SimpleStringUtils {
    public static String randomString(int size) {
        return randomString(size, false);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static String randomString(int size, boolean numberOnly) {
        String saltChars = "1234567890";
        if (!numberOnly) {
            saltChars += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        }
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < size) {
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }

        return salt.toString();
    }

    public Pageable getShort(String orderby, String ordertype, Integer page, Integer size) {
        Pageable show_data;
        if (orderby != null) {
            if (ordertype != null) {
                if (ordertype.equals("desc")) {
                    return show_data = PageRequest.of(page, size, Sort.by(orderby).descending());
                } else {
                    return    show_data = PageRequest.of(page, size, Sort.by(orderby).ascending());
                }
            } else {
                return  show_data = PageRequest.of(page, size, Sort.by(orderby).descending());
            }

        } else {
            return  show_data = PageRequest.of(page, size, Sort.by("id").descending());
        }
    }
    public String convertDateToString(Date date) {

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String strDate = dateFormat.format(date);
        return strDate;
    }

//    public String tokenExtractor(String token) {
//        Claims claims = Jwts.parser()
//                .setSigningKey("your-signing-key") // Replace with your actual signing key
//                .parseClaimsJws(jwtToken)
//                .getBody();
//
//        // Extract the user_name
//        return claims.get("user_name", String.class)
//    }

}
