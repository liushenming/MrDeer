package com.liushenming.mrdeer.translatemodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/*
 * ����һ��������
 */
public class TestClass {

	static boolean DBG=false;	//����һЩ���һЩlog
	static boolean VDBG=true;	//����һЩ���һЩϸ�ڵ�log
	static boolean VVDBG=false;	//����һЩ���һЩ�ǳ�ϸ�ڵ�log
	
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
			//��ʱmarkdownԭʼ�ı������Ѿ���string_get����
			//���н���
			M2HTranslater translater=new M2HTranslater(sb.toString());
			//H2EntityTranslater translater=new H2EntityTranslater(sb.toString());
			String string_html=translater.translate();
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
