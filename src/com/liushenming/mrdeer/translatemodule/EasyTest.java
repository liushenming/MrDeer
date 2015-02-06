package com.liushenming.mrdeer.translatemodule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.liushenming.mrdeer.utils.StringUtils;

public class EasyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*Pattern pattern=Pattern.compile("\\w");
		String s="abcdefg";
		Matcher m=pattern.matcher(s);
		if(m.matches()){
			System.out.println("success");
		}else{
			System.out.println("fail");
		}*/
		
		String a="1234567";
		String b="";
		String c=StringUtils.eliminate(a, 1, 2);
		System.out.println(c);
	}

}
