package com.mazerule.translatemodule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

/*
 * Markdown�ı���Html�ı���ת����
 */
public class M2HTranslater {

	//ͨ����������������ԭʼ�ַ���
	private String origin_string;
	
	//<�к�,���ַ���>
	private ArrayList<LineText> al_LineString;	//���ı�
	public boolean DBG=true;
	public boolean VDBG=true;
	public boolean VVDBG=false;
	
	/*
	 * �ı�������
	 */
	private final static int TEXTTYPE_NORMAL=10;  //��ͨ�ı�����
	private final static int TEXTYPE_SPACELINE=11;	//�հ׵��ı�����
	private final static int TEXTTYPE_TITLELINE=12;	//=====
	private final static int TEXTTYPE_SBLINE_CONTINUOUS=13;//---������ 
	private final static int TEXTTYPE_SBLINE_DISCONTINUOUS=14;	//- --�������ո��-
	private final static int TEXTTYPE_ENDNORMALTEXT=15;	//��������ո����ͨ�ı�����
	private final static int TEXTTYPE_STARLINE=16;	//** *�������пո�\
	
	//�����ڲ���LineText�ж��ı�������
	static final Pattern pattern_spaceline=Pattern.compile("\\s*");
	static final Pattern pattern_titleline=Pattern.compile("={3,}");
	static final Pattern pattern_sbline_continuous=Pattern.compile("-{3}");
	static final Pattern pattern_sbline_discontinuous=Pattern.compile("(\\s*-\\s*){3,}");
	//static final Pattern pattern_endnormaltext=Pattern.compile("");
	static final Pattern pattern_starline=Pattern.compile("(\\s*\\*\\s*){3,}");
	
	//���ı���
	class LineText{
		String content;	//�ı�����
		int type;	//���ı�������
		
		//����ʱֻ�ܴ����ı����ݡ�
		LineText(String c){
			content=c;
			type=judgeType(content);
			if(DBG){
				System.out.println("content:"+content+"\ntype:"+type+"\n");
			}
		}
		
		//�жϳ��ı��е�����
		//
		int judgeType(String content){
			//��������ʽ�ж��ı�������ʲô���͵�
			if(pattern_spaceline.matcher(content).matches()){
				return TEXTYPE_SPACELINE;
			}else if(pattern_titleline.matcher(content).matches()){
				return TEXTTYPE_TITLELINE;
			}else if(pattern_sbline_continuous.matcher(content).matches()){
				return TEXTTYPE_SBLINE_CONTINUOUS;
			}else if(pattern_sbline_discontinuous.matcher(content).matches()){
				return TEXTTYPE_SBLINE_DISCONTINUOUS;
			}else if(pattern_starline.matcher(content).matches()){
				return TEXTTYPE_STARLINE;
			}
			
			//TEXTTYPE_ENDNORMALTEXT��ʱ���ų����ж�
			else if(content.length()>2&&content.charAt(content.length()-1)==' '
					&&content.charAt(content.length()-2)==' '){
				return TEXTTYPE_ENDNORMALTEXT;
			}
			return TEXTTYPE_NORMAL;
		}	
	}
	
	/*
	 * Ԫ�ص�����
	 */
	private final static int ELEMENTTYPE_TEXT=0;
	private final static int ELEMENTTYPE_SPACE=0;
	private final static int ELEMENTTYPE_OP_TAB=0;
	private final static int ELEMENTTYPE_OP_STAR=0; 	//*
	private final static int ELEMENTTYPE_OP_2STAR=0;	//**
	private final static int ELEMENTTYPE_OP_SB=0; 		//_
	private final static int ELEMENTTYPE_OP_2SB=0;		//__
	private final static int ELEMENTTYPE_OP_JING=0;		//#
	private final static int ELEMENTTYPE_OP_2JING=0;	//##
	private final static int ELEMENTTYPE_OP_3JING=0;	//###
	private final static int ELEMENTTYPE_OP_4JING=0;	//####
	private final static int ELEMENTTYPE_OP_5JING=0;	//#####
	private final static int ELEMENTTYPE_OP_6JING=0;	//######
	private final static int ELEMENTTYPE_OP_ANGLE=0;	//>
	//........
	
	
	//�ڴ���ÿһ����ʱ��ջ��ÿһ���Ԫ����
	class StackElement{
		int type;	//�������ڲ����������ı�
		String content;	//���������
		StackElement(int t,String c){
			type=t;
			content=c;
		}
	}
	
	//������
	public M2HTranslater(String string){
		if(string!=null){
			//������һ���ַ������󣬷�ֹԭ�����ַ�����������
			this.origin_string=new String(string);	
			if(VVDBG){
				System.out.println("M2HTranslater(String string),origin_string=="+origin_string);
			}
		}
		else{
			this.origin_string="";	
		}
	}
	
	//��origin_string�и�ֵ�map_string��
	private boolean splitString(){
		//��map_string��������������Ѿ������ˣ������
		if(al_LineString==null){
			al_LineString=new ArrayList<LineText>();
		}
		else{
			al_LineString.clear();
		}
		if(origin_string==null){
			return false;
		}
		else{
			String[] string_arr=origin_string.split("\n");
			if(VDBG){
				System.out.println("splitString(),string_arr.length()=="+string_arr.length);
			}
			//һ��һ���ı����ֵ���al_LineString�ͬʱ�жϳ�����
			for(int i=0;i<string_arr.length;i++){
				if(DBG){
					System.out.println("splitString!");
				}
				al_LineString.add(new LineText(string_arr[i]));
			}
			return true;
		}
	}
	
	//������M2HTranslate�����Ժ����translate()����ת��
	public String translate(){
		//Ҫ���ص��ַ���
		String string_html="";	
		//�Ȱ�ԭʼ�ı����и���
		if(splitString()){
			//����Ϊ��λ���ı����д���
			//�ȶ�ÿһ�����html��ǩ��Ȼ���ٶԶ������<p>��ǩ
			
			//ֱ����һ��LinkedList��Ϊջ
			//push������addFirst()
			//pop������removeFirst()
			//peek������getFirst()
			LinkedList<StackElement> magic_stack=new LinkedList<StackElement>();
			Iterator<LineText> iterator=al_LineString.iterator();
			if(VDBG){
				System.out.println("����al_LineString:");
			}
			while(iterator.hasNext()){
				LineText linetext=iterator.next();
				if(VDBG){
					System.out.println("next:"+linetext.content);
				}
				String textstring=linetext.content;	//��ȡ�����ı�������
				int lineindex=0;
				magic_stack.clear();
				//��ͷ��βɨ���ַ�
				while(lineindex<textstring.length()){
					char letter=textstring.charAt(lineindex);
					//��ʼѹջ
					
					lineindex++;
				}
			}
		}
		else{	//����ʧ��
			return "";
		}
		return string_html;
	}
	
	//Ϊת�������¼���һ��Ҫת����html String
	public void loadString(String string){
		if(string==null){
			this.origin_string="";
		}
		else{
			this.origin_string=new String(string);
		}
	}
}
