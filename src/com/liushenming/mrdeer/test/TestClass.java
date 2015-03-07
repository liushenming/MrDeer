package com.liushenming.mrdeer.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.liushenming.mrdeer.translatemodule.HtmlGenerator;
import com.liushenming.mrdeer.translatemodule.M2HTranslator;
import com.liushenming.mrdeer.translatemodule.StringUtils;

/*
 * 这是一个测试类
 */
public class TestClass {

	static boolean DBG=false;	//用于一些输出一些log
	static boolean VDBG=true;	//用于一些输出一些细节的log
	static boolean VVDBG=true;	//用于一些输出一些非常细节的log
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filename="E:\\MDFiles\\html1.txt";
		String mdString;
		try {
			mdString = StringUtils.getStringFromFile(filename);
			M2HTranslator translator=new M2HTranslator(mdString);
			String string_html=translator.translate();
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
			if(VVDBG){
				System.out.println("titles:");
				List<String> titles=translator.getTitles();
				if(titles!=null){
					Iterator<String> iter=titles.iterator();
					while(iter.hasNext()){
						System.out.println(iter.next());
						
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	} 
}
