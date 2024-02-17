package com.example.wefly_app.util;

import org.springframework.stereotype.Component;

@Component("emailTemplate")
public class EmailTemplate {
    public String getRegisterTemplate() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "    .email-container {\n" +
                "        padding-top: 10px;\n" +
                "    }\n" +
                "    p {\n" +
                "        text-align: left;\n" +
                "    }\n" +
                "\n" +
                "    a.btn {\n" +
                "        display: block;\n" +
                "        margin: 30px auto;\n" +
                "        background-color: #01c853;\n" +
                "        padding: 10px 20px;\n" +
                "        color: #fff;\n" +
                "        text-decoration: none;\n" +
                "        width: 30%;\n" +
                "        text-align: center;\n" +
                "        border: 1px solid #01c853;\n" +
                "        text-transform: uppercase;\n" +
                "    }\n" +
                "    a.btn:hover,\n" +
                "    a.btn:focus {\n" +
                "        color: #01c853;\n" +
                "        background-color: #fff;\n" +
                "        border: 1px solid #01c853;\n" +
                "    }\n" +
                "    .user-name {\n" +
                "        text-transform: uppercase;\n" +
                "    }\n" +
                "    .manual-link,\n" +
                "    .manual-link:hover,\n" +
                "    .manual-link:focus {\n" +
                "        display: block;\n" +
                "        color: #396fad;\n" +
                "        font-weight: bold;\n" +
                "        margin-top: 15px;\n" +
                "    }\n" +
                "    .mt--15 {\n" +
                "        margin-top: 15px;\n" +
                "    }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <p>Hi! <span class=\"user-name\">{{USERNAME}}</span>, Welcome to WeFly</p>\n" +
                "        <p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">To complete account registration please do email confirmation by clicking the <a href=\"{{CONFIRMATION_URL}}\" style=\"color:blue; text-decoration:underline;\"><b>LINK<b/></a> </p>\n" +
                "        <p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">If this is not you, you can ignore this message.</p>\n" +
                "<p></p>\n" +
                "        <p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">Have A Nice Day!</p>\n" +
                "        <p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\"><a href=\"{{HOMEPAGE_URL}}\" style=\"color:blue; text-decoration:underline;\"><b>WeFly</b></a></p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";
    }

    public String getResetPasswordOTP(){

        return "<!doctype html>\n" +
                "<html lang=\"en-US\">\n" +
                "<head>" +
                "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />\n" +
                "<title>Reset Password Email Template</title>\n" +
                "<meta name=\"description\" content=\"Reset Password Email Template.\"> \n" +
                "<style type=\"text/css\"> \n" +
                "a:hover {text-decoration: underline !important;}\n" +
                "</style> \n" +
                "</head>\n" +
                "<body marginheight=\"0\" topmargin=\"0\" marginwidth=\"0\" style=\"margin: 0px; background-color: #f2f3f8;\" leftmargin=\"0\">\n" +
                "<table cellspacing=\"0\" border=\"0\" cellpadding=\"0\" width=\"100%\" bgcolor=\"#f2f3f8\"\n" +
                "style=\"@import url(https://fonts.googleapis.com/css?family=Rubik:300,400,500,700|Open+Sans:300,400,600,700); font-family: 'Open Sans', sans-serif;\">\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table style=\"background-color: #f2f3f8; max-width:670px;  margin:0 auto;\" width=\"100%\" border=\"0\"" +
                "align=\"center\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "<tr>\n" +
                "<td style=\"height:80px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"text-align:center;\">\n" +
                "<a href=\"#\" title=\"logo\" target=\"_blank\">\n" +
                "<img width=\"250\" src=\"https://lh3.googleusercontent.com/drive-viewer/AEYmBYTb_2Rd39mj2YVNilLT6puz8gia9I7kvH2CmUZBtXTI9xykpJeRRwg9CNRuME9_kmtCI9cq-s2vrdWMiJcuCiePfSZW=s2560\" title=\"logo\" alt=\"logo\">\n" +
                "</a>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"height:20px;\">&nbsp;</td>\n"+
                "</tr>\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table width=\"95%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\"" +
                "style=\"max-width:670px;background:#fff; border-radius:3px; text-align:center;-webkit-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);-moz-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);box-shadow:0 6px 18px 0 rgba(0,0,0,.06);\">\n" +
                "<tr>\n" +
                "<td style=\"height:40px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"padding:0 35px;\">\n" +
                "<h1 style=\"color:#1e1e2d; font-weight:500; margin:0;font-size:32px;font-family:'Rubik',sans-serif;\">Hi {{USERNAME}}, you have requested verification code for your password.</h1>\n" +
                "<span " +
                "style=\"display:inline-block; vertical-align:middle; margin:29px 0 26px; border-bottom:1px solid #cecece; width:100px;\"></span>\n" +
                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
                "Please use the verification code below to reset your password : <br/> </p>" +
                "<strong style=\"font-size:24px;\">{{PASS_TOKEN}}</strong> <br/>\n" +
                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
                "If this action wasn’t done by you can ignore this message. </p>" +
                "<p></p>\n" +
                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
                "Have A Nice Day!,\n" +
                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
                "<a href=\"{{HOMEPAGE_URL}}\" style=\"color:blue; text-decoration:underline;\"><b>WeFly</b></a>\n" +
                "</p>\n" +
                "</td>\n"+
                "</tr>\n"+
                "<tr>\n" +
                "<td style=\"height:40px;\">&nbsp;</td>" +
                "</tr>\n"+
                "</table>\n"+
                "</td>\n" +
                "<tr>\n" +
                "<td style=\"height:20px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n"+
                "<td style=\"text-align:center;\">\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"height:80px;\">&nbsp;</td>\n"+
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>\n";
    }

    public String getPaymentProofTemplate(){
        return  "<!doctype html>\n" +
                "<html lang=\"en-US\">\n" +
                "<head>\n" +
                "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />\n" +
                "<title>Payment Proof Email</title>\n" +
                "<meta name=\"description\" content=\"Payment Proof Email\"> \n" +
                "<style type=\"text/css\"> \n" +
                "a:hover {text-decoration: underline !important;}\n" +
                "</style> \n" +
                "</head>\n" +
                "<body marginheight=\"0\" topmargin=\"0\" marginwidth=\"0\" style=\"margin: 0px; background-color: #f2f3f8;\" leftmargin=\"0\">\n" +
                "<table cellspacing=\"0\" border=\"0\" cellpadding=\"0\" width=\"100%\" bgcolor=\"#f2f3f8\"\n" +
                "style=\"@import url(https://fonts.googleapis.com/css?family=Rubik:300,400,500,700|Open+Sans:300,400,600,700); font-family: 'Open Sans', sans-serif;\">\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table style=\"background-color: #f2f3f8; max-width:670px; margin:0 auto;\" width=\"100%\" border=\"0\"\n" +
                "align=\"center\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "<tr>\n" +
                "<td style=\"height:80px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"text-align:center;\">\n" +
                "<a href=\"#\" title=\"logo\" target=\"_blank\">\n" +
                "<img width=\"250\" src=\"https://lh3.googleusercontent.com/drive-viewer/AEYmBYTb_2Rd39mj2YVNilLT6puz8gia9I7kvH2CmUZBtXTI9xykpJeRRwg9CNRuME9_kmtCI9cq-s2vrdWMiJcuCiePfSZW=s2560\" title=\"logo\" alt=\"logo\">\n" +
                "</a>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"height:20px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table width=\"95%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\"\n" +
                "style=\"max-width:670px;background:#fff; border-radius:3px; text-align:left;-webkit-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);-moz-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);box-shadow:0 6px 18px 0 rgba(0,0,0,.06);\">\n" +
                "<tr>\n" +
                "<td style=\"height:40px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"padding:0 35px;\">\n" +
                "<h1 style=\"color:#1e1e2d; font-weight:500; margin:0;font-size:32px;font-family:'Rubik',sans-serif;\">Hi, {{USERNAME}}</h1>\n" +
                "<span style=\"display:inline-block; vertical-align:middle; margin:29px 0 26px; border-bottom:1px solid #cecece; width:100px;\"></span>\n" +
                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
                "{{MESSAGE}}<br/> </p>\n" +
                "<p></p>\n" +
                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
                "{{THANK_MESSAGE}},\n" +
                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
                "<a href=\"{{HOMEPAGE_URL}}\" style=\"color:blue; text-decoration:underline;\"><b>WeFly</b></a>\n" +
                "</p>\n" +
                "<p></p>\n" +
                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
                "<b>\"Elevating The Skies With Flight Excellence!\"</b> </p>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"height:40px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"height:20px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"text-align:center;\">\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"height:80px;\">&nbsp;</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>\n"
                ;
    }

//    public String getBoardingPass() {
//        return "<!doctype html>\n" +
//                "<html lang=\"en-US\">\n" +
//                "<head>\n" +
//                "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />\n" +
//                "<title>Payment Proof Email</title>\n" +
//                "<meta name=\"description\" content=\"Payment Proof Email\"> \n" +
//                "<style type=\"text/css\"> \n" +
//                "a:hover {text-decoration: underline !important;}\n" +
//                "</style> \n" +
//                "</head>\n" +
//                "<body marginheight=\"0\" topmargin=\"0\" marginwidth=\"0\" style=\"margin: 0px; background-color: #f2f3f8;\" leftmargin=\"0\">\n" +
//                "<table cellspacing=\"0\" border=\"0\" cellpadding=\"0\" width=\"100%\" bgcolor=\"#f2f3f8\"\n" +
//                "style=\"@import url(https://fonts.googleapis.com/css?family=Rubik:300,400,500,700|Open+Sans:300,400,600,700); font-family: 'Open Sans', sans-serif;\">\n" +
//                "<tr>\n" +
//                "<td>\n" +
//                "<table style=\"background-color: #f2f3f8; max-width:670px; margin:0 auto;\" width=\"100%\" border=\"0\"\n" +
//                "align=\"center\" cellpadding=\"0\" cellspacing=\"0\">\n" +
//                "<tr>\n" +
//                "<td style=\"height:80px;\">&nbsp;</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td style=\"text-align:center;\">\n" +
//                "<a href=\"#\" title=\"logo\" target=\"_blank\">\n" +
//                "<img width=\"250\" src=\"https://lh3.googleusercontent.com/drive-viewer/AEYmBYTb_2Rd39mj2YVNilLT6puz8gia9I7kvH2CmUZBtXTI9xykpJeRRwg9CNRuME9_kmtCI9cq-s2vrdWMiJcuCiePfSZW=s2560\" title=\"logo\" alt=\"logo\">\n" +
//                "</a>\n" +
//                "</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td style=\"height:20px;\">&nbsp;</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td>\n" +
//                "<table width=\"95%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\"\n" +
//                "style=\"max-width:670px;background:#fff; border-radius:3px; text-align:left;-webkit-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);-moz-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);box-shadow:0 6px 18px 0 rgba(0,0,0,.06);\">\n" +
//                "<tr>\n" +
//                "<td style=\"height:40px;\">&nbsp;</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td style=\"padding:0 35px;\">\n" +
//                "<h1 style=\"color:#1e1e2d; font-weight:500; margin:0;font-size:32px;font-family:'Rubik',sans-serif;\">Hi, {{USERNAME}}</h1>\n" +
//                "<span style=\"display:inline-block; vertical-align:middle; margin:29px 0 26px; border-bottom:1px solid #cecece; width:100px;\"></span>\n" +
//                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
//                "Here we attached your boarding pass for all the passengers. Thank you for using our services, it is a pleasure to serve you. Enjoy your flight, hope you reach your destination safely. <br/> </p>\n" +
//                "<p></p>\n" +
//                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
//                "Best Regards,\n" +
//                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
//                "<a href=\"{{HOMEPAGE_URL}}\" style=\"color:#455056; text-decoration:none;\">WeFly</a>\n" +
//                "</p>\n" +
//                "<p></p>\n" +
//                "<p style=\"color:#455056; font-size:15px;line-height:24px; margin:0;\">\n" +
//                "<b>\"Elevating The Skies With Flight Excellence!\"</b> </p>\n" +
//                "</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td style=\"height:40px;\">&nbsp;</td>\n" +
//                "</tr>\n" +
//                "</table>\n" +
//                "</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td style=\"height:20px;\">&nbsp;</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td style=\"text-align:center;\">\n" +
//                "</td>\n" +
//                "</tr>\n" +
//                "<tr>\n" +
//                "<td style=\"height:80px;\">&nbsp;</td>\n" +
//                "</tr>\n" +
//                "</table>\n" +
//                "</td>\n" +
//                "</tr>\n" +
//                "</table>\n" +
//                "</body>\n" +
//                "</html>\n"
//                ;
//    }

}

