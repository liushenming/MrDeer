package com.liushenming.mrdeer.translatemodule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * ר������html�ļ�
 */
public class HtmlGenerator {
	
	private String head;	//<body>���ϵĲ���
	private String foot;	//</body>���µĲ���
	private String body;	//<body></body>�м�Ĳ���
	
	public HtmlGenerator(){
		head="<!DOCTYPE html>\n<html><head>\n<meta http-equiv=\"Content-"
				+ "Type\" content=\"text/html; charset=gb2312\">\n<title>markdown"
				+ "</title>\n</head>";
		foot="</html>";
		body="<body></body>";
	}
	
	//��html�ļ�д��dirָ����·����
	public boolean writeFile(String dir,String filename,int mode){
		File path=new File(dir);
		//�ļ�·���Ѿ����ڵ�����£����ܴ����ļ�
		if(path.exists()&&path.isDirectory()){
			File file=new File(dir+filename);
			
			if(file.exists()){
				BufferedWriter bw=null;
				try {
					bw=new BufferedWriter(new FileWriter(file));
					bw.write(head+body+foot);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} finally {
					if(bw!=null){
						try {
							bw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return false;
						}
					}
				}
			}else{
				file.delete();
				BufferedWriter bw=null;
				try {
					bw=new BufferedWriter(new FileWriter(file));
					bw.write(head+body+foot);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} finally {
					if(bw!=null){
						try {
							bw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	//��ȡhtml�ļ�������
	public String getFileContent(){
		return head+body+foot;
	}
	
	//����html�ļ���body
	public void setHtmlBody(String content){
		this.body="<body>"+content+"</body>";
	}
}
