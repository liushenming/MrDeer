package com.liushenming.mrdeer.translatemodule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StringUtils {

	/**
	 * 从一个String中剔除start-end的一段字符串
	 * for example:
	 * StringUtils.eliminate("abcdefg",1,3): return "aefg"
	 * @param string:需要处理的字符串
	 * @param start:要剔除部分的起始下标
	 * @param end:要剔除部分的结束下标
	 * @return 剔除后的字符串
	 */
	public static String eliminate(String string,int start,int end)
	{
		if(string==null){
			return null;
		}
		String head="";
		String foot="";
		if(start<0){
			start=0;
		}
		if(end>=string.length()){
			end=string.length()-1;
		}
		if(start<string.length()){
			head=string.substring(0, start);
		}
		if(end<string.length()){
			foot=string.substring(end+1);
		}
		return head+foot;
	}
	
	/**
	 * 剪接字符串，先剪开，再插入，再拼接
	 * for example: StringUtils.motage("abc","kk",1) :return "akkbc"
	 * @param string_board:拼接在该字符串上
	 * @param string_piece:这是要拼的碎片
	 * @param start:在board的该处插入piece
	 * @return
	 */
	public static String montage(String string_board,String string_piece,
			int start){
		if(start>=string_board.length()){
			return string_board+string_piece;
		}
		if(start<0){
			return string_piece+string_board;
		}
		String head=string_board.substring(0, start);
		String foot=string_board.substring(start);
		return head+string_piece+foot;
	}
	
	/**
	 * 将board中start-end的文本替换成piece
	 * @param string_board
	 * @param string_piece
	 * @param start
	 * @param end
	 * @return
	 */
	public static String replace(String string_board,String string_piece,
			int start,int end)
	{
		//先剔除
		string_board=eliminate(string_board,start,end);
		//再剪接
		string_board=montage(string_board, string_piece, start);
		return string_board;
	}
	
	/**
	 * 从指定的path中的文件中读取String
	 */
	public static String getStringFromFile(String path) throws IOException{
		BufferedReader br=new BufferedReader(new FileReader(path));
		StringBuilder sb=new StringBuilder();
		String string_get;
		while((string_get=br.readLine())!=null){
			sb.append(string_get+"\n");
		}
		br.close();
		return sb.toString();
	}
}
