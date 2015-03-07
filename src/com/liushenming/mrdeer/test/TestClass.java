package com.liushenming.mrdeer.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.liushenming.mrdeer.translatemodule.HtmlGenerator;
import com.liushenming.mrdeer.translatemodule.M2HTranslator;
import com.liushenming.mrdeer.translatemodule.StringUtils;

/*
 * ����һ��������
 */
public class TestClass {

	static boolean DBG=false;	//����һЩ���һЩlog
	static boolean VDBG=true;	//����һЩ���һЩϸ�ڵ�log
	static boolean VVDBG=true;	//����һЩ���һЩ�ǳ�ϸ�ڵ�log
	
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
			//��string_htmlд��.html�ļ���
			HtmlGenerator hg=new HtmlGenerator();
			hg.setHtmlBody(string_html);
			if(hg.writeFile("E:\\MDFiles\\", "hgfile.html", 0)){
				if(VDBG){
					System.out.println("html�ļ����ɳɹ�");
				}
			}else{
				if(VDBG){
					System.out.println("html�ļ�����ʧ��");
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
