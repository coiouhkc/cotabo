package app.taskboard

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

	void testUpdateSortOrder() {		
		def tasks = [1,3,2,4,5]		
		//We need to be authenticated for that
		SpringSecurityUtils.doWithAuth('user') {
			taskService.updateSortOrder(tasks)
		}
		def taskList = tasks.collect{Task.get(it)}
		
		assertEquals taskList.collect{it.id}, Column.get(1).tasks.collect{it.id}	
	}
	
	void testMoveTask() {
		def fromColumn = 1
		def tooColumn = 2
		def task = 3
		def order =[6,3,7]
				
		def result
		//We need to be authenticated for that
		SpringSecurityUtils.doWithAuth('user') {
			result = taskService.moveTask(order, fromColumn, tooColumn, task)
		}
		
		assertEquals '', result
		
		assertEquals 3, Column.get(2).tasks.size()
		assertEquals order, Column.get(2).tasks.collect{it.id}
		
		//Testing the generated events
		assertEquals 1, TaskMovementEvent.list().size()
		assertEquals User.findByUsername('user'), TaskMovementEvent.list()[0].user
		assertEquals 2, ColumnStatusEntry.list().size()
		
		def expectedTasksForColumn1 = Column.get(1).tasks.collect{it.id}
		def expectedTasksForColumn2 = Column.get(2).tasks.collect{it.id}
		
		assertEquals expectedTasksForColumn1, ColumnStatusEntry.findByColumn(Column.get(1)).tasks.collect{it.id}
		assertEquals expectedTasksForColumn2, ColumnStatusEntry.findByColumn(Column.get(2)).tasks.collect{it.id}
		
		task = 4
		order = [6,3,4,7]
		
		//We need to be authenticated for that
		SpringSecurityUtils.doWithAuth('user') {
			//Move another task an see whether the tasks on the event objects stay the same
			result =  taskService.moveTask(order, fromColumn, tooColumn, task)
		}
		assertEquals '', result
		assertEquals 2, TaskMovementEvent.list().size()
		assertEquals 4, ColumnStatusEntry.list().size()
		//See whether the initial events still have the initial task set
		assertEquals expectedTasksForColumn1, ColumnStatusEntry.findAllByColumn(Column.get(1))[0].tasks.collect{it.id}
		assertEquals expectedTasksForColumn2, ColumnStatusEntry.findAllByColumn(Column.get(2))[0].tasks.collect{it.id}
		//See whether the new events have the real set of tasks
		def newExpectedTasksForColumn1 = Column.get(1).tasks.collect{it.id}
		def newExpectedTasksForColumn2 =Column.get(2).tasks.collect{it.id}
		assertEquals newExpectedTasksForColumn1, ColumnStatusEntry.findAllByColumn(Column.get(1))[1].tasks.collect{it.id}
		assertEquals newExpectedTasksForColumn2, ColumnStatusEntry.findAllByColumn(Column.get(2))[1].tasks.collect{it.id}
		
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
		
		//Check if our tasks appears in the last ColumnStatusEntry
		assertNotNull ColumnStatusEntry.findAllByColumn(Column.findByName('ToDo')).last().tasks.find{it==task}
		
		assertEquals 1, TaskMovementEvent.findAllByTask(task).size()

		
	}
}

