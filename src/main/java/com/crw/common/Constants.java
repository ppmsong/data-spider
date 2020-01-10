package com.crw.common;

public class Constants {
	public final static int SUCCESS = 0;
	public final static int INPUTERROR = 1;
	public final static int SYSTEMERROR = 5;

	public static String getMessage(int key) {
		switch (key) {
		case SYSTEMERROR:
			return "system error";
		case INPUTERROR:
			return "input error";
		case SUCCESS:
			return "success";
		default:
			break;
		}
		return "";
	}

}
