package com.mazerule.translatemodule;

import java.util.ArrayList;
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
			//一行一行文本划分到了al_LineString里
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
			//
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
