package com.mazerule.mrdeer.translatemodule;

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
	static boolean VDBG=false;	//����һЩ���һЩϸ�ڵ�log
	static boolean VVDBG=false;	//����һЩ���һЩ�ǳ�ϸ�ڵ�log
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filename="E:\\MDFiles\\html1.txt";
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			StringBuilder sb=new StringBuilder();
			//System.out.print("sb length="+sb.length());
			String string_get;
			while((string_get=br.readLine())!=null){
				sb.append(string_get+"\n");
			}
			if(DBG){
				System.out.println("main(),sb=="+sb.toString());
			}
			//��ʱmarkdownԭʼ�ı������Ѿ���string_get����
			//���н���
			M2HTranslater translater=new M2HTranslater(sb.toString());
			String string_html=translater.translate();
			
			if(DBG)
			{
				
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
