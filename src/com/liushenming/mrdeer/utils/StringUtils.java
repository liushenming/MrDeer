package com.liushenming.mrdeer.utils;

public class StringUtils {

	/**
	 * ��һ��String���޳�start-end��һ���ַ���
	 * @param string:��Ҫ������ַ���
	 * @param start:Ҫ�޳����ֵ���ʼ�±�
	 * @param end:Ҫ�޳����ֵĽ����±�
	 * @return �޳�����ַ���
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
	 * �����ַ������ȼ������ٲ��룬��ƴ��
	 * @param string_board:ƴ���ڸ��ַ�����
	 * @param string_piece:����Ҫƴ����Ƭ
	 * @param start:��board�ĸô�����piece
	 * @return
	 */
	public static String montage(String string_board,String string_piece,
			int start){
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
	
}
