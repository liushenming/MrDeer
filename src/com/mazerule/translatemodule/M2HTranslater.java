package com.mazerule.translatemodule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

/*
 * Markdown文本到Html文本的转换器
 */
public class M2HTranslater {

	//通过构造器传进来的原始字符串
	private String origin_string;
	
	//<行号,行字符串>
	private ArrayList<LineText> al_LineString;	//行文本
	public boolean DBG=true;
	public boolean VDBG=true;
	public boolean VVDBG=false;
	
	/*
	 * 文本的类型
	 */
	private final static int TEXTTYPE_NORMAL=10;  //普通文本类型
	private final static int TEXTYPE_SPACELINE=11;	//空白的文本类型
	private final static int TEXTTYPE_TITLELINE=12;	//=====
	private final static int TEXTTYPE_SBLINE_CONTINUOUS=13;//---连续的 
	private final static int TEXTTYPE_SBLINE_DISCONTINUOUS=14;	//- --，包含空格和-
	private final static int TEXTTYPE_ENDNORMALTEXT=15;	//最后是两空格的普通文本类型
	private final static int TEXTTYPE_STARLINE=16;	//** *，可以有空格\
	
	//用于内部类LineText判断文本类型用
	static final Pattern pattern_spaceline=Pattern.compile("\\s*");
	static final Pattern pattern_titleline=Pattern.compile("={3,}");
	static final Pattern pattern_sbline_continuous=Pattern.compile("-{3}");
	static final Pattern pattern_sbline_discontinuous=Pattern.compile("(\\s*-\\s*){3,}");
	//static final Pattern pattern_endnormaltext=Pattern.compile("");
	static final Pattern pattern_starline=Pattern.compile("(\\s*\\*\\s*){3,}");
	
	//行文本类
	class LineText{
		String content;	//文本内容
		int type;	//行文本的类型
		
		//创建时只能传入文本内容。
		LineText(String c){
			content=c;
			type=judgeType(content);
			if(DBG){
				System.out.println("content:"+content+"\ntype:"+type+"\n");
			}
		}
		
		//判断出文本行的类型
		//
		int judgeType(String content){
			//用正则表达式判断文本行属于什么类型的
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
			
			//TEXTTYPE_ENDNORMALTEXT暂时用排除法判断
			else if(content.length()>2&&content.charAt(content.length()-1)==' '
					&&content.charAt(content.length()-2)==' '){
				return TEXTTYPE_ENDNORMALTEXT;
			}
			return TEXTTYPE_NORMAL;
		}	
	}
	
	/*
	 * 元素的类型
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
	
	
	//在处理每一个行时，栈中每一格的元素类
	class StackElement{
		int type;	//他是属于操作符还是文本
		String content;	//具体的内容
		StackElement(int t,String c){
			type=t;
			content=c;
		}
	}
	
	//构造器
	public M2HTranslater(String string){
		if(string!=null){
			//另开辟了一个字符串对象，防止原来的字符串有其他用
			this.origin_string=new String(string);	
			if(VVDBG){
				System.out.println("M2HTranslater(String string),origin_string=="+origin_string);
			}
		}
		else{
			this.origin_string="";	
		}
	}
	
	//将origin_string切割，分到map_string中
	private boolean splitString(){
		//把map_string创建出来，如果已经存在了，就清空
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
			//一行一行文本划分到了al_LineString里，同时判断出类型
			for(int i=0;i<string_arr.length;i++){
				if(DBG){
					System.out.println("splitString!");
				}
				al_LineString.add(new LineText(string_arr[i]));
			}
			return true;
		}
	}
	
	//构造了M2HTranslate对象以后调用translate()进行转换
	public String translate(){
		//要返回的字符串
		String string_html="";	
		//先把原始文本给切割了
		if(splitString()){
			//以行为单位对文本进行处理
			//先对每一行添加html标签，然后再对段落加上<p>标签
			
			//直接用一个LinkedList作为栈
			//push操作：addFirst()
			//pop操作：removeFirst()
			//peek操作：getFirst()
			LinkedList<StackElement> magic_stack=new LinkedList<StackElement>();
			Iterator<LineText> iterator=al_LineString.iterator();
			if(VDBG){
				System.out.println("遍历al_LineString:");
			}
			while(iterator.hasNext()){
				LineText linetext=iterator.next();
				if(VDBG){
					System.out.println("next:"+linetext.content);
				}
				String textstring=linetext.content;	//获取了行文本的内容
				int lineindex=0;
				magic_stack.clear();
				//从头至尾扫描字符
				while(lineindex<textstring.length()){
					char letter=textstring.charAt(lineindex);
					//开始压栈
					
					lineindex++;
				}
			}
		}
		else{	//划分失败
			return "";
		}
		return string_html;
	}
	
	//为转换器重新加载一个要转换的html String
	public void loadString(String string){
		if(string==null){
			this.origin_string="";
		}
		else{
			this.origin_string=new String(string);
		}
	}
}
