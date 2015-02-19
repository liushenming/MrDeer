package com.liushenming.mrdeer.translatemodule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Translator that translate MarkDown(.md) into HTML(.html)
 */
public class M2HTranslator {

	//the original String get from constructor
	private String origin_string;
	
	//the origin_string will be stored in mLineString in some specified rules.
	private ArrayList<LineText> mLineString;	
	//mListTail stores the list tail(true:</ol>,false:</ul>).
	private LinkedList<Boolean> mListTail;
	//mEscapeCharSet stores all the (Escape Sequence).
	private HashSet<Character> mEscapeCharSet;
	//mReferDefineMap stores all the (Reference Define) information into Map<id,(path,title)>
	private HashMap<String,PathTitleUnit> mReferDefineMap;
	
	public boolean DBG=false;
	public boolean VDBG=false;
	public boolean VVDBG=true;
	
	/*
	 * below are the text types of String stored in mLineString 
	 */
	//normal
	private final static int TEXTTYPE_NORMAL=10;  
	//space line 
	private final static int TEXTYPE_SPACELINE=11;	
	//title-format line:=====
	private final static int TEXTTYPE_TITLELINE=12;	
	//title line titled by "##"
	private final static int TEXTTYPE_TITLE_JING=13;
	//title line titled by "==="
	private final static int TEXTTYPE_TITLE_EQUAL=14;	
	//consecutive "-":------
	private final static int TEXTTYPE_SBLINE_CONSECUTIVE=15;
	//discontinuous "-":- - - --
	private final static int TEXTTYPE_SBLINE_DISCONTINUOUS=16;
	//normal text ends with two space
	private final static int TEXTTYPE_ENDNORMALTEXT=17;
	//star line:* *  **
	private final static int TEXTTYPE_STARLINE=18;
	//text line in a list
	private final static int TEXTTYPE_LIST=19;
	
	/*
	 * Patterns use for recognizing the text type 
	 * and finding specified substrings in a string
	 */
	//
	static final Pattern pattern_spaceline=Pattern.compile("\\s*");
	//====
	static final Pattern pattern_titleline=Pattern.compile("={3,}");
	//###xx xx
	static final Pattern pattern_title_jing=Pattern.compile("[#]{2,}.*");
	//---
	static final Pattern pattern_sbline_consecutive=Pattern.compile("-{3}");
	//- - - - -
	static final Pattern pattern_sbline_discontinuous=Pattern.compile("(\\s*-\\s*){3,}");
	//  *  *  **  **
	static final Pattern pattern_starline=Pattern.compile("(\\s*\\*\\s*){3,}");
	// >>>xxxx xxx
	static final Pattern pattern_blockquote=Pattern.compile("\\s*>+.+");
	// *xxxx    +xxxx    -xxxx
	// 1.xxxx
	static final Pattern pattern_list=Pattern.compile("\\s*([*+-]|[0-9]+\\.)\\s+.+");
	//![xxx](xxx "xxx")
	static final Pattern pattern_image=Pattern.compile("!\\[.+\\]\\(.+ \".+\"\\)");
	//[xxx](xxxxx)
	static final Pattern pattern_weburl=Pattern.compile("\\[.+\\]\\(.+\\)");
	//[xxx]
	static final Pattern pattern_squarebracket=Pattern.compile("\\[.+?\\]");
	//[xxx][xxx]
	static final Pattern pattern_squarebracket2=Pattern.compile("\\[.+?\\]\\[.+?\\]");
	//![xxx][xxx]
	static final Pattern pattern_squarebracket2_image=Pattern.compile("!\\[.+?\\]\\[.+?\\]");	
	//(xxxx)
	static final Pattern pattern_bracket=Pattern.compile("\\(.+?\\)");
	//[id]:xxx  "xxxx"
	static final Pattern pattern_referdefine=Pattern.compile("\\[.+?\\]:.+\\s+\".+?\"");
	//```xx```x```xx````
	static final Pattern pattern_code_greedy=Pattern.compile("[`]+.+[`]+");
	//````xxxx```
	static final Pattern pattern_code_scared=Pattern.compile("[`]+.+?[`]+");
	//`````
	static final Pattern pattern_code_op=Pattern.compile("[`]+");
	//\s\s\s\s or \t 
	static final Pattern pattern_codeline=Pattern.compile("^(\\t|[ ]{4}).*[^ \\t]+.*");
	
	//encapsulate the line String in mLineString
	private class LineText{
		String content;	//the content of the LineText
		int type;	//the type of the LineText
		int blockquote_depth;	//the depth of the (Block Quote)
		boolean isblockquote_start;//true if is the first line of the (Block Quote)
		boolean isblockquote_end;//true if is the line should be followed with </ul></ol>
		int list_len;	//the deep length in the list
		boolean islist_order;	//true if is in a <ol>,false if is in a <ul>
		boolean islist_head;	//true if is the first line of a new list(or sublist)
		int list_tailnum;	//number of the </ul>,</ol>
		/*
		 * a stack created if needed,and pop the true or false to add 
		 * </ol>,</ul> at the end of the line text.
		 */
		LinkedList<Boolean> list_tailstack;	
		boolean isCode;	//true if is in a <code>
		
		//create with the String content
		LineText(String _content){
			content=_content;
			//calculate the depth of the (Block Quote).
			blockquote_depth=calculateBlockquoteDepth();
			//judge the type of the line text (TEXTTYPE_).
			type=judgeType(content);
			/*
			 * calculate the parameters about the list 
			 * (list_len,islist_head,list_tailnum,islist_order)
			 */
			calculateListParam();
			//the line text is not the end of (Block Quote) at beginning.
			isblockquote_end=false;
			//the line text begins with 4 space or 1 tab so it's a code line
			if(pattern_codeline.matcher(_content).matches()){
				isCode=true;
			}else{
				isCode=false;
			}
		}
		
		/*
		 * calculate the parameters of the list
		 * (list_len,islist_head,list_tailnum,islist_order) 
		 * remove the list-format sequence: *,1.,+,- 
		 */
		void calculateListParam(){
			list_len=0;
			islist_head=false;
			list_tailnum=0;
			if(type!=TEXTTYPE_LIST){
				return;
			}
			int index=0;
			/*
			 * calculate the list_len's coarse value,it's just a original value
			 *  and will be trimmed in the SCAN#2.
			 */
			list_len=1;
			while(index<content.length()){
				if(content.charAt(index)==' '){
					//the ' ' at the beginning will be ignored
					list_len++;
				}else{
					if(content.charAt(index)=='*'||content.charAt(index)=='+'
							||content.charAt(index)=='-'){
						//if the line text begins with *,+,-,it isn't order list 
						islist_order=false;
					}else{
						//else the line text must begin with 10.,it's order list
						islist_order=true;
					}
					break;
				}
				index++;
			}
			//remove the *,+,-,100.
			while(index<content.length()){
				if(content.charAt(index)!=' '){
					index++;
				}else{
					break;
				}
			}
			content=content.substring(index);
		}
		
		//calculate the depth of the (Block Quote)
		int calculateBlockquoteDepth(){
			int quotecount=0;
			if(pattern_blockquote.matcher(content).matches()){
				//the line text is in format:>>> xxx
				/*
				 * the line text will be considered as the start of the (Block Quote)
				 * and the value of isblockquote_start will be trimmed next.  
				 */
				isblockquote_start=true;	
				int index=0;
				//scan the line text and get the count of '>'.
				while(index<content.length()){
					if(quotecount==0&&content.charAt(index)!='>'){
						//haven't found '>'.
						index++;
						continue;
					}
					if(content.charAt(index)=='>'){
						//find a '>',count and continue.
						quotecount++;
						index++;
						continue;
					}
					else{
						//first letter after >>> sequence.
						break;
					}
				}
				//remove the ">>>" sequence from the line text.
				content=content.substring(index);
			}else{
				//the line text is not begin with " >>>" 
				isblockquote_start=false;
			}
			return quotecount;
		}
		
		//judge the type of the line text (TEXTTYPE_).
		int judgeType(String content){
			/*
			 * judge by the Patterns defined above.
			 * the order of the judge progress cannot be upside down.
			 */
			if(pattern_spaceline.matcher(content).matches()){
				return TEXTYPE_SPACELINE;
			}else if(pattern_titleline.matcher(content).matches()){
				return TEXTTYPE_TITLELINE;
			}else if(pattern_sbline_consecutive.matcher(content).matches()){
				return TEXTTYPE_SBLINE_CONSECUTIVE;
			}else if(pattern_sbline_discontinuous.matcher(content).matches()){
				return TEXTTYPE_SBLINE_DISCONTINUOUS;
			}else if(pattern_starline.matcher(content).matches()){
				return TEXTTYPE_STARLINE;
			}else if(pattern_title_jing.matcher(content).matches()){
				return TEXTTYPE_TITLE_JING;
			}else if(pattern_list.matcher(content).matches()){
				return TEXTTYPE_LIST;
			}
			else if(content.length()>2&&content.charAt(content.length()-1)==' '
					&&content.charAt(content.length()-2)==' '){
				return TEXTTYPE_ENDNORMALTEXT;
			}
			return TEXTTYPE_NORMAL;
		}	
	}
	
	/*
	 * below are the type of the element in the magic_stack.
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
	private final static int ELEMENTTYPE_OP_BR=14;	//\n
	//below are the Escape Sequence,and they will also be push into the magic_stack
	private final static int ELEMENTTYPE_ESCAPECHAR_XIEGANG=100;	//'\\'
	private final static int ELEMENTTYPE_ESCAPECHAR_FANYING=101;	//'\`'
	private final static int ELEMENTTYPE_ESCAPECHAR_STAR=102;	//'\*'
	private final static int ELEMENTTYPE_ESCAPECHAR_SB=103;	//'\_'
	private final static int ELEMENTTYPE_ESCAPECHAR_LEFTHUA=104;	//'\{'
	private final static int ELEMENTTYPE_ESCAPECHAR_RIGHTHUA=105;	//'\}'
	private final static int ELEMENTTYPE_ESCAPECHAR_LEFTFANG=107;	//'\['
	private final static int ELEMENTTYPE_ESCAPECHAR_RIGHTFANG=108;	//'\]'
	private final static int ELEMENTTYPE_ESCAPECHAR_LEFTYUAN=109;	//'\('
	private final static int ELEMENTTYPE_ESCAPECHAR_RIGHTYUAN=110;	//'\)'
	private final static int ELEMENTTYPE_ESCAPECHAR_JING=111;	//'\#'
	private final static int ELEMENTTYPE_ESCAPECHAR_JIA=112;	//'\+'
	private final static int ELEMENTTYPE_ESCAPECHAR_JIAN=113;	//'\-'
	private final static int ELEMENTTYPE_ESCAPECHAR_DOT=114;	//'\.'
	private final static int ELEMENTTYPE_ESCAPECHAR_GANTAN=115;	//'\!'

	//package the element in the magic_stack
	private class StackElement{
		int type;	//the type of the element,(ELEMENTTYPE_).
		String content;	//the content of the element
		boolean isEscapeChar;	//true if is the escape char
		StackElement(int _type,String _content){
			type=_type;
			content=_content;
		}
		
		//create a StackElement and judge it's type then.
		StackElement(char _content,boolean _isescape){
			content=_content+"";
			isEscapeChar=_isescape;
			type=judgeType(_content, _isescape);
		}
		
		//judge the type the _char
		int judgeType(char _char,boolean _isescape)
		{
			int t=ELEMENTTYPE_UNKNOW;
			if(_isescape){
				//if escape char.
				switch(_char){
				case '\\':
					t=ELEMENTTYPE_ESCAPECHAR_XIEGANG;
					break;
				case '`':
					t=ELEMENTTYPE_ESCAPECHAR_FANYING;
					break;
				case '*':
					t=ELEMENTTYPE_ESCAPECHAR_STAR;
					break;
				case '_':
					t=ELEMENTTYPE_ESCAPECHAR_SB;
					break;
				case '{':
					t=ELEMENTTYPE_ESCAPECHAR_LEFTHUA;
					break;
				case '}':
					t=ELEMENTTYPE_ESCAPECHAR_RIGHTHUA;
					break;
				case '[':
					t=ELEMENTTYPE_ESCAPECHAR_LEFTFANG;
					break;
				case ']':
					t=ELEMENTTYPE_ESCAPECHAR_RIGHTFANG;
					break;
				case '(':
					t=ELEMENTTYPE_ESCAPECHAR_LEFTYUAN;
					break;
				case ')':
					t=ELEMENTTYPE_ESCAPECHAR_RIGHTYUAN;
					break;
				case '#':
					t=ELEMENTTYPE_ESCAPECHAR_JING;
					break;
				case '+':
					t=ELEMENTTYPE_ESCAPECHAR_JIA;
					break;
				case '-':
					t=ELEMENTTYPE_ESCAPECHAR_JIAN;
					break;
				case '.':
					t=ELEMENTTYPE_ESCAPECHAR_DOT;
					break;
				case '!':
					t=ELEMENTTYPE_ESCAPECHAR_GANTAN;
					break;
				default:
					t=ELEMENTTYPE_TEXT;
					break;
				}
			}
			else
			{
				//is not escape char.
				switch(_char){
				case ' ':
					t=ELEMENTTYPE_SPACE;
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
				case '\n':
					t=ELEMENTTYPE_OP_BR;
					break;
				case '\t':
					t=ELEMENTTYPE_OP_TAB;
					break;
				default:
					t=ELEMENTTYPE_TEXT;
					break;	
				}
			}
			return t;
		}
	}
	
	//the constructor of the M2HTranslator
	public M2HTranslator(String string){
		if(string!=null){
			//new a String.
			this.origin_string=new String(string);	
			if(VVDBG){
				System.out.println("M2HTranslater(String string),origin_string=="+origin_string);
			}
		}
		else{
			this.origin_string="";	
		}
		mListTail=new LinkedList<Boolean>();
		mReferDefineMap=new HashMap<String,PathTitleUnit>();
		//add all the escape chars into the mEscpaeCharSet.
		mEscapeCharSet=new HashSet<Character>();
		mEscapeCharSet.add('\\');
		mEscapeCharSet.add('`');
		mEscapeCharSet.add('*');
		mEscapeCharSet.add('_');
		mEscapeCharSet.add('{');
		mEscapeCharSet.add('}');
		mEscapeCharSet.add('[');
		mEscapeCharSet.add(']');
		mEscapeCharSet.add('(');
		mEscapeCharSet.add(')');
		mEscapeCharSet.add('#');
		mEscapeCharSet.add('+');
		mEscapeCharSet.add('-');
		mEscapeCharSet.add('.');
		mEscapeCharSet.add('!');
	}
	
	/*
	 * judge whether a char is escape or not.
	 * for example�� isTransChar(*)  return true;
	 * 		isTransChar(a)	return false;
	 */
	private boolean isEscapeChar(char c){
		if(mEscapeCharSet.contains(new Character(c))){
			return true;
		}
		return false;
	}
	
	//split the origin_string into mLineString.
	private boolean splitString(){
		//create the mLineString.
		if(mLineString==null){
			mLineString=new ArrayList<LineText>();
		}else{
			mLineString.clear();
		}
		
		if(origin_string==null){
			return false;
		}else{
			//split the origin_string by "\n"
			String[] string_arr=origin_string.split("\n");
			if(VDBG){
				System.out.println("splitString(),string_arr.length()=="+string_arr.length);
			}
			
			/*
			 * SCAN#1:
			 * add the LineText into mLineString.
			 * judge the type of LineText at the same time.
			 */
			for(int i=0;i<string_arr.length;i++){
				if(VVDBG){
					System.out.println("splitString!");
				}
				mLineString.add(new LineText(string_arr[i]));
			}

			/*
			 * SCAN#2:
			 * remove the extra space lines and all the title-format lines.
			 * (      ,====,-----)
			 */
			for(int i=0;i<mLineString.size();i++){
				//the current LineText
				LineText lt_curr=mLineString.get(i);
				//the next LineText
				LineText lt_next=null;
				if(i+1<mLineString.size()){
					lt_next=mLineString.get(i+1);
				}
				if(lt_next==null){
					//at the end of the mLineString.
					if(lt_curr.type==TEXTTYPE_LIST){
						lt_curr.islist_head=true;
						lt_curr.list_tailnum=lt_curr.list_len;
						lt_curr.list_tailstack=new LinkedList<Boolean>();
						mListTail.push(lt_curr.islist_order);
						/*
						 * move the element from mListTail to lt_curr.list_tailstack
						 * and the orders of the element are same.
						 */
						for(int j=0;j<lt_curr.list_tailnum;j++){
							lt_curr.list_tailstack.addLast(mListTail.pop());
						}
					}
					break;
				}
				if(lt_curr.type==TEXTTYPE_LIST&&i==0){
					//the first LineText is a list text
					lt_curr.islist_head=true;
					lt_curr.list_len=1;
					/*
					 * the Boolean value of the lt_curr should 
					 * be pushed into the mListTail.
					 */
					mListTail.push(lt_curr.islist_order);
				}
				if((lt_curr.type==TEXTTYPE_NORMAL||
						lt_curr.type==TEXTTYPE_ENDNORMALTEXT)&&
						(lt_next.type==TEXTTYPE_TITLELINE||
						lt_next.type==TEXTTYPE_SBLINE_CONSECUTIVE)){
					//next line is title-format line and it will be removed then. 
					//the title-format line will play a role.
					lt_curr.type=TEXTTYPE_TITLE_EQUAL;
					//the depth of the (Block Quote).
					lt_curr.blockquote_depth=0;	
					//now the title-format will be removed.
					mLineString.remove(i+1);
					continue;
				}
				if(lt_curr.type==TEXTYPE_SPACELINE&&
						lt_next.type==TEXTYPE_SPACELINE){
					//now remove the extra space line and go one line back .
					mLineString.remove(i+1);
					i--;
					continue;
				}
				if((lt_curr.type==TEXTTYPE_NORMAL||
						lt_curr.type==TEXTTYPE_ENDNORMALTEXT||
						lt_curr.type==TEXTTYPE_TITLE_JING)&&
						lt_curr.blockquote_depth>0){
					/*
					 * the current line has depth of (Block Quote),
					 * and the blockquote_depth will be trimmed now. 
					 */
					if(lt_next.type==TEXTYPE_SPACELINE){
						lt_curr.isblockquote_end=true;
					}else if((lt_next.type==TEXTTYPE_NORMAL||
							lt_next.type==TEXTTYPE_ENDNORMALTEXT||
							lt_next.type==TEXTTYPE_TITLE_JING)
							&&lt_next.blockquote_depth<lt_curr.blockquote_depth){
						lt_next.blockquote_depth=lt_curr.blockquote_depth;
						lt_next.isblockquote_start=false;
					}
				}
				if(lt_curr.type!=TEXTTYPE_LIST&&lt_next.type==TEXTTYPE_LIST){
					//next line is the head of a list.
					lt_next.islist_head=true;
					lt_next.list_len=1;
					mListTail.push(lt_next.islist_order);
				}
				if(lt_curr.type==TEXTTYPE_LIST&&lt_next.type==TEXTTYPE_LIST){
					//the current line and the next line are all lists.
					if(i==0){
						lt_curr.islist_head=true;
						lt_curr.list_len=1;
						mListTail.push(lt_curr.islist_order);
					}
					if(lt_next.list_len>lt_curr.list_len){
						lt_next.list_len=lt_curr.list_len+1;
						lt_next.islist_head=true;
						mListTail.push(lt_next.islist_order);
					}else if(lt_next.list_len<lt_curr.list_len){
						lt_curr.list_tailnum=lt_curr.list_len-lt_next.list_len;
						lt_curr.list_tailstack=new LinkedList<Boolean>();
						for(int j=0;j<lt_curr.list_tailnum;j++){
							lt_curr.list_tailstack.addLast(mListTail.pop());
						}
					}
				}
				if(lt_curr.type==TEXTTYPE_LIST&&lt_next.type!=TEXTTYPE_LIST){
					//we are now at the tail of a list.
					lt_curr.list_tailnum=lt_curr.list_len;
					lt_curr.list_tailstack=new LinkedList<Boolean>();
					mListTail.push(lt_curr.islist_order);
					for(int j=0;j<lt_curr.list_tailnum;j++){
						lt_curr.list_tailstack.addLast(mListTail.pop());		
					}
				}
			}
			if(VVDBG){
				System.out.println("��2��ɨ���Ժ�:");
				printLineStrings();
				printStack_tail();
			}
			
			
			/*
			 * SCAN#3:
			 * merge the LineTexts which are in the same unit.
			 */
			for(int i=0;i<mLineString.size();i++){
				LineText lt_curr=mLineString.get(i);
				LineText lt_next=null;
				if(i+1<mLineString.size()){
					lt_next=mLineString.get(i+1);
				}
				if(lt_next==null){
					break;
				}
				if(lt_curr.type==TEXTTYPE_NORMAL&&
						(lt_next.type==TEXTTYPE_NORMAL||
						lt_next.type==TEXTTYPE_ENDNORMALTEXT)){
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth>0){
						//the next line is in a (Block Quote).
						continue;
					}
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth==0){
						/*
						 * the current line and the next line are not in the 
						 * Block Quote.merge the next line into the current line
						 * and remove the next one.
						 */
						lt_curr.content+=lt_next.content;
						lt_curr.type=lt_next.type;
						mLineString.remove(i+1);
						i--;
						continue;
					}
					if(lt_curr.blockquote_depth>0&&lt_next.blockquote_depth>0){
						//the current and next lines are all in (Block Quote).
						if(lt_curr.isblockquote_start==true){
							//the current line is the start of the Block Quote.
							//add a '\t' at the beginning at the current line text.
							lt_curr.content="\t"+lt_curr.content;
						}
						if(lt_next.isblockquote_start==true){
							//if next line is the start of the Block Quote.
							//add a '\n\t' at the beginning at the next line text.
							//the  merge the next line to current line.
							lt_curr.content+="\n\t"+lt_next.content;
						}else{
							lt_curr.content+=lt_next.content;
						}
						//reset the islockquote_start to false.
						lt_curr.isblockquote_start=false;
						lt_curr.type=lt_next.type;
						//set the blockquote_depth of current line with next line.
						lt_curr.blockquote_depth=lt_next.blockquote_depth;
						mLineString.remove(i+1);
						i--;
						continue;
					}	
				}
				if((lt_curr.type==TEXTTYPE_ENDNORMALTEXT)&&
						(lt_next.type==TEXTTYPE_NORMAL||
						lt_next.type==TEXTTYPE_ENDNORMALTEXT)){
					//similar with the above if().
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth>0){
						continue;
					}
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth==0){
						//add '\n' to the beginning of the next line
						//then merge the current and the next line text.
						lt_curr.content+="\n"+lt_next.content;
						lt_curr.type=lt_next.type;
						mLineString.remove(i+1);
						i--;
						continue;
					}
					if(lt_curr.blockquote_depth>0){
						if(lt_curr.isblockquote_start==true){
							//add '\t' to the beginning of the current line.
							lt_curr.content="\t"+lt_curr.content;
						}
						if(lt_next.isblockquote_start==true){
							//next line is the start of a block quote.
							//add "\t\n" to the beginning of the next line.
							lt_curr.content+="\n\t"+lt_next.content;
						}else{
							lt_curr.content+="\n"+lt_next.content;
						}
						lt_curr.type=lt_next.type;
						//reset the isblockquote_start to false.
						lt_curr.isblockquote_start=false;
						//set the blockquote_depth of current line with next line.
						lt_curr.blockquote_depth=lt_next.blockquote_depth;
						mLineString.remove(i+1);
						i--;
						continue;
					}
					
				}
				if(lt_curr.type==TEXTTYPE_TITLE_JING&&
						lt_curr.isblockquote_start==true){
					//TEXTTYPE_TITLE_JING should add '\t' to the beginning.
					lt_curr.content="\t"+lt_curr.content;
				}
			}
			if(VVDBG){
				System.out.println("������ɨ���Ժ�the al_LineString:");
				printLineStrings();
			}
			return true;
		}
	}
	
	/**
	 * the core method of M2HTranslator.
	 * 
	 */
	public String translate(){
		//�����е�referdefine���͵Ķ���ȫ����ȡ���������õ�map_referdefine��
		Matcher matcher_referdefine=pattern_referdefine.matcher(origin_string);
		while(matcher_referdefine.find()){
			String rd_string=matcher_referdefine.group();
			int rd_start_index=matcher_referdefine.start();
			int rd_end_index=matcher_referdefine.end();
			//[id]:PathTitleUnit����
			Matcher matcher_id=pattern_squarebracket.matcher(rd_string);
			String id_string="";
			int start_index=0;
			int end_index=0;
			if(matcher_id.find()){
				id_string=matcher_id.group();
				start_index=matcher_id.start();
				end_index=matcher_id.end();
			}
			if(id_string.length()>=2){
				id_string=id_string.substring(1, id_string.length()-1);//ȥ������
			}
			//��ʱend_indexָ�����[id]��ĩβ����:ҲӦ�ñ��޳�
			//�ҵ�:����index����Ϊend_index
			while(end_index<rd_string.length()){
				if(rd_string.charAt(end_index)==':'){
					break;
				}
				end_index++;
			}
			String pt_string=StringUtils.eliminate(rd_string, start_index, end_index);
			mReferDefineMap.put(id_string, new PathTitleUnit(pt_string));
			origin_string=StringUtils.eliminate(origin_string, rd_start_index, rd_end_index-1);
			matcher_referdefine=pattern_referdefine.matcher(origin_string);
		}
		if(VDBG){
			printReferDefine();	
		}
		
		
		if(!splitString()){
			//����ʧ��
			return "ERROR";
		}
		//�Ȱ�ԭʼ�ı����и���
		//����Ϊ��λ���ı����д���
		//�ȶ�ÿһ�����html��ǩ��Ȼ���ٶԶ������<p>��ǩ
		
		//ֱ����һ��LinkedList��Ϊջ
		//push������addFirst()
		//pop������removeFirst()
		//peek������getFirst()
		LinkedList<StackElement> magic_stack=new LinkedList<StackElement>();
		Iterator<LineText> iterator=mLineString.iterator();
		if(VDBG){
			System.out.println("����al_LineString:");
		}
		while(iterator.hasNext()){
			LineText linetext=iterator.next();	//���������ı���
			if(VDBG){
				System.out.println("next:"+linetext.content);
			}
			//ֻ��NORMAL/ENDNORMALTEXT/TITLE���͵��ı��ſ��Խ���stack���д���
			if(linetext.type==TEXTTYPE_NORMAL||
					linetext.type==TEXTTYPE_ENDNORMALTEXT||
					linetext.type==TEXTTYPE_TITLE_JING||
					linetext.type==TEXTTYPE_TITLE_EQUAL||
					linetext.type==TEXTTYPE_LIST){
				String textstring=linetext.content;	//��ȡ�����ı�������
				int lineindex=0;
				magic_stack.clear();
				//��ͷ��βɨ���ַ�
				//step1:����ѹջ
				//step2:���հ�ջ����һ��
				while(lineindex<textstring.length()){
					char letter=textstring.charAt(lineindex);
					//�˴��ж��Ƿ���'\'
					//���ǣ���Ϊת���ַ�����ȡ��һ���ַ�������ƴ��ת����ת��
					//�����ǣ��������
					StackElement se_add;
					if(letter=='\\'){
						if(lineindex+1<textstring.length()){
							char letter_next=textstring.charAt(lineindex+1);
							//�����һ���ַ����Ժ�\ƴ��ת���ַ�
							if(isEscapeChar(letter_next)){
								if(VVDBG){
									System.out.println("magic_stackת���ַ���"+letter_next);	
								}
								lineindex++;
								se_add=new StackElement(letter_next,true);
							}
							//���򲻹ܣ�'\'ֻ�Ǹ���ͨ���ַ�
							else{
								se_add=new StackElement(letter,false);
							}
						}
						else{
							se_add=new StackElement(letter,false);
						}
					}else{
						se_add=new StackElement(letter,false);
					}
					
					StackElement se_top=magic_stack.peek();	//ջ��Ԫ��
					while(true){
						//��ת���ַ�
						if(se_add.isEscapeChar){
							if(se_top!=null&&se_top.type==ELEMENTTYPE_TEXT){
								String newstring=se_top.content+se_add.content;
								magic_stack.pop();
								se_add=new StackElement(ELEMENTTYPE_TEXT,newstring);
								magic_stack.push(se_add);
								break;
							}else{
								se_add=new StackElement(ELEMENTTYPE_TEXT,se_add.content);
								magic_stack.push(se_add);
								break;
							}
						}
						//ѹ�ո�ջԪ��
						else if(se_add.type==ELEMENTTYPE_SPACE){
							if(se_top==null){
								//ֱ�������ı�����ǰ�Ŀո�
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
									//���ж���ո�ϲ���Ϊһ�������Ӹ�һ���ո�
									break;
								}
							}else{
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
							}
							else{
								magic_stack.add(se_add);
							}
						}
						//ѹ'#'
						else if(se_add.type==ELEMENTTYPE_OP_JING){
							//���ջ����#���л���#ǰֻ�пո���ô�����ʽ��������
							//�����ֱ�ӱ���ı�����
							if(se_top==null){
								//ֱ�Ӿ����ı�����ǰ��#
								magic_stack.push(se_add);
								break;
							}
							if(magic_stack.size()==1){
								//��ǰջ���Ѿ�����һ��Ԫ�أ�#��������ǰ�Ͳ��Ǹ�ʽ���Ʒ�
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
									magic_stack.push(new StackElement(
											ELEMENTTYPE_TEXT,"#"));
									break;
								}else if(se_top.type==ELEMENTTYPE_TEXT){
									Pattern patt_block=Pattern.compile("(<blockquote>)+");
									Matcher matcher=patt_block.matcher(se_top.content);
									if(matcher.matches()){
										//˵��#ǰֻ��\t
										magic_stack.push(se_add);
										break;
									}
									magic_stack.pop();
									//����ĩβ
									String newstring=se_top.content+="#";
									se_add=new StackElement(ELEMENTTYPE_TEXT,newstring);
									magic_stack.push(se_add);
									break;
								}else{
									break;
								}
							}
							else{
								break;
							}
							
						}							
						//ѹ'##'
						else if(se_add.type==ELEMENTTYPE_OP_2JING){
							break;
						}
						//ѹ'###'
						else if(se_add.type==ELEMENTTYPE_OP_3JING){
							break;
						}
						//ѹ'####'
						else if(se_add.type==ELEMENTTYPE_OP_4JING){
							break;
						}
						//ѹ'#####'
						else if(se_add.type==ELEMENTTYPE_OP_5JING){
							break;
						}
						//ѹ'######'
						else if(se_add.type==ELEMENTTYPE_OP_6JING){
							break;
						}
						//ѹ'\n'
						else if(se_add.type==ELEMENTTYPE_OP_BR){
							if(se_top==null){
								//ֱ��������һ�л��з�������
								break;
							}
							if(se_top.type==ELEMENTTYPE_TEXT){
								magic_stack.pop();
								String newtext=se_top.content+"<br/>";
								se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
								magic_stack.push(se_add);
								break;
							}
							//����һ�������Ҫ����
							else{
								break;
							}
						}
						//ѹ'\t'
						else if(se_add.type==ELEMENTTYPE_OP_TAB){
							//ֱ�Ӱ�'\t'ת����"<blockquote>ѹ��ջ��"
							if(se_top==null){
								se_add=new StackElement(ELEMENTTYPE_TEXT,"<blockquote>");
								magic_stack.push(se_add);
								break;
							}
							
							else if(se_top.type==ELEMENTTYPE_TEXT){
								magic_stack.pop();
								String newtext=se_top.content+"<blockquote>";
								se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
								magic_stack.push(se_add);
								break;
							}
							else{
								break;
							}
						}
						//������������Ӱ�
						else{
							break;
						}
					}
						
					lineindex++;
				}
					
				/*
				 * ����magic_stack�����н��
				 * 
				 */
				//��magic_stack�н����ַ���ȡ����
				String stackstring="";	//��ջ��ȡ���ļӹ���һ�ε��ı�
				String stackendstring="";	//����</h1>���ֽ�β�����ı�
				//��ջ�׿�ʼ�Ե�����ȡ��ÿ��StackElement
				ListIterator<StackElement> listiter=magic_stack.listIterator(
						magic_stack.size());
				boolean flag_isstackbottom=true;	//��־�Ƿ���ջ��
				while(listiter.hasPrevious()){
					StackElement se_get=listiter.previous();
					String content_get=se_get.content;	//�ı�����
					int type_get=se_get.type;
					//�ǿհ�Ԫ�أ��Ǳ�Ȼ�����ı���ͷ��ȥ��
					if(type_get==ELEMENTTYPE_SPACE||type_get==ELEMENTTYPE_OP_2STAR||
							type_get==ELEMENTTYPE_OP_2SB){
						continue;
					}
					//���ھ��ŵĴ�����Ҫע��һ������
					//�����ı���#####�س�
					//����Ҫת����<h4>#</h4>
					//���###�����ı����������1
					else if(type_get==ELEMENTTYPE_OP_JING){
						//�ı��о�һ��#
						if(magic_stack.size()==1){
							stackstring="#";
						}else{
							stackstring+="<h1>";
							stackendstring="</h1>"+stackendstring;
						}
					}else if(type_get==ELEMENTTYPE_OP_2JING){
						//�ı��о�һ��##
						if(magic_stack.size()==1){
							stackstring="<h1>#</h1>";
						}else{
							stackstring+="<h2>";
							stackendstring="</h2>"+stackendstring;
						}
					}else if(type_get==ELEMENTTYPE_OP_3JING){
						if(magic_stack.size()==1){
							stackstring="<h2>#</h2>";
						}else{
							stackstring+="<h3>";
							stackendstring="</h3>"+stackendstring;
						}
					}else if(type_get==ELEMENTTYPE_OP_4JING){
						if(magic_stack.size()==1){
							stackstring="<h3>#</h3>";
						}else{
							stackstring+="<h4>";
							stackendstring="</h4>"+stackendstring;
						}
					}else if(type_get==ELEMENTTYPE_OP_5JING){
						if(magic_stack.size()==1){
							stackstring="<h4>#</h4>";
						}else{
							stackstring+="<h5>";
							stackendstring="</h5>"+stackendstring;
						}
					}else if(type_get==ELEMENTTYPE_OP_6JING){
						if(magic_stack.size()==1){
							stackstring="<h5>#</h5>";
						}else{
							stackstring+="<h6>";
							stackendstring="</h6>"+stackendstring;
						}
					}else if(type_get==ELEMENTTYPE_TEXT){
						stackstring+=content_get;
					}else{
						stackstring+=content_get;
					}
					flag_isstackbottom=false;	//ջ���Ѿ�������ˣ���һ��Ԫ�ز���ջ��
				}
				
				/*
				 * �˴���������`````,ת����<code>
				 */
				Matcher matcher_code_out=pattern_code_greedy.matcher(stackstring);
				//̰����ƥ��
				if(matcher_code_out.find()){
					String string_code_out=matcher_code_out.group();
					int start_out=matcher_code_out.start();
					int end_out=matcher_code_out.end();
					//��ǿ��ƥ��
					Matcher matcher_code_in=pattern_code_scared.matcher(string_code_out);
					int find_index=0;
					H2EntityTranslater h2ttranlater=new H2EntityTranslater();
					while(matcher_code_in.find(find_index)){
						String string_code_in=matcher_code_in.group();
						int start_in=matcher_code_in.start();
						int end_in=matcher_code_in.end();
						//System.out.println("code p:"+string_code_in);
						//string_code_in��һ������:start_in~end_in
						//����``````xxx``
						//Ҳ����``xxxxx`````
						//Ҳ����`````
						if(pattern_code_op.matcher(string_code_in).matches()){
							continue;
						}
						
						int start=start_in;
						int end=end_in-1;
						while(string_code_out.charAt(start)=='`'&&
								string_code_out.charAt(end)=='`'&&
								start<end){
							start++;
							end--;
						}
						if(start>=end){
							//string_code_in ��> <code>string_code_out.charAt(start)</code>
							string_code_in=""+string_code_out.charAt(start);
							h2ttranlater.loadString(string_code_in);
							string_code_in="<code>" + h2ttranlater.translate() + "</code>";
						}
						else if(string_code_out.charAt(start)!='`'){
							if(string_code_out.charAt(end)!='`'){
								//string_code_in -> <code>string_code_out.substring(start,end)</code>
								string_code_in=string_code_out.substring(start,end+1);
								h2ttranlater.loadString(string_code_in);
								string_code_in="<code>"+h2ttranlater.translate()+"</code>";
							}else{
								//���仯
							}
						}
						else if(string_code_out.charAt(end)!='`'){
							//string_code_in -><code>string_code_out.substring(start,end)
							string_code_in=string_code_out.substring(start,end+1);
							h2ttranlater.loadString(string_code_in);
							string_code_in="<code>"+h2ttranlater.translate()+"</code>";
						}
						string_code_out=StringUtils.replace(string_code_out, 
								string_code_in, start_in, end_in-1);
						matcher_code_in=pattern_code_scared.matcher(string_code_out);
						find_index=end_in;
					}
					//string_code_out���滻��<code>�Ĵ���Ӧ���滻��stackstring��
					stackstring=StringUtils.replace(stackstring, string_code_out, start_out, end_out-1);
					
				}
					
				/*
				 * �˴���Ҫ�������е�pattern_image,pattern_weburl
				 * ����ת����html��ǩ
				 * 
				 * ע�⣬һ��Ҫ��image����weburl
				 */
				//����image��ʽ��
				int find_index=0;
				Matcher matcher=pattern_image.matcher(stackstring);
				while(matcher.find(find_index)){
					int start_index=matcher.start();
					int end_index=matcher.end();
					String imagestring=matcher.group();	//����stackstring���ҵ���Ŀ�괮
					//����imagestring��ת���ɱ�ǩ��ʽ
					Matcher sqbrackets_matcher=pattern_squarebracket.
						matcher(imagestring);
					Matcher brackets_matcher=pattern_bracket.
							matcher(imagestring);
					String img_alt="";
					String img_path="";
					String img_title="";
					if(sqbrackets_matcher.find()){
						img_alt=sqbrackets_matcher.group();
						img_alt=img_alt.substring(1,img_alt.length()-1);
					}
					if(brackets_matcher.find()){
						img_path=brackets_matcher.group();
						img_path=img_path.substring(1,
								img_path.length()-1);
						PathTitleUnit ptu=new PathTitleUnit(img_path);
						img_path=ptu.getPath();
						img_title=ptu.getTitle();
					}
					imagestring="<img src=\""+img_path+"\" alt=\""+
							img_alt +"\" title=\"" + img_title + "\" />";
					//�޳��˶�Ӧλ�õ��ַ���
					stackstring=StringUtils.replace(stackstring,imagestring,
							start_index,end_index-1);
					find_index=start_index+imagestring.length();
					matcher=pattern_image.matcher(stackstring);
				}
				
				find_index=0;
				matcher=pattern_weburl.matcher(stackstring);
				while(matcher.find()){
					int start_index=matcher.start();
					int end_index=matcher.end();
					String urlstring=matcher.group();	//����stackstring���ҵ���Ŀ�괮
					//����urlstring��ת���ɱ�ǩ��ʽ
					Matcher sqbrackets_matcher=pattern_squarebracket.
							matcher(urlstring);
					Matcher brackets_matcher=pattern_bracket.
							matcher(urlstring);
					String url_path="";	//ʵ���ϵ�����·��
					String url_title="";	//url��title
					String url_display="";	//��ʾ����������
					//�ҵ��˷��������ݣ���Ϊ��ʾ��������������
					if(sqbrackets_matcher.find()){
						url_display=sqbrackets_matcher.group();
						url_display=url_display.substring(1,
								url_display.length()-1);
					}
					//�ҵ���Բ���ŵ�����
					//��Ҫ�ж���xxx��ʽ����xxx "xxx"��ʽ
					if(brackets_matcher.find()){
						url_path=brackets_matcher.group();
						url_path=url_path.substring(1,
								url_path.length()-1);
						//����һ��PathTitleUnit�����
						PathTitleUnit ptu=new PathTitleUnit(url_path);
						url_path=ptu.getPath();
						url_title=ptu.getTitle();
						
					}
					if("".equals(url_title)){
						urlstring="<a href=\"" + url_path + "\">"
								+ url_display + "</a>";
					}else{
						urlstring="<a href=\"" + url_path + "\" title=\"" + 
								url_title+"\">" + url_display + "</a>";
					}
					//�޳��˶�Ӧλ�õ��ַ���
					stackstring=StringUtils.replace(stackstring,urlstring,
							start_index,end_index-1);
					find_index=start_index+urlstring.length();
					matcher=pattern_weburl.matcher(stackstring);
				}
				//![alt][id],image
				find_index=0;
				matcher=pattern_squarebracket2_image.matcher(stackstring);
				while(matcher.find()){
					int start_index=matcher.start();
					int end_index=matcher.end();
					String image_pair=matcher.group();
					String image_alt="";
					String image_path="";
					String image_title="";
					String image_id="";
					String imagestring="";
					Matcher matcher_sbracket=pattern_squarebracket.matcher(image_pair);
					if(matcher_sbracket.find()){
						image_alt=matcher_sbracket.group();
						image_alt=image_alt.substring(1, image_alt.length()-1);
					}
					while(matcher_sbracket.find()){
						image_id=matcher_sbracket.group();
					}
					if(image_id.length()>=2){
						image_id=image_id.substring(1, image_id.length()-1);
						}
					PathTitleUnit ptu=mReferDefineMap.get(image_id);
					if(ptu!=null){
						image_path=ptu.getPath();
						image_title=ptu.getTitle();
					}
					imagestring="<img src=\""+image_path+"\" alt=\""+
							image_alt +"\" title=\"" + image_title + "\" />";
					stackstring=StringUtils.replace(stackstring,imagestring,
							start_index,end_index-1);
					find_index=start_index+imagestring.length();
					matcher=pattern_squarebracket2_image.matcher(stackstring);
				}
				
				//[display][id],weburl
				find_index=0;
				matcher=pattern_squarebracket2.matcher(stackstring);
				while(matcher.find()){
					int start_index=matcher.start();
					int end_index=matcher.end();
					String url_pair=matcher.group();
					String url_id="";
					String url_display="";
					String url_path="";
					String url_title="";
					String url_string="";
					Matcher matcher_sbracket=pattern_squarebracket.matcher(url_pair);
					if(matcher_sbracket.find()){
						url_display=matcher_sbracket.group();
						url_display=url_display.substring(1, url_display.length()-1);
					}
					while(matcher_sbracket.find()){
						url_id=matcher_sbracket.group();	
					}
					if(url_id.length()>=2){
						url_id=url_id.substring(1, url_id.length()-1);
					}
					PathTitleUnit ptu=mReferDefineMap.get(url_id);
					if(ptu!=null){
						url_path=ptu.getPath();
						url_title=ptu.getTitle();
					}
					if("".equals(url_title)){
						url_string="<a href=\"" + url_path + "\">"
								+ url_display + "</a>";
					}else{
						url_string="<a href=\"" + url_path + "\" title=\"" + 
								url_title+"\">" + url_display + "</a>";
					}
					//�޳��˶�Ӧλ�õ��ַ���
					
					stackstring=StringUtils.replace(stackstring,url_string,
							start_index,end_index-1);
					find_index=start_index+url_string.length();
					matcher=pattern_squarebracket2.matcher(stackstring);
				}
									
				stackstring=stackstring+stackendstring;	//ƴ����</..></..>
				if(linetext.type==TEXTTYPE_NORMAL){
					stackstring="<p>"+stackstring+"</p>";
					if(linetext.isblockquote_end){
						//��������ȵĽ�����ǩ����
						for(int i=0;i<linetext.blockquote_depth;i++){
							stackstring+="</blockquote>";
						}
					}
				}else if(linetext.type==TEXTTYPE_ENDNORMALTEXT){
					stackstring="<p>"+stackstring+"<br/></p>";
					if(linetext.isblockquote_end){
						//��������ȵĽ�����ǩ����
						for(int i=0;i<linetext.blockquote_depth;i++){
							stackstring+="</blockquote>";
						}
					}
				}
				else if(linetext.type==TEXTTYPE_TITLE_EQUAL){
					stackstring="<h1>"+stackstring+"</h1>";
				}
				else if(linetext.type==TEXTTYPE_TITLE_JING){
					if(linetext.isblockquote_end){
					//��������ȵĽ�����ǩ����
					for(int i=0;i<linetext.blockquote_depth;i++){
						stackstring+="</blockquote>";
						}
					}
				}else if(linetext.type==TEXTTYPE_LIST){
					stackstring="<li>"+stackstring+"</li>";
					//�����б�ͷ��
					if(linetext.islist_head){
						if(linetext.islist_order){
							stackstring="<ol>"+stackstring;
						}else{
							stackstring="<ul>"+stackstring;
						}
					}
					//�����б�β
					for(int i=0;i<linetext.list_tailnum;i++){
						if(linetext.list_tailstack!=null){
							while(!linetext.list_tailstack.isEmpty()){
								boolean isorder=linetext.list_tailstack.pop();
								if(isorder){
									stackstring+="</ol>";
								}else{
									stackstring+="</ul>";
								}
							}
						}
					}
				}
				if(linetext.isCode){
					H2EntityTranslater h2etranslater=new H2EntityTranslater(stackstring);
					stackstring="<code>"+h2etranslater.translate()+"</code>";
				}
					
				if(VDBG){
					System.out.println("stackstring:"+stackstring);
				}
				//�ٷ��û�al_LineString
				linetext.content=stackstring;	//���ı��滻��magic_stack�����ӹ�����
			}
			
			
			//�����ǰ�ı�����- - -- --
			else if(linetext.type==TEXTTYPE_SBLINE_DISCONTINUOUS||
					linetext.type==TEXTTYPE_STARLINE){
				linetext.content="<hr>";
			}
		}
		
		Iterator<LineText> iter=mLineString.iterator();
		StringBuilder html_stringbuilder=new StringBuilder();
		while(iter.hasNext()){
			LineText lt_get=iter.next();
			html_stringbuilder.append(lt_get.content+"\n");
		}
		return html_stringbuilder.toString();
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
	
	//��ӡ��ÿһ�е��ı����ݺ��ı�����
	private void printLineStrings(){
		if(this.mLineString!=null){
			Iterator<LineText> iter=mLineString.iterator();
			while(iter.hasNext()){
				LineText lt=iter.next();
				System.out.println("type:"+lt.type+"\ncontent:"+lt.content+"\n"
						+ "blockquote_depth:"+lt.blockquote_depth+
						"\nisblockquote_start:"+lt.isblockquote_start+
						"\nlist_len:"+lt.list_len+
						"\nlist_tailnum"+lt.list_tailnum+"\n");
			}
		}
	}

	private void printReferDefine(){
		if(mReferDefineMap!=null){
			Set<String> set=mReferDefineMap.keySet();
			Iterator<String> it=set.iterator();
			while(it.hasNext()){
				String key=it.next();
				PathTitleUnit ptu=mReferDefineMap.get(key);
				System.out.println("id:"+key+",path:"+ptu.getPath()+",title:"+ptu.getTitle());
				
			}
		}
	}
	
	private void printStack_tail(){
		System.out.println(mListTail);
	}
}
