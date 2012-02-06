package org.cotabo

class TaskColor {
	
	// TODO: named property Board
	
	static belongsTo = [Task, Board]
	static hasMany = [tasks : Task]
	
    static constraints = {
		name nullable:false, unique:true, blank:false
		color nullable:false, validator: {val, obj -> try {java.awt.Color.decode(val)} catch (Exception e){return false}; return true}
    }
	
	static exportables = ['name', 'color']
	
	String name
	String color
	boolean active
	
	@Override
	public String toString(){
		return color;
	}
}
