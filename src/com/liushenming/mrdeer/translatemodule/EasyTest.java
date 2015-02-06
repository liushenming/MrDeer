package com.liushenming.mrdeer.translatemodule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.liushenming.mrdeer.utils.StringUtils;

public class EasyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pattern pattern=Pattern.compile("\\[[^\\[]+\\]\\[.+\\]");
		String s="[][][][][][]";
		Matcher m=pattern.matcher(s);
		if(m.find()){
			System.out.println("success");
			//System.out.println("start:"+m.start());
			//System.out.println("end:"+m.end());
		}else{
			System.out.println("fail");
		}
		
		/*String a="1234567";
		String b="";
		String c=StringUtils.eliminate(a,6,6);
		System.out.println(c);*/
	}

}
