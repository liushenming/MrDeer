package com.liushenming.mrdeer.translatemodule;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 
 * @author liushenming
 * used to parse the HTML String.
 *
 */
public class HtmlParser {

	private String htmlstring="";
	
	private final static Pattern PATTERN_H=Pattern.compile("<h[1-6]>(.+)?<//h[1-6]>");
	
	public HtmlParser(String hstring){
		this.htmlstring=hstring;
	}
	
	public List<String> geth(){
		return null;
	}
}
