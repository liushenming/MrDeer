package com.liushenming.mrdeer.translatemodule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * class to generate html file.
 */
public class HtmlGenerator {
	
	private String head;	//text before <body>.
	private String foot;	//text after </body>.
	private String body;	//text between <body> and </body>.
	
	public HtmlGenerator(){
		head="<!DOCTYPE html>\n<html><head>\n<meta http-equiv=\"Content-"
				+ "Type\" content=\"text/html; charset=gb2312\">\n<title>markdown"
				+ "</title>\n</head>";
		foot="</html>";
		body="<body></body>";
	}
	
	/**
	 * write the html file to dir/filename.
	 * @param dir
	 * @param filename
	 * @param mode
	 * @return
	 */
	public boolean writeFile(String dir,String filename,int mode){
		File path=new File(dir);
		if(path.exists()&&path.isDirectory()){
			//the path exists.
			//new a file using the filename at the path.
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
	
	/**
	 * get the content of the html.
	 * @return content
	 */
	public String getFileContent(){
		return head+body+foot;
	}
	
	/**
	 * set the html body.
	 * @param content
	 */
	public void setHtmlBody(String content){
		this.body="<body>"+content+"</body>";
	}
}
