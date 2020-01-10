package com.crw.util;

import org.joda.time.DateTime;

public class JodatimeUtils {

  public static final String FORMAT_YYYYMMDD_01 = "yyyy-MM-dd";
  public static final String FORMAT_YYYYMM_01 = "yyyy-MM";
  public static final String FORMAT_YYYYMM_02 = "yyyyMM";

  public static String now(String format) {
    return new DateTime().toString(format);
  }

}
