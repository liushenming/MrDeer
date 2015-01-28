package com.mazerule.mrdeer.translatemodule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
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
	private final static int ELEMENTTYPE_UNKNOW=0;
	private final static int ELEMENTTYPE_TEXT=1;
	private final static int ELEMENTTYPE_SPACE=2;
	private final static int ELEMENTTYPE_OP_TAB=3;
	private final static int ELEMENTTYPE_OP_STAR=4; 	//*
	private final static int ELEMENTTYPE_OP_2STAR=5;	//**
	private final static int ELEMENTTYPE_OP_SB=6; 		//_
	private final static int ELEMENTTYPE_OP_2SB=7;		//__
	private final static int ELEMENTTYPE_OP_JING=8;		//#
	private final static int ELEMENTTYPE_OP_2JING=9;	//##
	private final static int ELEMENTTYPE_OP_3JING=10;	//###
	private final static int ELEMENTTYPE_OP_4JING=11;	//####
	private final static int ELEMENTTYPE_OP_5JING=12;	//#####
	private final static int ELEMENTTYPE_OP_6JING=13;	//######
	private final static int ELEMENTTYPE_OP_ANGLE=14;	//>
	//........
	
	
	//�ڴ���ÿһ����ʱ��ջ��ÿһ���Ԫ����
	class StackElement{
		int type;	//�������ڲ����������ı�
		String content;	//���������
		StackElement(int t,String c){
			type=t;
			content=c;
		}
		
		//��ɨ�赽һ���ַ�ʱֱ�Ӵ���һ��StackElement�������ڹ��캯�����ж�����
		StackElement(char c){
			content=c+"";
			type=judgeType(c);
		}
		
		//�ж�һ���ַ���ʲô����
		int judgeType(char c)
		{
			int t=ELEMENTTYPE_UNKNOW;
			switch(c){
			case ' ':
				t=ELEMENTTYPE_SPACE;
				break;
			case '	':
				t=ELEMENTTYPE_OP_TAB;
				break;
			case '*':
				t=ELEMENTTYPE_OP_STAR;
				break;
			case '_':
				t=ELEMENTTYPE_OP_SB;
				break;
			case '#':
				t=ELEMENTTYPE_OP_JING;
				break;
			default:
				t=ELEMENTTYPE_TEXT;
				break;	
			}
			return t;
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
				//ֻ��NORMAL��ENDNORMALTEXT���͵��ı��ſ��Խ���stack���д���
				if(linetext.type!=TEXTTYPE_NORMAL
						&&linetext.type!=TEXTTYPE_ENDNORMALTEXT){
					continue;
				}
				String textstring=linetext.content;	//��ȡ�����ı�������
				int lineindex=0;
				magic_stack.clear();
				//��ͷ��βɨ���ַ�
				//step1:����ѹջ
				//step2:���հ�ջ����һ��
				while(lineindex<textstring.length()){
					char letter=textstring.charAt(lineindex);
					StackElement se_add=new StackElement(letter);
					StackElement se_top=magic_stack.peek();	//ջ��Ԫ��
					while(true){
						//ѹ�ո�ջԪ��
						if(se_add.type==ELEMENTTYPE_SPACE){
							if(se_top==null){
								//��û��Ԫ�أ�ֱ��ѹջ
								magic_stack.push(se_add);
								break;
							}
							if(se_top.type==ELEMENTTYPE_TEXT){
								if(se_top.content.charAt(se_top.content.length()-1)
										!=' '){
									//�������һ���ո�
									String newstring=se_top.content+" ";
									magic_stack.pop();
									se_add=new StackElement(ELEMENTTYPE_TEXT,newstring);
									se_top=magic_stack.peek();
									continue;
								}else{
									//������һ���ո�
									break;
								}
							}else if(se_top.type==ELEMENTTYPE_SPACE){
								break;
							}
						}
						//ѹ����
						else if(se_add.type==ELEMENTTYPE_TEXT){
							//ջ��Ҳ��TEXT����
							if(se_top!=null&&se_top.type==ELEMENTTYPE_TEXT){
								magic_stack.pop();
								String newtext=se_top.content+se_add.content;
								se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
								se_top=magic_stack.peek();
								continue;
							}else{
								//���������ֱ��ѹջ
								magic_stack.push(se_add);
								break;
							}
						}
						//ѹ'_'
						else if(se_add.type==ELEMENTTYPE_OP_SB){
							if(se_top==null){
								magic_stack.push(se_add);
								break;
							}
							if(se_top.type==ELEMENTTYPE_OP_SB){
								magic_stack.pop();
								se_add=new StackElement(ELEMENTTYPE_OP_2SB, "__");
								se_top=magic_stack.peek();
								continue;
							}else if(se_top.type==ELEMENTTYPE_TEXT&&magic_stack.size()>=2&&
									magic_stack.get(1).type==ELEMENTTYPE_OP_SB){
								//<em>text</em>
								magic_stack.pop();
								magic_stack.pop();
								String newtext="<em>"+se_top.content+"</em>";
								se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
								se_top=magic_stack.peek();
								continue;
							}else{
								magic_stack.push(se_add);
								break;
							}
						}
						//ѹ'__'
						else if(se_add.type==ELEMENTTYPE_OP_2SB){
							if(se_top==null){
								magic_stack.push(se_add);
								break;
							}
							if(se_top.type==ELEMENTTYPE_TEXT&&magic_stack.size()>=2&&
									magic_stack.get(1).type==ELEMENTTYPE_OP_2SB){
								//<strong>text</strong>
								magic_stack.pop();
								magic_stack.pop();
								String newtext="<strong>"+se_top.content+"</strong>";
								se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
								se_top=magic_stack.peek();
								continue;
							}else{
								magic_stack.add(se_add);
							}
						}
						//ѹ'*'
						else if(se_add.type==ELEMENTTYPE_OP_STAR){
							if(se_top==null){
								magic_stack.push(se_add);
								break;
							}
							if(se_top.type==ELEMENTTYPE_OP_STAR){
								magic_stack.pop();
								se_add=new StackElement(ELEMENTTYPE_OP_2STAR, "**");
								se_top=magic_stack.peek();
								continue;
							}else if(se_top.type==ELEMENTTYPE_TEXT&&magic_stack.size()>=2&&
									magic_stack.get(1).type==ELEMENTTYPE_OP_STAR){
								magic_stack.pop();
								magic_stack.pop();
								String newtext="<em>"+se_top.content+"</em>";
								se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
								se_top=magic_stack.peek();
								continue;
							}else{
								magic_stack.push(se_add);
								break;
							}
						}
						//ѹ'**'
						else if(se_add.type==ELEMENTTYPE_OP_2STAR){
							if(se_top==null){
								magic_stack.push(se_add);
								break;
							}
							if(se_top.type==ELEMENTTYPE_TEXT&&magic_stack.size()>=2&&
									magic_stack.get(1).type==ELEMENTTYPE_OP_2STAR){
								//<strong>text</strong>
								magic_stack.pop();
								magic_stack.pop();
								String newtext="<strong>"+se_top.content+"</strong>";
								se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
								se_top=magic_stack.peek();
								continue;
							}else{
								magic_stack.add(se_add);
							}
						}
						//ѹ'#'
						else if(se_add.type==ELEMENTTYPE_OP_JING){
							if(se_top==null){
								magic_stack.push(se_add);
								break;
							}
							if(se_top.type==ELEMENTTYPE_OP_JING){
								magic_stack.pop();
								magic_stack.push(new StackElement(
										ELEMENTTYPE_OP_2JING,"##"));
								break;
							}else if(se_top.type==ELEMENTTYPE_OP_2JING){
								magic_stack.pop();
								magic_stack.push(new StackElement(
										ELEMENTTYPE_OP_3JING,"###"));
								break;
							}else if(se_top.type==ELEMENTTYPE_OP_3JING){
								magic_stack.pop();
								magic_stack.push(new StackElement(
										ELEMENTTYPE_OP_4JING,"####"));
								break;
							}else if(se_top.type==ELEMENTTYPE_OP_4JING){
								magic_stack.pop();
								magic_stack.push(new StackElement(
										ELEMENTTYPE_OP_5JING,"#####"));
								break;
							}else if(se_top.type==ELEMENTTYPE_OP_5JING){
								magic_stack.pop();
								magic_stack.push(new StackElement(
										ELEMENTTYPE_OP_6JING,"######"));
								break;
							}else if(se_top.type==ELEMENTTYPE_OP_6JING){
								magic_stack.pop();
								magic_stack.push(new StackElement(
										ELEMENTTYPE_TEXT,"#"));
								break;
							}
						}
						//ѹ'##'
						else if(se_add.type==ELEMENTTYPE_OP_2JING){
							
						}
						//ѹ'###'
						else if(se_add.type==ELEMENTTYPE_OP_3JING){
							
						}
						//ѹ'####'
						else if(se_add.type==ELEMENTTYPE_OP_4JING){
							
						}
						//ѹ'#####'
						else if(se_add.type==ELEMENTTYPE_OP_5JING){
							
						}
						//ѹ'######'
						else if(se_add.type==ELEMENTTYPE_OP_6JING){
							
						}
					}
					
					
					lineindex++;
				}
				//��magic_stack�н����ַ���ȡ����
				String stackstring="";	//��ջ��ȡ���ļӹ���һ�ε��ı�
				ListIterator<StackElement> listiter=magic_stack.listIterator(
						magic_stack.size());
				while(listiter.hasPrevious()){
					StackElement se_get=listiter.previous();
					stackstring+=se_get.content;
				}
				if(VDBG){
					System.out.println("stackstring:"+stackstring);
				}
				//�ٷ��û�al_LineString
				linetext.content=stackstring;	//���ı��滻��magic_stack�����ӹ�����
			}
		}
		else{	//����ʧ��
			return "ERROR";
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
