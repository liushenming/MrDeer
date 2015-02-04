package com.liushenming.mrdeer.translatemodule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EasyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pattern pattern=Pattern.compile("!\\[.+\\]\\(.+ \".+\"\\)");
		//Pattern pattern=Pattern.compile("\"+");
		String s="![alt text](/path/to/img.jpg \"Title\")";
		Matcher m=pattern.matcher(s);
		if(m.matches()){
			System.out.println("success");
		}else{
			System.out.println("fail");
		}
		
	}

}
