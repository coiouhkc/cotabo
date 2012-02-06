package org.cotabo

import grails.test.mixin.domain.DomainClassUnitTestMixin

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ColorService)
@TestMixin(DomainClassUnitTestMixin)
class ColorServiceTests {

    void testCreate() {
		mockDomain(TaskColor)
		
		def taskColor = [name:"Tag for Me", color:"#123456"] as TaskColor
		def service = new ColorService()
		
		service.create(taskColor)
		
		assertNotNull TaskColor.findByName("Tag for Me")
		assertNull TaskColor.findByColor("123456")
		assertNotNull TaskColor.findByColor("#123456")
		
		taskColor = TaskColor.findByName("Tag for Me")
		
		assertNotNull taskColor.id
		assertTrue taskColor.active
        
    }
	
	void testRead() {
		mockDomain(TaskColor)
		
		def taskColor = [name:"Tag for Me", color:"#123456"] as TaskColor
		def service = new ColorService()
		
		service.create(taskColor)
		taskColor = service.read("Tag for Me")
		
		assertNotNull taskColor.id
		assertTrue taskColor.active
		assertEquals taskColor.name, "Tag for Me"
		assertEquals taskColor.color, "#123456"
	}
	
	void testUpdate() {
		mockDomain(TaskColor)
		
		def taskColor = [name:"Tag for Me", color:"#123456"] as TaskColor
		def service = new ColorService()
		
		service.create(taskColor)
		taskColor = service.read("Tag for Me")
		
		taskColor.name = "Tag for Me was here"
		service.update(taskColor)
		
		taskColor = TaskColor.findByColor("#123456")
		
		assertNotNull taskColor.id
		assertTrue taskColor.active
		assertEquals taskColor.name, "Tag for Me was here"
		assertEquals taskColor.color, "#123456"
	}
	
	void testDelete() {
		mockDomain(TaskColor)
		
		def taskColor = [name:"Tag for Me", color:"#123456"] as TaskColor
		def service = new ColorService()
		
		service.create(taskColor)
		taskColor = service.read("Tag for Me")
		
		service.delete(taskColor)
		
		taskColor = TaskColor.findByColor("#123456")
		
		assertNotNull taskColor
		
		assertNotNull taskColor.id
		assertFalse taskColor.active
		assertEquals taskColor.name, "Tag for Me"
		assertEquals taskColor.color, "#123456"
	}
	
	void testAssignColorBoard() {
		mockDomain(TaskColor)
		mockDomain(Board)
		
		def board = [name: "test board", columns:[]] as Board
		def taskColor = [name:"Tag for Me", color:"#123456"] as TaskColor
		
		def service = new ColorService()
		service.assign(board, taskColor)
		
		assertNotNull TaskColor.findByName("Tag for Me")
		assertNotNull Board.findByName("test board")
		
		board = Board.findByName("test board")
		taskColor = TaskColor.findByName("Tag for Me")
		
		assertNotNull board.colors
		assertTrue board.colors.size()>0
		//assertNotNull taskColor.board //taskColor has no property board
		
		assertNotNull Board.list().collect{taskColor in it.colors}
		assertTrue Board.list().collect{taskColor in it.colors}.size()>0
		//assertNotNull TaskColor.findByBoard(board) // taskColor has not property board
	}
	
	void testUnassignColorBoard() {
		mockDomain(TaskColor)
		mockDomain(Board)
		
		def board = [name: "test board", columns:[]] as Board
		def taskColor = [name:"Tag for Me", color:"#123456"] as TaskColor
		
		def service = new ColorService()
		service.assign(board, taskColor)
		
		service.unassign(board, taskColor)
		
		board = Board.findByName("test board")
		taskColor = TaskColor.findByName("Tag for Me")
		
		assertNotNull board.colors
		assertEquals 0, board.colors.size()
		
		assertNotNull Board.list().collect{taskColor in it.colors}
		assertEquals 0, Board.list().count{taskColor in it.colors}
		
	}
}
