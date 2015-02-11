package com.liushenming.mrdeer.translatemodule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.liushenming.mrdeer.utils.StringUtils;

/*
 * Markdown文本到Html文本的转换器
 */
public class M2HTranslater {

	//通过构造器传进来的原始字符串
	private String origin_string;
	
	//<行号,行字符串>
	private ArrayList<LineText> al_LineString;	//行文本
	private LinkedList<Boolean> stack_listtail;	//列表项的尾巴栈
	private HashMap<String,PathTitleUnit> map_referdefine;	//存放[id]:xxx "xxxx"
	public boolean DBG=false;
	public boolean VDBG=false;
	public boolean VVDBG=true;
	
	/*
	 * 文本的类型
	 */
	private final static int TEXTTYPE_NORMAL=10;  //普通文本类型
	private final static int TEXTYPE_SPACELINE=11;	//空白的文本类型
	private final static int TEXTTYPE_TITLELINE=12;	//=====
	private final static int TEXTTYPE_TITLE_JING=13;	//标题，标题类似于独立的段(##)
	private final static int TEXTTYPE_TITLE_EQUAL=14;	//标题，标题类似于独立的段(==)
	private final static int TEXTTYPE_SBLINE_CONTINUOUS=15;//---连续的 
	private final static int TEXTTYPE_SBLINE_DISCONTINUOUS=16;	//- --，包含空格和-
	private final static int TEXTTYPE_ENDNORMALTEXT=17;	//最后是两空格的普通文本类型
	private final static int TEXTTYPE_STARLINE=18;	//** *，可以有空格\
	private final static int TEXTTYPE_LIST=19;	//列表类型的文本行
	
	//用于内部类LineText判断文本类型用
	static final Pattern pattern_spaceline=Pattern.compile("\\s*");
	static final Pattern pattern_titleline=Pattern.compile("={3,}");
	static final Pattern pattern_title_jing=Pattern.compile("[#]{2,}.*");
	static final Pattern pattern_sbline_continuous=Pattern.compile("-{3}");
	static final Pattern pattern_sbline_discontinuous=Pattern.compile("(\\s*-\\s*){3,}");
	//static final Pattern pattern_endnormaltext=Pattern.compile("");
	static final Pattern pattern_starline=Pattern.compile("(\\s*\\*\\s*){3,}");
	static final Pattern pattern_blockquote=Pattern.compile("\\s*>+.+");
	static final Pattern pattern_list=Pattern.compile("\\s*([*+-]|[0-9]+\\.)\\s+.+");
	
	static final Pattern pattern_image=Pattern.compile("!\\[.+\\]\\(.+ \".+\"\\)");
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
	static final Pattern pattern_code_greedy=Pattern.compile("[`]+.+[`]+");
	static final Pattern pattern_code_scared=Pattern.compile("[`]+.+?[`]+");
	static final Pattern pattern_code_op=Pattern.compile("[`]+");
	
	//行文本类
	class LineText{
		String content;	//文本内容
		int type;	//行文本的类型
		int blockquote_depth;	//处于引用块的深度，初值为0
		boolean isblockquote_start;//是否是一个引用块的头
		boolean isblockquote_end;//是否是引用块的结尾
		int list_len;	//列表的缩进
		boolean islist_order;	//true代表有序列表，false代表无序列表
		boolean islist_head;	//是否是某个列表的第一个
		int list_tailnum;	//列表的尾数量
		LinkedList<Boolean> list_tailstack;	//列表的尾栈
		
		//创建时只能传入文本内容。
		LineText(String c){
			content=c;
			//计算出引用深度，并将引用符号去除
			blockquote_depth=caculateBlockquoteDepth();
			type=judgeType(content);
			caculateListParam();
			isblockquote_end=false;
		}
		
		//计算list相关的参数
		void caculateListParam(){
			list_len=0;
			islist_head=false;
			list_tailnum=0;
			if(type!=TEXTTYPE_LIST){
				return;
			}
			int index=0;
			//计算list_len的粗糙值(要在第二次扫描中进行调整)
			list_len=1;	//初值是1
			while(index<content.length()){
				if(content.charAt(index)==' '){
					list_len++;
				}else{
					//此时判断list项的类型，ul是无序，ol是有序
					if(content.charAt(index)=='*'||content.charAt(index)=='+'
							||content.charAt(index)=='-'){
						islist_order=false;
					}
					else{
						islist_order=true;
					}
					break;
				}
				index++;
			}
			//去除文本行中'+','*','100.'这类列表符号
			while(index<content.length()){
				if(content.charAt(index)!=' '){
					index++;
				}else{
					break;
				}
			}
			content=content.substring(index);
		}
		
		//计算文本行的引用深度
		int caculateBlockquoteDepth(){
			int quotecount=0;
			//引用模式匹配成功，
			if(pattern_blockquote.matcher(content).matches()){
				//此时只要是有引用符号的，都认为是引用开始，接下来的扫描会细化
				isblockquote_start=true;	
				int index=0;
				//扫描字符串，数连续的'>'个数
				while(index<content.length()){
					//一个还没找着，这次也没找着，要继续找
					if(quotecount==0&&content.charAt(index)!='>'){
						index++;
						continue;
					}
					//这次找着了，进入下一轮
					if(content.charAt(index)=='>'){
						quotecount++;
						index++;
						continue;
					}
					//已经找着过了，但这次不是
					else{
						break;
					}
				}
				content=content.substring(index);
			}else{
				isblockquote_start=false;
			}
			return quotecount;
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
			}else if(pattern_title_jing.matcher(content).matches()){
				return TEXTTYPE_TITLE_JING;
			}else if(pattern_list.matcher(content).matches()){
				return TEXTTYPE_LIST;
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
	//........
	
	
	//在处理每一个行时，栈中每一格的元素类
	class StackElement{
		int type;	//他是属于操作符还是文本
		String content;	//具体的内容
		StackElement(int t,String c){
			type=t;
			content=c;
		}
		
		//刚扫描到一个字符时直接创建一个StackElement，并且在构造函数中判断类型
		StackElement(char c){
			content=c+"";
			type=judgeType(c);
		}
		
		//判断一个字符是什么类型
		int judgeType(char c)
		{
			int t=ELEMENTTYPE_UNKNOW;
			switch(c){
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
			return t;
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
		stack_listtail=new LinkedList<Boolean>();
		map_referdefine=new HashMap<String,PathTitleUnit>();
		
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
			//第一次扫描
			//一行一行文本划分到了al_LineString里，同时判断出类型
			for(int i=0;i<string_arr.length;i++){
				if(VVDBG){
					System.out.println("splitString!");
				}
				al_LineString.add(new LineText(string_arr[i]));
			}
			//此时已经初步按照\n将整个大段文本分开，并且标记了类型。
			//需要进一步整合行文本，使其以段落为行文本的单位
			//第二次扫描,去除多余的空白行，将TEXTTYPE_TITLELINE的行删掉
			for(int i=0;i<al_LineString.size();i++){
				LineText lt_curr=al_LineString.get(i);	//当前的LineText
				LineText lt_next=null;	//下一个text
				if(i+1<al_LineString.size()){
					lt_next=al_LineString.get(i+1);
				}
				if(lt_next==null){
					if(lt_curr.type==TEXTTYPE_LIST){
						lt_curr.list_tailnum=lt_curr.list_len;
						lt_curr.list_tailstack=new LinkedList<Boolean>();
						//向文本行的list_tailstack成员中加入Boolean对象,并且结构于stack_listtail一致
						for(int j=0;j<lt_curr.list_tailnum;j++){
							lt_curr.list_tailstack.addLast(stack_listtail.pop());
						}
						
					}
					break;
				}
				//如果当前文本行是一个普通文本/换行文本，并且下一个文本行是====或者是-----
				if((lt_curr.type==TEXTTYPE_NORMAL
						||lt_curr.type==TEXTTYPE_ENDNORMALTEXT)&&
						(lt_next.type==TEXTTYPE_TITLELINE||
						lt_next.type==TEXTTYPE_SBLINE_CONTINUOUS)){
					//将当前文本的type改成TEXTTYPE_TITLE
					lt_curr.type=TEXTTYPE_TITLE_EQUAL;
					lt_curr.blockquote_depth=0;	//这类文本行不能位于引用中
					//===/----这种TEXTTYPE_TITLELINE文本行已经失去他们的价值，删掉
					al_LineString.remove(i+1);
					continue;
				}
				//当前行，下一行都是空白行
				if(lt_curr.type==TEXTYPE_SPACELINE&&
						lt_next.type==TEXTYPE_SPACELINE){
					//删掉下面一行
					al_LineString.remove(i+1);
					//退回一格，下次循环仍要从本空白行开始
					i--;
					continue;
				}
				//当前行是个文本且可能影响下面的行，因为它是具有引用深度的
				if((lt_curr.type==TEXTTYPE_NORMAL||
						lt_curr.type==TEXTTYPE_ENDNORMALTEXT||
						lt_curr.type==TEXTTYPE_TITLE_JING)&&
						lt_curr.blockquote_depth>0){
					if(lt_next.type==TEXTYPE_SPACELINE){
						lt_curr.isblockquote_end=true;
					}else if((lt_next.type==TEXTTYPE_NORMAL||
							lt_next.type==TEXTTYPE_ENDNORMALTEXT||
							lt_next.type==TEXTTYPE_TITLE_JING)
							&&lt_next.blockquote_depth<lt_curr.blockquote_depth){
						lt_next.blockquote_depth=lt_curr.blockquote_depth;
						lt_next.isblockquote_start=false;	//下一行文本不是引用开始
					}
				}
				//处理列表第一个项
				if(lt_curr.type!=TEXTTYPE_LIST&&lt_next.type==TEXTTYPE_LIST){
					lt_next.islist_head=true;
					lt_next.list_len=1;	//第一个列表项的list_len必须是1
					stack_listtail.push(lt_next.islist_order);
				}
				//当前文本和下一文本都是list
				if(lt_curr.type==TEXTTYPE_LIST&&lt_next.type==TEXTTYPE_LIST){
					//第一行文本就是列表项
					if(i==0){
						lt_curr.islist_head=true;
						lt_curr.list_len=1;
						stack_listtail.push(lt_curr.islist_order);
					}
					if(lt_next.list_len>lt_curr.list_len){
						lt_next.list_len=lt_curr.list_len+1;
						lt_next.islist_head=true;
						stack_listtail.push(lt_next.islist_order);
					}else if(lt_next.list_len<lt_curr.list_len){
						lt_curr.list_tailnum=lt_curr.list_len-lt_next.list_len;
						lt_curr.list_tailstack=new LinkedList<Boolean>();
						for(int j=0;j<lt_curr.list_tailnum;j++){
							lt_curr.list_tailstack.addLast(stack_listtail.pop());
						}
					}
				}
				//处理列表最后一个项
				if(lt_curr.type==TEXTTYPE_LIST&&lt_next.type!=TEXTTYPE_LIST){
					lt_curr.list_tailnum=lt_curr.list_len;
					lt_curr.list_tailstack=new LinkedList<Boolean>();
					for(int j=0;j<lt_curr.list_tailnum;j++){
						lt_curr.list_tailstack.addLast(stack_listtail.pop());
					}
				}
			}
			if(VVDBG){
				System.out.println("第2次扫描以后:");
				printLineStrings();
				printStack_tail();
			}
			
			
			
			//第三次扫描，将同一个段落的文本合并成一行
			for(int i=0;i<al_LineString.size();i++){
				LineText lt_curr=al_LineString.get(i);	//当前的LineText
				LineText lt_next=null;	//下一个text
				if(i+1<al_LineString.size()){
					lt_next=al_LineString.get(i+1);
				}
				if(lt_next==null){
					break;
				}
				//当前是普通文本，下一行是也是普通文本/TEXTTYPE_ENDNORMALTEXT
				if(lt_curr.type==TEXTTYPE_NORMAL&&
						(lt_next.type==TEXTTYPE_NORMAL||
						lt_next.type==TEXTTYPE_ENDNORMALTEXT)){
					
					//当前行不在引用中，下一行在引用中
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth>0){
						//这一行就停止了
						continue;
					}
					//都不在引用中
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth==0){
						//合并两个文本到当前的LineText,删除next
						lt_curr.content+=lt_next.content;
						lt_curr.type=lt_next.type;
						al_LineString.remove(i+1);
						i--;
						continue;
					}
					//都在引用中
					if(lt_curr.blockquote_depth>0){
						//当前行是引用开始
						if(lt_curr.isblockquote_start==true){
							lt_curr.content="\t"+lt_curr.content;
						}
						//如果下一行是个引用开始，要加入‘\t\n’
						if(lt_next.isblockquote_start==true){
							lt_curr.content+="\n\t"+lt_next.content;
						}else{
							lt_curr.content+=lt_next.content;
						}
						//该行的isblockquote_start属性已经发挥过了价值了，应该置false
						lt_curr.isblockquote_start=false;
						lt_curr.type=lt_next.type;
						//深度只会越变越大
						lt_curr.blockquote_depth=lt_next.blockquote_depth;
						al_LineString.remove(i+1);
						i--;
						continue;
					}
					
				}
				//当前是TEXTTYPE_ENDNORMALTEXT，下一行是也是普通文本/TEXTTYPE_ENDNORMALTEXT
				if((lt_curr.type==TEXTTYPE_ENDNORMALTEXT)
						&&(lt_next.type==TEXTTYPE_NORMAL||
						lt_next.type==TEXTTYPE_ENDNORMALTEXT)){
					//合并两个文本到当前的LineText,以\n间隔,删除next
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth>0){
						//这一行就停止了
						continue;
					}
					//都不在引用中
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth==0){
						//合并两个文本到当前的LineText,删除next
						lt_curr.content+="\n"+lt_next.content;
						lt_curr.type=lt_next.type;
						al_LineString.remove(i+1);
						i--;
						continue;
					}
					//都在引用中
					if(lt_curr.blockquote_depth>0){
						//当前行是引用开始
						if(lt_curr.isblockquote_start==true){
							lt_curr.content="\t"+lt_curr.content;
						}
						//如果下一行是个引用开始，要加入‘\t\n’
						if(lt_next.isblockquote_start==true){
							lt_curr.content+="\n\t"+lt_next.content;
						}else{
							lt_curr.content+="\n"+lt_next.content;
						}
						lt_curr.type=lt_next.type;
						//该行的isblockquote_start属性已经发挥过了价值了，应该置false
						lt_curr.isblockquote_start=false;
						//深度只会越变越大
						lt_curr.blockquote_depth=lt_next.blockquote_depth;
						al_LineString.remove(i+1);
						i--;
						continue;
					}
					
				}
				//#标题文本则自己判断要不要在开头加上<blockquote>
				if(lt_curr.type==TEXTTYPE_TITLE_JING&&
						lt_curr.isblockquote_start==true){
					lt_curr.content="\t"+lt_curr.content;
				}
			}
			if(VVDBG){
				System.out.println("第三次扫描以后，the al_LineString:");
				printLineStrings();
			}
			
			return true;
		}
	}
	
	//构造了M2HTranslate对象以后调用translate()进行转换
	public String translate(){
		//将所有的referdefine类型的短语全部提取出来，放置到map_referdefine中
		Matcher matcher_referdefine=pattern_referdefine.matcher(origin_string);
		while(matcher_referdefine.find()){
			String rd_string=matcher_referdefine.group();
			int rd_start_index=matcher_referdefine.start();
			int rd_end_index=matcher_referdefine.end();
			//[id]:PathTitleUnit部分
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
				id_string=id_string.substring(1, id_string.length()-1);//去方括号
			}
			//此时end_index指向的是[id]的末尾，而:也应该被剔除
			//找到:所在index，设为end_index
			while(end_index<rd_string.length()){
				if(rd_string.charAt(end_index)==':'){
					break;
				}
				end_index++;
			}
			String pt_string=StringUtils.eliminate(rd_string, start_index, end_index);
			map_referdefine.put(id_string, new PathTitleUnit(pt_string));
			origin_string=StringUtils.eliminate(origin_string, rd_start_index, rd_end_index-1);
			matcher_referdefine=pattern_referdefine.matcher(origin_string);
		}
		if(VDBG){
			printReferDefine();	
		}
		
		
		if(!splitString()){
			//划分失败
			return "ERROR";
		}
		//先把原始文本给切割了
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
				LineText linetext=iterator.next();	//遍历到的文本行
				if(VDBG){
					System.out.println("next:"+linetext.content);
				}
				//只有NORMAL/ENDNORMALTEXT/TITLE类型的文本才可以进入stack进行处理
				if(linetext.type==TEXTTYPE_NORMAL||
						linetext.type==TEXTTYPE_ENDNORMALTEXT||
						linetext.type==TEXTTYPE_TITLE_JING||
						linetext.type==TEXTTYPE_TITLE_EQUAL||
						linetext.type==TEXTTYPE_LIST){
					String textstring=linetext.content;	//获取了行文本的内容
					int lineindex=0;
					magic_stack.clear();
					//从头至尾扫描字符
					//step1:依次压栈
					//step2:最终把栈处理一下
					while(lineindex<textstring.length()){
						char letter=textstring.charAt(lineindex);
						StackElement se_add=new StackElement(letter);
						StackElement se_top=magic_stack.peek();	//栈顶元素
						while(true){
							//压空格栈元素
							if(se_add.type==ELEMENTTYPE_SPACE){
								if(se_top==null){
									//直接无视文本行最前的空格
									break;
								}
								if(se_top.type==ELEMENTTYPE_TEXT){
									if(se_top.content.charAt(se_top.content.length()-1)
											!=' '){
										//添加上这一个空格
										String newstring=se_top.content+" ";
										magic_stack.pop();
										se_add=new StackElement(ELEMENTTYPE_TEXT,newstring);
										se_top=magic_stack.peek();
										continue;
									}else{
										//行中多个空格合并成为一个，无视该一个空格
										break;
									}
								}else{
									break;
								}
							}
							//压正文
							else if(se_add.type==ELEMENTTYPE_TEXT){
								//栈顶也是TEXT类型
								if(se_top!=null&&se_top.type==ELEMENTTYPE_TEXT){
									magic_stack.pop();
									String newtext=se_top.content+se_add.content;
									se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
									se_top=magic_stack.peek();
									continue;
								}else{
									//其他情况，直接压栈
									magic_stack.push(se_add);
									break;
								}
							}
							//压'_'
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
							//压'__'
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
							//压'*'
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
							//压'**'
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
							//压'#'
							else if(se_add.type==ELEMENTTYPE_OP_JING){
								//如果栈底是#序列或者#前只有空格，那么则起格式控制作用
								//否则就直接变成文本类型
								if(se_top==null){
									//直接就是文本行最前的#
									magic_stack.push(se_add);
									break;
								}
								if(magic_stack.size()==1){
									//当前栈中已经有了一个元素，#不放在最前就不是格式控制符
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
											//说明#前只有\t
											magic_stack.push(se_add);
											break;
										}
										magic_stack.pop();
										//填至末尾
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
							//压'##'
							else if(se_add.type==ELEMENTTYPE_OP_2JING){
								break;
							}
							//压'###'
							else if(se_add.type==ELEMENTTYPE_OP_3JING){
								break;
							}
							//压'####'
							else if(se_add.type==ELEMENTTYPE_OP_4JING){
								break;
							}
							//压'#####'
							else if(se_add.type==ELEMENTTYPE_OP_5JING){
								break;
							}
							//压'######'
							else if(se_add.type==ELEMENTTYPE_OP_6JING){
								break;
							}
							//压'\n'
							else if(se_add.type==ELEMENTTYPE_OP_BR){
								if(se_top==null){
									//直接输入了一行换行符，无视
									break;
								}
								if(se_top.type==ELEMENTTYPE_TEXT){
									magic_stack.pop();
									String newtext=se_top.content+"<br/>";
									se_add=new StackElement(ELEMENTTYPE_TEXT,newtext);
									magic_stack.push(se_add);
									break;
								}
								//其他一律情况都要无视
								else{
									break;
								}
							}
							//压'\t'
							else if(se_add.type==ELEMENTTYPE_OP_TAB){
								//直接把'\t'转换成"<blockquote>压进栈中"
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
							//其余情况就无视吧
							else{
								break;
							}
						}
						
						
						lineindex++;
					}
					
					/*
					 * 清理magic_stack，出行结果
					 * 
					 */
					//从magic_stack中将行字符串取出来
					String stackstring="";	//从栈中取出的加工过一次的文本
					String stackendstring="";	//例如</h1>这种结尾控制文本
					//从栈底开始自底向上取出每个StackElement
					ListIterator<StackElement> listiter=magic_stack.listIterator(
							magic_stack.size());
					boolean flag_isstackbottom=true;	//标志是否是栈底
					while(listiter.hasPrevious()){
						StackElement se_get=listiter.previous();
						String content_get=se_get.content;	//文本内容
						int type_get=se_get.type;
						//是空白元素，那必然处于文本开头，去掉
						if(type_get==ELEMENTTYPE_SPACE||type_get==ELEMENTTYPE_OP_2STAR||
								type_get==ELEMENTTYPE_OP_2SB){
							continue;
						}
						//关于井号的处理需要注意一个问题
						//例如文本：#####回车
						//则需要转换成<h4>#</h4>
						//如果###后有文本，则无需减1
						else if(type_get==ELEMENTTYPE_OP_JING){
							//文本行就一个#
							if(magic_stack.size()==1){
								stackstring="#";
							}else{
								stackstring+="<h1>";
								stackendstring="</h1>"+stackendstring;
							}
						}else if(type_get==ELEMENTTYPE_OP_2JING){
							//文本行就一个##
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
						}
						flag_isstackbottom=false;	//栈底已经处理过了，下一个元素不是栈底
					}
					
					/*
					 * 此处处理所有`````,转换成<code>
					 */
					Matcher matcher_code_out=pattern_code_greedy.matcher(stackstring);
					//贪婪型匹配
					if(matcher_code_out.find()){
						String string_code_out=matcher_code_out.group();
						int start_out=matcher_code_out.start();
						int end_out=matcher_code_out.end();
						//勉强型匹配
						Matcher matcher_code_in=pattern_code_scared.matcher(string_code_out);
						int find_index=0;
						H2EntityTranslater h2ttranlater=new H2EntityTranslater();
						while(matcher_code_in.find(find_index)){
							String string_code_in=matcher_code_in.group();
							int start_in=matcher_code_in.start();
							int end_in=matcher_code_in.end();
							//System.out.println("code p:"+string_code_in);
							//string_code_in是一块代码段:start_in~end_in
							//可能``````xxx``
							//也可能``xxxxx`````
							//也可能`````
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
								//string_code_in —> <code>string_code_out.charAt(start)</code>
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
									//不变化
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
						//string_code_out是替换过<code>的串，应当替换回stackstring中
						stackstring=StringUtils.replace(stackstring, string_code_out, start_out, end_out-1);
						
					}
					
					/*
					 * 此处需要分析行中的pattern_image,pattern_weburl
					 * 并且转换成html标签
					 * 
					 * 注意，一定要先image，后weburl
					 */
					//处理image格式的
					int find_index=0;
					Matcher matcher=pattern_image.matcher(stackstring);
					while(matcher.find(find_index)){
						int start_index=matcher.start();
						int end_index=matcher.end();
						String imagestring=matcher.group();	//这是stackstring中找到的目标串
						//分析imagestring，转换成标签格式
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
						//剔除了对应位置的字符串
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
						String urlstring=matcher.group();	//这是stackstring中找到的目标串
						//分析urlstring，转换成标签格式
						Matcher sqbrackets_matcher=pattern_squarebracket.
								matcher(urlstring);
						Matcher brackets_matcher=pattern_bracket.
								matcher(urlstring);
						String url_path="";	//实际上的链接路径
						String url_title="";	//url的title
						String url_display="";	//显示的链接名称
						//找到了方括号内容，即为显示出来的链接文字
						if(sqbrackets_matcher.find()){
							url_display=sqbrackets_matcher.group();
							url_display=url_display.substring(1,
									url_display.length()-1);
						}
						//找到了圆括号的内容
						//需要判断是xxx格式还是xxx "xxx"格式
						if(brackets_matcher.find()){
							url_path=brackets_matcher.group();
							url_path=url_path.substring(1,
									url_path.length()-1);
							//建立一个PathTitleUnit类对象
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
						//剔除了对应位置的字符串
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
						PathTitleUnit ptu=map_referdefine.get(image_id);
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
						PathTitleUnit ptu=map_referdefine.get(url_id);
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
						//剔除了对应位置的字符串
						
						stackstring=StringUtils.replace(stackstring,url_string,
								start_index,end_index-1);
						find_index=start_index+url_string.length();
						matcher=pattern_squarebracket2.matcher(stackstring);
					}
					
					
					
					stackstring=stackstring+stackendstring;	//拼接上</..></..>
					if(linetext.type==TEXTTYPE_NORMAL){
						stackstring="<p>"+stackstring+"</p>";
						if(linetext.isblockquote_end){
							//把引用深度的结束标签添上
							for(int i=0;i<linetext.blockquote_depth;i++){
								stackstring+="</blockquote>";
							}
						}
					}else if(linetext.type==TEXTTYPE_ENDNORMALTEXT){
						stackstring="<p>"+stackstring+"<br/></p>";
						if(linetext.isblockquote_end){
							//把引用深度的结束标签添上
							for(int i=0;i<linetext.blockquote_depth;i++){
								stackstring+="</blockquote>";
							}
						}
					}else if(linetext.type==TEXTTYPE_TITLE_EQUAL){
						stackstring="<h1>"+stackstring+"</h1>";
					}else if(linetext.type==TEXTTYPE_TITLE_JING){
						if(linetext.isblockquote_end){
							//把引用深度的结束标签添上
							for(int i=0;i<linetext.blockquote_depth;i++){
								stackstring+="</blockquote>";
							}
						}
					}else if(linetext.type==TEXTTYPE_LIST){
						stackstring="<li>"+stackstring+"</li>";
						//加上列表头：
						if(linetext.islist_head){
							if(linetext.islist_order){
								stackstring="<ol>"+stackstring;
							}else{
								stackstring="<ul>"+stackstring;
							}
						}
						//加上列表尾
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
						
					if(VDBG){
						System.out.println("stackstring:"+stackstring);
					}
					//再放置回al_LineString
					linetext.content=stackstring;	//将文本替换成magic_stack中生加工过的
				}
				
				
				//如果当前文本行是- - -- --
				else if(linetext.type==TEXTTYPE_SBLINE_DISCONTINUOUS||
						linetext.type==TEXTTYPE_STARLINE){
					linetext.content="<hr>";
				}
			}
		
		Iterator<LineText> iter=al_LineString.iterator();
		StringBuilder html_stringbuilder=new StringBuilder();
		while(iter.hasNext()){
			LineText lt_get=iter.next();
			html_stringbuilder.append(lt_get.content+"\n");
		}
		return html_stringbuilder.toString();
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
	
	//打印出每一行的文本内容和文本类型
	private void printLineStrings(){
		if(this.al_LineString!=null){
			Iterator<LineText> iter=al_LineString.iterator();
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
		if(map_referdefine!=null){
			Set<String> set=map_referdefine.keySet();
			Iterator<String> it=set.iterator();
			while(it.hasNext()){
				String key=it.next();
				PathTitleUnit ptu=map_referdefine.get(key);
				System.out.println("id:"+key+",path:"+ptu.getPath()+",title:"+ptu.getTitle());
				
			}
		}
	}
	
	private void printStack_tail(){
		System.out.println(stack_listtail);
	}
}
