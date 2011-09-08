package org.cotabo

import grails.test.*
import groovy.time.TimeCategory


/**
 * Representation of a TaskBoard unit test class.
 * This mocks a complete domain that can be used by all subsequent unit tests.
 * 
 * @author Robert Krombholz
 *
 */
class TaskBoardUnitTest extends GrailsUnitTestCase {
	
	protected taskService
	protected final Date startDate = Date.parse("dd/MM/yyyy HH:mm:ss SSS z", "02/04/2011 13:13:13 013 GMT+2:00")
	
    protected void setUp() {
        super.setUp()
		taskService = new TaskService()
		
		//Mocking the security service (only principal as this is enough for our purposes
		def springSecurityExpando = new Expando()				
		springSecurityExpando.metaClass.getPrincipal = {return ['username':'testuser']}
		taskService.springSecurityService = springSecurityExpando
		assertEquals 'testuser', springSecurityExpando.principal.username
		
		//Mocking the configuration that we need
		mockConfig '''
		taskboard.colors = ['#faf77a', '#fa7a88', '#bcbcf5', '#f9d7a9']
		taskboard.priorities = ['Critical', 'Major', 'Normal', 'Low']
		// Added by the Spring Security Core plugin:
		grails.plugins.springsecurity.userLookup.userDomainClassName = 'org.cotabo.User'
		grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'org.cotabo.UserRole'
		grails.plugins.springsecurity.authority.className = 'org.cotabo.Role'
		grails.plugins.springsecurity.securityConfigType = SecurityConfigType.Annotation
		'''
		
		mockDomain(User,[
			new User(
				username: 'testuser',
				password: 'testpassword',
				firstname: 'firstname',
				lastname: 'lastname',
				email: 'e@mail.com'
			)]
		)
		//No need to mock this with data currently
		mockDomain(Role)
		mockDomain(UserRole)
		
		//Board>Column/User relationship mocking
		def column1 = new Column(name:'todo')
		def column2 = new Column(name:'wip', workflowStartColumn:true)
		def column3 = new Column(name:'done', workflowEndColumn:true)
		def board = new Board(name:'myboard')
		def user = User.findByUsername('testuser')
		board.columns = [column1, column2, column3]		
		column1.board = board
		column2.board = board
		column3.board = board
		
		mockDomain(Column, [column1, column2, column3])
		mockDomain(Board, [board])
		mockDomain(Task)
		mockDomain(Block)		
		mockDomain(ColumnStatusEntry)
		mockDomain(UserBoard)
		UserBoard.create(user, board, RoleEnum.ADMIN)
		
		
		//We need to disable the overwriting of the dateCreated field
		//the mock is too good four our purposes as during tests
		//We only want to manually set the dateCreated
		//We just reject the value if it is already set
		def newSetDateCreated = { Date d ->
			if (!delegate.@dateCreated) {
				delegate.@dateCreated = d
			}
		}				
		ColumnStatusEntry.metaClass.setDateCreated = newSetDateCreated
		

		//Mock tasks in the first column
		for(i in 1..20) {
			taskService.saveTask (new Task(name: "testtask$i", durationHours: 0.5, column: Column.findByName('todo'),
				creator: user, sortorder: i, priority: 'Critical'), startDate).addToColors(new TaskColor(color:'#faf77a', name:'none'))	
		}

		//Re-setting the todo as we need the IDs generated by hibernate (or the mock)
		def todo = Task.list()
		//This is the simulated order of the target column
		def orderedTaskIdListTodo= []
		def orderedTaskIdListWip = []
		def orderedTaskIdListDone = []
		//Moving tasks over the whole board. 1 Per day over both columns too the end
		todo.eachWithIndex { task, idx->
			//We just put everything in the lists - we don't care about gaps here when tasks are moved
			orderedTaskIdListTodo << task.id
			orderedTaskIdListWip << task.id
			orderedTaskIdListDone << task.id
			// starting from my birthday :P			
			use(TimeCategory) {			
				def movementMessage1 = [
					task: task.id,
					fromColumn: Column.findByName('todo').id,
					toColumn: Column.findByName('wip').id,
					newTaskOrderIdList: orderedTaskIdListWip
				]
				def movementMessage2 = 	[
					task: task.id,
					fromColumn: Column.findByName('wip').id,
					toColumn: Column.findByName('done').id,
					newTaskOrderIdList: orderedTaskIdListWip
			]
				//Move too wip
				taskService.moveTask movementMessage1, startDate+idx.days
				//We emulate keeping each tasks for 4 hour in the WIP column
				taskService.moveTask movementMessage2, startDate+(idx.days+4.hours)			
			}								
		}		
		//Now we realle move the tasks too done
		todo*.column = Column.findByName('done')
		todo*.save()
		
		//After doing all this stuff we want to add at least 1 task to todo & wip
		def todoCol = Column.findByName('todo')
		def wipCol = Column.findByName('wip')
		//We save a todo task for further testing 
		taskService.saveTask(
			new Task(name: "todotask", durationHours: 0.5, column: todoCol,
				creator: user, sortorder: 100, color: new TaskColor(color:'#faf77a', name:'none'), priority: 'Critical'),
			startDate)
		//We save a wip task for further testing
		taskService.saveTask(
			new Task(name: "wiptask", durationHours: 0.5, column: wipCol,
				creator: user, sortorder: 100, color: new TaskColor(color:'#faf77a', name:'none'), priority: 'Critical'),
			startDate)
		todoCol.addToTasks(Task.findByName('todotask'))
		wipCol.addToTasks(Task.findByName('wiptask'))
    }

    protected void tearDown() {
        super.tearDown()
    }
	
	
	void testOwnMockup() {		
		//Extecting 100 ColumnStatusEntries 
		//120 = 40 moves
		//+ 60 = 20 creates
		//+ 2 (for 2 single task creations - creation created only 1 ColumnStatusEntry)
		assertEquals 186, ColumnStatusEntry.list().size()			
		
		use(TimeCategory) {			
			//Expecting 3 columnStatusEntries as we expect 1 move on the startDate.2
			assertEquals 3, ColumnStatusEntry.findAllByDateCreated(startDate+2.days).size()
		}
		assertNotNull Board.findByName('myboard')
		assertNotNull Column.findByName('todo')
		assertEquals 1, Column.findByName('todo').tasks.size()
		assertNotNull Column.findByName('wip')
		assertEquals 1, Column.findByName('wip').tasks.size()
		assertNotNull Column.findByName('done')
		assertEquals 20, Column.findByName('done').tasks.size()
		assertNotNull User.findByUsername('testuser')
		def todoTask = Task.findByName('todotask')
		def wipTask = Task.findByName('wiptask') 
		assertNotNull todoTask
		assertNotNull todoTask.column
		assertNotNull wipTask
		assertNotNull wipTask.column
	}
}
