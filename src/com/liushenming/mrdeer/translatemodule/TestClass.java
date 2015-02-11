package com.liushenming.mrdeer.translatemodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/*
 * 这是一个测试类
 */
public class TestClass {

	static boolean DBG=false;	//用于一些输出一些log
	static boolean VDBG=true;	//用于一些输出一些细节的log
	static boolean VVDBG=false;	//用于一些输出一些非常细节的log
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filename="E:\\MDFiles\\xueguo.txt";
		//String filename="E:\\MDFiles\\hgfile.html";
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			StringBuilder sb=new StringBuilder();
			//System.out.print("sb length="+sb.length());
			String string_get;
			while((string_get=br.readLine())!=null){
				sb.append(string_get+"\n");
			}
			if(VDBG){
				System.out.println("main(),sb:\n"+sb.toString());
			}
			//此时markdown原始文本内容已经在string_get中了
			//进行解析
			M2HTranslater translater=new M2HTranslater(sb.toString());
			//H2EntityTranslater translater=new H2EntityTranslater(sb.toString());
			String string_html=translater.translate();
			if(VDBG)
			{
				System.out.println("main(),string_html:\n"+string_html);
			}
			//将string_html写入.html文件中
			HtmlGenerator hg=new HtmlGenerator();
			hg.setHtmlBody(string_html);
			if(hg.writeFile("E:\\MDFiles\\", "hgfile.html", 0)){
				if(VDBG){
					System.out.println("html文件生成成功");
				}
			}else{
				if(VDBG){
					System.out.println("html文件生成失败");
				}
			}			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
