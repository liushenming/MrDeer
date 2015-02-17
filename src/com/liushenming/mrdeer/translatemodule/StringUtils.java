package com.liushenming.mrdeer.translatemodule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StringUtils {

	/**
	 * ��һ��String���޳�start-end��һ���ַ���
	 * for example:
	 * StringUtils.eliminate("abcdefg",1,3): return "aefg"
	 * @param string:��Ҫ������ַ���
	 * @param start:Ҫ�޳����ֵ���ʼ�±�
	 * @param end:Ҫ�޳����ֵĽ����±�
	 * @return �޳�����ַ���
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
	 * �����ַ������ȼ������ٲ��룬��ƴ��
	 * for example: StringUtils.motage("abc","kk",1) :return "akkbc"
	 * @param string_board:ƴ���ڸ��ַ�����
	 * @param string_piece:����Ҫƴ����Ƭ
	 * @param start:��board�ĸô�����piece
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
	 * ��board��start-end���ı��滻��piece
	 * @param string_board
	 * @param string_piece
	 * @param start
	 * @param end
	 * @return
	 */
	public static String replace(String string_board,String string_piece,
			int start,int end)
	{
		//���޳�
		string_board=eliminate(string_board,start,end);
		//�ټ���
		string_board=montage(string_board, string_piece, start);
		return string_board;
	}
	
	/**
	 * ��ָ����path�е��ļ��ж�ȡString
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
