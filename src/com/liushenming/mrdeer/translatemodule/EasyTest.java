package com.liushenming.mrdeer.translatemodule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.liushenming.mrdeer.utils.StringUtils;

public class EasyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//\\[.+?\\]
		Pattern pattern=Pattern.compile("\\B[^!]\\[.+?\\]");
		String s="This is ![google][1],this is [apple][2],and this is [ms][3].";
		Matcher m=pattern.matcher(s);
		/*if(m.find()){
			System.out.println("success");
			//System.out.println("start:"+m.start());
			//System.out.println("end:"+m.end());
		}else{
			System.out.println("fail");
		}*/
		/*while(m.find()){
			String s_get=m.group();
			System.out.println(s_get);
		}*/
		
		String a="abcdefg";
		String b="kk";
		String c=StringUtils.eliminate(a,3,7);
		System.out.println(c);
	}

}
