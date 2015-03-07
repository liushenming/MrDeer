package com.liushenming.mrdeer.translatemodule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * xxx  "xxx"
 * part of the weburl and the image.
 * @author liushenming
 */
public class PathTitleUnit {

	private String mPath="";
	private String mTitle="";
	
	//xxx  "xxx"
	private static final Pattern pattern_path_title=Pattern.compile(".+[ ]+\".+\"[ ]*");
	//xxxxx
	private static final Pattern pattern_string_nospace=Pattern.compile("[^\\s]+");
	//"xx xx"
	private static final Pattern pattern_string_quot=Pattern.compile("\".*?\"");
		
	public PathTitleUnit(String path,String title){
		this.mPath=path;
		this.mTitle=title;
	}
	
	//get a String (xxx  "xxxx").
	public PathTitleUnit(String origin){
		if(isPathTitleUnit(origin)){
			Matcher matcher_nospace=pattern_string_nospace.matcher(origin);
			Matcher matcher_quot=pattern_string_quot.matcher(origin);
			//the first no space sequence as mPath.
			if(matcher_nospace.find()){
				mPath=matcher_nospace.group();
			}
			//the last quotation sequence as the mTitle.
			while(matcher_quot.find()){
				mTitle=matcher_quot.group();
				mTitle=mTitle.substring(1, mTitle.length()-1);
			}
		}else{
			mPath=origin;
			mTitle="";
		}
		
	}
	
	//judge if a String is the type of pathtitle
	public static boolean isPathTitleUnit(String string){
		Matcher matcher_pt=pattern_path_title.matcher(string);
		if(matcher_pt.matches()){
			return true;
		}
		return false;
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public String getPath(){
		return mPath;
	}
}
