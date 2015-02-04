package com.liushenming.mrdeer.translatemodule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * 专门生成html文件
 */
public class HtmlGenerator {
	
	private String head;	//<body>以上的部分
	private String foot;	//</body>以下的部分
	private String body;	//<body></body>中间的部分
	
	public HtmlGenerator(){
		head="<!DOCTYPE html>\n<html><head>\n<meta http-equiv=\"Content-"
				+ "Type\" content=\"text/html; charset=gb2312\">\n<title>markdown"
				+ "</title>\n</head>";
		foot="</html>";
		body="<body></body>";
	}
	
	//将html文件写到dir指定的路径下
	public boolean writeFile(String dir,String filename,int mode){
		File path=new File(dir);
		//文件路径已经存在的情况下，才能创建文件
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
	
	//获取html文件的内容
	public String getFileContent(){
		return head+body+foot;
	}
	
	//设置html文件的body
	public void setHtmlBody(String content){
		this.body="<body>"+content+"</body>";
	}
}
