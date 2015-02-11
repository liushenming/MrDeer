package com.liushenming.mrdeer.translatemodule;

import java.util.Iterator;
import java.util.LinkedHashSet;
/*
 * 将Html代码片段中所有的HTMl符号转换成HTML实体
 */

public class H2EntityTranslater {
	
	//传入的字符串
	private String string_origin="";
	private LinkedHashSet<EntityRule> ruleSet;
	
	//origin->entity
	private class EntityRule{
		String origin;
		String entity;
		public EntityRule(String os,String es){
			origin=os;
			entity=es;
		}
	}
	
	public H2EntityTranslater(){
		ruleSet=new LinkedHashSet<EntityRule>();
		ruleSet.add(new EntityRule("&","&amp;"));
		ruleSet.add(new EntityRule("<","&lt;"));
		ruleSet.add(new EntityRule(">","&gt;"));
		ruleSet.add(new EntityRule("\"","&quot;"));
		ruleSet.add(new EntityRule(" ","&nbsp;"));
		ruleSet.add(new EntityRule("©","&copy;"));
		ruleSet.add(new EntityRule("®","&reg;"));
		ruleSet.add(new EntityRule("'","&apos;"));
	}
	
	public H2EntityTranslater(String string){
		this();
		if(string!=null){
			string_origin=new String(string);
		}
	}

	/**
	 * 转换方法，返回转换为html实体的String
	 * @return
	 */
	public String translate(){
		if(string_origin!=null&&ruleSet!=null){
			Iterator<EntityRule> iter=ruleSet.iterator();
			while(iter.hasNext()){
				EntityRule er=iter.next();
				string_origin=string_origin.replaceAll(er.origin, er.entity);
			}
		}
		return string_origin;		
	}
	
	public void loadString(String string){
		if(string!=null){
			string_origin=new String(string);
		}else{
			string_origin="";
		}
	}
}
