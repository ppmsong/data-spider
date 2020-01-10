package com.crw.common;

import java.io.Serializable;

public class Result implements Serializable {
	public int code;
	public Object data;
	public Result(int code, Object data) {
		this.code = code;
		this.data = data;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}


}
