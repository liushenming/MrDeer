package com.liushenming.mrdeer.translatemodule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.liushenming.mrdeer.utils.StringUtils;

public class EasyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*Pattern pattern=Pattern.compile("!\\[.+\\]\\(.+ \".+\"\\)");
		String s="![alt text](/path/to/img.jpg \"Title\")";
		Matcher m=pattern.matcher(s);
		if(m.matches()){
			System.out.println("success");
		}else{
			System.out.println("fail");
		}*/
		
		String a="abcdefg";
		String b="rr";
		String c=StringUtils.replace(a, b, 1, 3);
		System.out.println("c:"+c);
		
	}

}
