package com.mazerule.mrdeer.translatemodule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Markdown�ı���Html�ı���ת����
 */
public class M2HTranslater {

	//ͨ����������������ԭʼ�ַ���
	private String origin_string;
	
	//<�к�,���ַ���>
	private ArrayList<LineText> al_LineString;	//���ı�
	private LinkedList<Boolean> stack_listtail;	//�б����β��ջ
	public boolean DBG=false;
	public boolean VDBG=false;
	public boolean VVDBG=true;
	
	/*
	 * �ı�������
	 */
	private final static int TEXTTYPE_NORMAL=10;  //��ͨ�ı�����
	private final static int TEXTYPE_SPACELINE=11;	//�հ׵��ı�����
	private final static int TEXTTYPE_TITLELINE=12;	//=====
	private final static int TEXTTYPE_TITLE_JING=13;	//���⣬���������ڶ����Ķ�(##)
	private final static int TEXTTYPE_TITLE_EQUAL=14;	//���⣬���������ڶ����Ķ�(==)
	private final static int TEXTTYPE_SBLINE_CONTINUOUS=15;//---������ 
	private final static int TEXTTYPE_SBLINE_DISCONTINUOUS=16;	//- --�������ո��-
	private final static int TEXTTYPE_ENDNORMALTEXT=17;	//��������ո����ͨ�ı�����
	private final static int TEXTTYPE_STARLINE=18;	//** *�������пո�\
	private final static int TEXTTYPE_LIST=19;	//�б����͵��ı���
	
	//�����ڲ���LineText�ж��ı�������
	static final Pattern pattern_spaceline=Pattern.compile("\\s*");
	static final Pattern pattern_titleline=Pattern.compile("={3,}");
	static final Pattern pattern_title_jing=Pattern.compile("[#]{2,}.*");
	static final Pattern pattern_sbline_continuous=Pattern.compile("-{3}");
	static final Pattern pattern_sbline_discontinuous=Pattern.compile("(\\s*-\\s*){3,}");
	//static final Pattern pattern_endnormaltext=Pattern.compile("");
	static final Pattern pattern_starline=Pattern.compile("(\\s*\\*\\s*){3,}");
	static final Pattern pattern_blockquote=Pattern.compile("\\s*>+.+");
	static final Pattern pattern_list=Pattern.compile("\\s*([*+-]|[0-9]+\\.)\\s+.+");
	static final Pattern pattern_image=Pattern.compile("");
	static final Pattern pattern_weburl=Pattern.compile("");
	
	//���ı���
	class LineText{
		String content;	//�ı�����
		int type;	//���ı�������
		int blockquote_depth;	//�������ÿ����ȣ���ֵΪ0
		boolean isblockquote_start;//�Ƿ���һ�����ÿ��ͷ
		boolean isblockquote_end;//�Ƿ������ÿ�Ľ�β
		int list_len;	//�б������
		boolean islist_order;	//true���������б�false���������б�
		boolean islist_head;	//�Ƿ���ĳ���б�ĵ�һ��
		int list_tailnum;	//�б��β����
		LinkedList<Boolean> list_tailstack;	//�б��βջ
		
		//����ʱֻ�ܴ����ı����ݡ�
		LineText(String c){
			content=c;
			//�����������ȣ��������÷���ȥ��
			blockquote_depth=caculateBlockquoteDepth();
			type=judgeType(content);
			caculateListParam();
			isblockquote_end=false;
		}
		
		//����list��صĲ���
		void caculateListParam(){
			list_len=0;
			islist_head=false;
			list_tailnum=0;
			if(type!=TEXTTYPE_LIST){
				return;
			}
			int index=0;
			//����list_len�Ĵֲ�ֵ(Ҫ�ڵڶ���ɨ���н��е���)
			list_len=1;	//��ֵ��1
			while(index<content.length()){
				if(content.charAt(index)==' '){
					list_len++;
				}else{
					//��ʱ�ж�list������ͣ�ul������ol������
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
			//ȥ���ı�����'+','*','100.'�����б����
			while(index<content.length()){
				if(content.charAt(index)!=' '){
					index++;
				}else{
					break;
				}
			}
			content=content.substring(index);
		}
		
		//�����ı��е��������
		int caculateBlockquoteDepth(){
			int quotecount=0;
			//����ģʽƥ��ɹ���
			if(pattern_blockquote.matcher(content).matches()){
				//��ʱֻҪ�������÷��ŵģ�����Ϊ�����ÿ�ʼ����������ɨ���ϸ��
				isblockquote_start=true;	
				int index=0;
				//ɨ���ַ�������������'>'����
				while(index<content.length()){
					//һ����û���ţ����Ҳû���ţ�Ҫ������
					if(quotecount==0&&content.charAt(index)!='>'){
						index++;
						continue;
					}
					//��������ˣ�������һ��
					if(content.charAt(index)=='>'){
						quotecount++;
						index++;
						continue;
					}
					//�Ѿ����Ź��ˣ�����β���
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
			}else if(pattern_title_jing.matcher(content).matches()){
				return TEXTTYPE_TITLE_JING;
			}else if(pattern_list.matcher(content).matches()){
				return TEXTTYPE_LIST;
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
	private final static int ELEMENTTYPE_OP_BR=15;	//\n
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
		stack_listtail=new LinkedList<Boolean>();
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
			//��һ��ɨ��
			//һ��һ���ı����ֵ���al_LineString�ͬʱ�жϳ�����
			for(int i=0;i<string_arr.length;i++){
				if(VVDBG){
					System.out.println("splitString!");
				}
				al_LineString.add(new LineText(string_arr[i]));
			}
			//��ʱ�Ѿ���������\n����������ı��ֿ������ұ�������͡�
			//��Ҫ��һ���������ı���ʹ���Զ���Ϊ���ı��ĵ�λ
			//�ڶ���ɨ��,ȥ������Ŀհ��У���TEXTTYPE_TITLELINE����ɾ��
			for(int i=0;i<al_LineString.size();i++){
				LineText lt_curr=al_LineString.get(i);	//��ǰ��LineText
				LineText lt_next=null;	//��һ��text
				if(i+1<al_LineString.size()){
					lt_next=al_LineString.get(i+1);
				}
				if(lt_next==null){
					if(lt_curr.type==TEXTTYPE_LIST){
						lt_curr.list_tailnum=lt_curr.list_len;
						lt_curr.list_tailstack=new LinkedList<Boolean>();
						//���ı��е�list_tailstack��Ա�м���Boolean����,���ҽṹ��stack_listtailһ��
						for(int j=0;j<lt_curr.list_tailnum;j++){
							lt_curr.list_tailstack.addLast(stack_listtail.pop());
						}
						
					}
					break;
				}
				//�����ǰ�ı�����һ����ͨ�ı�/�����ı���������һ���ı�����====������-----
				if((lt_curr.type==TEXTTYPE_NORMAL
						||lt_curr.type==TEXTTYPE_ENDNORMALTEXT)&&
						(lt_next.type==TEXTTYPE_TITLELINE||
						lt_next.type==TEXTTYPE_SBLINE_CONTINUOUS)){
					//����ǰ�ı���type�ĳ�TEXTTYPE_TITLE
					lt_curr.type=TEXTTYPE_TITLE_EQUAL;
					lt_curr.blockquote_depth=0;	//�����ı��в���λ��������
					//===/----����TEXTTYPE_TITLELINE�ı����Ѿ�ʧȥ���ǵļ�ֵ��ɾ��
					al_LineString.remove(i+1);
					continue;
				}
				//��ǰ�У���һ�ж��ǿհ���
				if(lt_curr.type==TEXTYPE_SPACELINE&&
						lt_next.type==TEXTYPE_SPACELINE){
					//ɾ������һ��
					al_LineString.remove(i+1);
					//�˻�һ���´�ѭ����Ҫ�ӱ��հ��п�ʼ
					i--;
					continue;
				}
				//��ǰ���Ǹ��ı��ҿ���Ӱ��������У���Ϊ���Ǿ���������ȵ�
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
						lt_next.isblockquote_start=false;	//��һ���ı��������ÿ�ʼ
					}
				}
				//�����б��һ����
				if(lt_curr.type!=TEXTTYPE_LIST&&lt_next.type==TEXTTYPE_LIST){
					lt_next.islist_head=true;
					lt_next.list_len=1;	//��һ���б����list_len������1
					stack_listtail.push(lt_next.islist_order);
				}
				//��ǰ�ı�����һ�ı�����list
				if(lt_curr.type==TEXTTYPE_LIST&&lt_next.type==TEXTTYPE_LIST){
					//��һ���ı������б���
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
				//�����б����һ����
				if(lt_curr.type==TEXTTYPE_LIST&&lt_next.type!=TEXTTYPE_LIST){
					lt_curr.list_tailnum=lt_curr.list_len;
					lt_curr.list_tailstack=new LinkedList<Boolean>();
					for(int j=0;j<lt_curr.list_tailnum;j++){
						lt_curr.list_tailstack.addLast(stack_listtail.pop());
					}
				}
			}
			if(VVDBG){
				System.out.println("��2��ɨ���Ժ�:");
				printLineStrings();
				printStack_tail();
			}
			
			
			
			//������ɨ�裬��ͬһ��������ı��ϲ���һ��
			for(int i=0;i<al_LineString.size();i++){
				LineText lt_curr=al_LineString.get(i);	//��ǰ��LineText
				LineText lt_next=null;	//��һ��text
				if(i+1<al_LineString.size()){
					lt_next=al_LineString.get(i+1);
				}
				if(lt_next==null){
					break;
				}
				//��ǰ����ͨ�ı�����һ����Ҳ����ͨ�ı�/TEXTTYPE_ENDNORMALTEXT
				if(lt_curr.type==TEXTTYPE_NORMAL&&
						(lt_next.type==TEXTTYPE_NORMAL||
						lt_next.type==TEXTTYPE_ENDNORMALTEXT)){
					
					//��ǰ�в��������У���һ����������
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth>0){
						//��һ�о�ֹͣ��
						continue;
					}
					//������������
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth==0){
						//�ϲ������ı�����ǰ��LineText,ɾ��next
						lt_curr.content+=lt_next.content;
						lt_curr.type=lt_next.type;
						al_LineString.remove(i+1);
						i--;
						continue;
					}
					//����������
					if(lt_curr.blockquote_depth>0){
						//��ǰ�������ÿ�ʼ
						if(lt_curr.isblockquote_start==true){
							lt_curr.content="\t"+lt_curr.content;
						}
						//�����һ���Ǹ����ÿ�ʼ��Ҫ���롮\t\n��
						if(lt_next.isblockquote_start==true){
							lt_curr.content+="\n\t"+lt_next.content;
						}else{
							lt_curr.content+=lt_next.content;
						}
						//���е�isblockquote_start�����Ѿ����ӹ��˼�ֵ�ˣ�Ӧ����false
						lt_curr.isblockquote_start=false;
						lt_curr.type=lt_next.type;
						//���ֻ��Խ��Խ��
						lt_curr.blockquote_depth=lt_next.blockquote_depth;
						al_LineString.remove(i+1);
						i--;
						continue;
					}
					
				}
				//��ǰ��TEXTTYPE_ENDNORMALTEXT����һ����Ҳ����ͨ�ı�/TEXTTYPE_ENDNORMALTEXT
				if((lt_curr.type==TEXTTYPE_ENDNORMALTEXT)
						&&(lt_next.type==TEXTTYPE_NORMAL||
						lt_next.type==TEXTTYPE_ENDNORMALTEXT)){
					//�ϲ������ı�����ǰ��LineText,��\n���,ɾ��next
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth>0){
						//��һ�о�ֹͣ��
						continue;
					}
					//������������
					if(lt_curr.blockquote_depth==0&&lt_next.blockquote_depth==0){
						//�ϲ������ı�����ǰ��LineText,ɾ��next
						lt_curr.content+="\n"+lt_next.content;
						lt_curr.type=lt_next.type;
						al_LineString.remove(i+1);
						i--;
						continue;
					}
					//����������
					if(lt_curr.blockquote_depth>0){
						//��ǰ�������ÿ�ʼ
						if(lt_curr.isblockquote_start==true){
							lt_curr.content="\t"+lt_curr.content;
						}
						//�����һ���Ǹ����ÿ�ʼ��Ҫ���롮\t\n��
						if(lt_next.isblockquote_start==true){
							lt_curr.content+="\n\t"+lt_next.content;
						}else{
							lt_curr.content+="\n"+lt_next.content;
						}
						lt_curr.type=lt_next.type;
						//���е�isblockquote_start�����Ѿ����ӹ��˼�ֵ�ˣ�Ӧ����false
						lt_curr.isblockquote_start=false;
						//���ֻ��Խ��Խ��
						lt_curr.blockquote_depth=lt_next.blockquote_depth;
						al_LineString.remove(i+1);
						i--;
						continue;
					}
					
				}
				//#�����ı����Լ��ж�Ҫ��Ҫ�ڿ�ͷ����<blockquote>
				if(lt_curr.type==TEXTTYPE_TITLE_JING&&
						lt_curr.isblockquote_start==true){
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
						StackElement se_add=new StackElement(letter);
						StackElement se_top=magic_stack.peek();	//ջ��Ԫ��
						while(true){
							//ѹ�ո�ջԪ��
							if(se_add.type==ELEMENTTYPE_SPACE){
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
						}
						flag_isstackbottom=false;	//ջ���Ѿ�������ˣ���һ��Ԫ�ز���ջ��
					}
					
					/*
					 * �˴���Ҫ�������е�pattern_image,pattern_weburl
					 * ����ת����html��ǩ
					 */
					
					
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
					}else if(linetext.type==TEXTTYPE_TITLE_EQUAL){
						stackstring="<h1>"+stackstring+"</h1>";
					}else if(linetext.type==TEXTTYPE_TITLE_JING){
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
		}
		else{	//����ʧ��
			return "ERROR";
		}
		Iterator<LineText> iter=al_LineString.iterator();
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
	
	private void printStack_tail(){
		System.out.println(stack_listtail);
	}
}
