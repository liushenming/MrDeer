package com.liushenming.mrdeer.translatemodule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * xxx  "xxx"的单元
 */
public class PathTitleUnit {

	private String mPath="";
	private String mTitle="";
	
	//xxx  "xxx"
	private static final Pattern pattern_path_title=Pattern.compile(".+[ ]+\".+\"");
	//xxxxx
	private static final Pattern pattern_string_nospace=Pattern.compile("[^\\s]+");
	//"xx xx"
	private static final Pattern pattern_string_quot=Pattern.compile("\".*\"");
		
	public PathTitleUnit(String path,String title){
		this.mPath=path;
		this.mTitle=title;
	}
	
	//传进来一个xxx  "xxxx"
	public PathTitleUnit(String origin){
		if(isPathTitleUnit(origin)){
			Matcher matcher_nospace=pattern_string_nospace.matcher(origin);
			Matcher matcher_quot=pattern_string_quot.matcher(origin);
			//找第一个匹配成功的nospace作为mPath
			if(matcher_nospace.find()){
				mPath=matcher_nospace.group();
			}
			//找最后一个匹配成功的quot
			while(matcher_quot.find()){
				mTitle=matcher_quot.group();
				mTitle=mTitle.substring(1, mTitle.length()-1);
			}
		}else{
			mPath=origin;
			mTitle="";
		}
		
	}
	
	//判断一个字符串是否是pathtitle类型的
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
