package com.mazerule.mrdeer.translatemodule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EasyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pattern pattern=Pattern.compile("\\s*>+.+");
		String s=" >>> ss";
		Matcher m=pattern.matcher(s);
		if(m.matches()){
			System.out.println("success");
		}else{
			System.out.println("fail");
		}
		
	}

}
