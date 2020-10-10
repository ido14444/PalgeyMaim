package com.palgeymaim.client.entity;

public class IdModel {
	
	public String id;
	public String name;
	
	public IdModel(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	@Override
	public String toString(){
		return name;
	}

}
