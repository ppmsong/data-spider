package com.crw.util;

import java.security.MessageDigest;

public class MD5 {

  public static String encode(String sourceString) {
    return encodeLowerCase(sourceString).toUpperCase();
  }

  public static String encodeLowerCase(String sourceString) {
    String resultString = null;
    try {
      resultString = new String(sourceString);
      MessageDigest md = MessageDigest.getInstance("MD5");
      resultString = byte2hexString(md.digest(resultString.getBytes()));
    } catch (Exception ex) {
      System.out.println(ex);
    }
    return resultString;
  }

  public static final String byte2hexString(byte bytes[]) {
    StringBuffer buf = new StringBuffer(bytes.length * 2);
    for (int i = 0; i < bytes.length; i++) {
      if ((bytes[i] & 0xff) < 16) {
        buf.append("0");
      }
      buf.append(Long.toString(bytes[i] & 0xff, 16));
    }
    return buf.toString();
  }

}
