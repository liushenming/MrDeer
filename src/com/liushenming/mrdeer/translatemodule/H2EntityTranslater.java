package com.liushenming.mrdeer.translatemodule;

import java.util.Iterator;
import java.util.LinkedHashSet;
/*
 * Translate Html String to HTMl Entity String.
 */

public class H2EntityTranslater {
	
	//the original String.
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
	 * the core method,translate the html String to html entity.
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
	
	/**
	 * load a new String.
	 * @param string
	 */
	public void loadString(String string){
		if(string!=null){
			string_origin=new String(string);
		}else{
			string_origin="";
		}
	}
}
