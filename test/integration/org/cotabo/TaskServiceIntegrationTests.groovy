package org.cotabo

import grails.test.*
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class TaskServiceIntegrationTests extends GrailsUnitTestCase {
	def taskService
	def springSecurityUtils
	
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

	//TODO: move the below test to unit tests as it doesn't really fit for integration tests
	/*
	void testUpdateSortOrder() {		
		def tasks = [1,3,2,4,5]		
		//We need to be authenticated for that
		SpringSecurityUtils.doWithAuth('user') {
			taskService.updateSortOrder(tasks)
		}
		def taskList = tasks.collect{Task.get(it)}
		
		assertEquals taskList.collect{it.id}, Column.get(1).tasks.collect{it.id}	
	}
	*/
	
	void testMoveTask() {
		def result
		def order = [1,3,2]
		def expectedTaskMovementEventSize = TaskMovementEvent.list().size() + 1
		def expectedColumnStatusEntrySize = ColumnStatusEntry.list().size() + 2
		//We need to be authenticated for that
		SpringSecurityUtils.doWithAuth('user') {
			result = taskService.moveTask(
				task:3,
				fromColumn: 1,
				toColumn: 2,
				newTaskOrderIdList: order
			)
		}
		
		assertEquals '', result
		assertEquals 3, Column.findByName('In Progress').tasks.size()
		assertEquals order, Column.findByName('In Progress').tasks.collect{it.id}
		
		//Testing the generated events
		assertEquals expectedTaskMovementEventSize, TaskMovementEvent.list().size()
		assertEquals User.findByUsername('user'), TaskMovementEvent.list()[0].user
		assertEquals expectedColumnStatusEntrySize, ColumnStatusEntry.list().size()		

		//Test whether the assignment works
		assertEquals 'user', Task.get(3).assignee.username

		//We need to be authenticated for that
		SpringSecurityUtils.doWithAuth('user') {
			//Move another task an see whether the tasks on the event objects stay the same
			result =  taskService.moveTask(
				task:4,
				fromColumn: 1,
				toColumn: 2,
				newTaskOrderIdList: [6,3,4,7]
			)
		}
		assertEquals '', result
		assertEquals expectedTaskMovementEventSize + 1, TaskMovementEvent.list().size()
		assertEquals expectedColumnStatusEntrySize + 2, ColumnStatusEntry.list().size()				
		
	}
	
	void testSaveTask() {
		def col = Column.findByName('ToDo')
		def user = User.findByUsername('admin')
		
		def task = new Task(
			name: 'mytask',
			durationHours: 0.5,
			creator: user,
			sortorder: 100,
			priority: 'Critical',
			color: '#faf77a',
			column: col
		)

		//We need to be authenticated for that
		SpringSecurityUtils.doWithAuth('user') {
			task = taskService.saveTask(task)
		}
		assertEquals 0, task.errors.errorCount 
		
		//Check if our tasks appears in the last ColumnStatusEntry - was 5 - should be 6
		assertEquals 6, ColumnStatusEntry.findAllByColumn(Column.findByName('ToDo')).last().tasks		
		assertEquals 1, TaskMovementEvent.findAllByTask(task).size()	
	}
}