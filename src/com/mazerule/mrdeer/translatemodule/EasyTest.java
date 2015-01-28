package com.mazerule.mrdeer.translatemodule;

import java.util.LinkedList;
import java.util.ListIterator;

public class EasyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkedList<String> ll=new LinkedList<String>();
		ll.add("ab");
		ll.add("cd");
		ll.add("ef");
		ll.add("gh");
		System.out.println(ll);
		
		ListIterator<String> li=ll.listIterator(ll.size());
		while(li.hasPrevious())
		{
			System.out.println(li.previous());
		}
	}

}
