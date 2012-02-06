package org.cotabo

class ColorService {
	
	static transactional = true
	
	def create(TaskColor color){
		color.active = true
		color.save(flush:true)
		return color
	}
	
	def read(def name){
		return TaskColor.findByName(name)
	}
	
	def update(TaskColor color){
		color.save(flush:true)
	}
	
	def delete(TaskColor color){
		color.active = false
	}

    def assign(Task task, TaskColor color) {
		task.addToColors(color)
		task.save(flush:true)
		color.save(flush:true)
    }
	
	def unassign(Task task, TaskColor color) {
		task.removeFromColors(color)
		task.save(flush:true)
		color.save(flush:true)
	}
	
	def assign(Board board, TaskColor color) {
		board.addToColors(color)
		board.save(flush:true)
		color.save(flush:true)
	}
	
	def unassign(Board board, TaskColor color) {
		board.removeFromColors(color)
		board.save(flush:true)
		color.save(flush:true)
	}
}
