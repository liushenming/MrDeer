package com.liushenming.mrdeer.translatemodule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StringUtils {

	/**
	 * eliminate a sequence from start to end from String.
	 * for example:
	 * StringUtils.eliminate("abcdefg",1,3): return "aefg"
	 * 
	 * @param string:String to be dealt with.
	 * @param start:the start index to eliminate.
	 * @param end:the end index to eliminate.
	 * @return the new String after eliminate.
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
	 * add string_piece to string_board at the start index of string_board.
	 * for example: StringUtils.motage("abc","kk",1) :return "akkbc"
	 * 
	 * @param string_board:the board string.
	 * @param string_piece:the string to be added.
	 * @param start:the index to add in the string_board.
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
	 * replace the sequence from start to end in string_board by string_piece.
	 * @param string_board
	 * @param string_piece
	 * @param start
	 * @param end
	 * @return
	 */
	public static String replace(String string_board,String string_piece,
			int start,int end)
	{
		string_board=eliminate(string_board,start,end);
		string_board=montage(string_board, string_piece, start);
		return string_board;
	}
	
	/**
	 * get String from the specified path.
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
