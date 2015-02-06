package com.liushenming.mrdeer.utils;

public class StringUtils {

	/**
	 * 从一个String中剔除start-end的一段字符串
	 * @param string:需要处理的字符串
	 * @param start:要剔除部分的起始下标
	 * @param end:要剔除部分的结束下标
	 * @return 剔除后的字符串
	 */
	public static String eliminate(String string,int start,int end)
	{
		String head=string.substring(0, start);
		String foot="";
		if(end<string.length()){
			foot=string.substring(end+1);
		}
		
		return head+foot;
	}
	
	/**
	 * 剪接字符串，先剪开，再插入，再拼接
	 * @param string_board:拼接在该字符串上
	 * @param string_piece:这是要拼的碎片
	 * @param start:在board的该处插入piece
	 * @return
	 */
	public static String montage(String string_board,String string_piece,
			int start){
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
	
}
