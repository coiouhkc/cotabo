package app.taskboard

import grails.test.*
import groovy.time.TimeCategory

class DashboardServiceTests extends TaskBoardUnitTest {
	def dashboardService 
    protected void setUp() {
        super.setUp()
		dashboardService = new DashboardService()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGetColumnStatusForDate() {
		def column = Column.findByName('wip')
		def entry 
		use(TimeCategory) {
			entry = dashboardService.getColumnStatusForDate(column, startDate + 27.hours)
			assertNotNull entry
			assertEquals ColumnStatusEntry.findByDateCreatedAndColumn(startDate+1.days, column), entry
		}		
    }	
	
	void testGetCDFDataForColumn() {
		
		//our startDate (so the latest one) was 
		//Always starting with a 0 value for the first timestamp
		//for better graph output.
		def expected = '''1301757193013,0
1301757193013,1
1301843593013,2
1301929993013,3
1302016393013,4
1302102793013,5
1302189193013,6
1302275593013,7
1302361993013,8
1302448393013,9
1302534793013,10
1302621193013,11
1302707593013,12
1302793993013,13
1302880393013,14
1302966793013,15
1303053193013,16
1303139593013,17
1303225993013,18
1303312393013,19
1303398793013,20
'''	
		def result = dashboardService.getCDFDataForColumn(Column.findByName('done'))
		def resultList = result.readLines()
		//Need to remove the last line as this is the current timestamp
		//which is not really testable
		resultList.pop()
		
		
		assertEquals expected.readLines(), resultList
	}
	
	void testGetCDFDataForColumnWithDateRestriction() {
		
		//our startDate (so the latest one) was
		def expected = '''1302016393013,0
1302016393013,4
1302102793013,5
1302189193013,6
1302275593013,7
1302361993013,8
1302448393013,9
1302534793013,10
1302621193013,11
'''
		def from
		def too
		use(TimeCategory) {
			//Including the 3rd day where we had 4 tasks in (as we start with 1 task on the day 0)
			from = startDate + 3.days - 20.seconds			
			//Including the 10th day where we had 11 tasks in
			too = startDate + 10.days + 4.hours + 20.seconds
		}
		def result = dashboardService.getCDFDataForColumn(Column.findByName('done'), from, too)
		def resultList = result.readLines()
		//Need to remove the last line as this is the current timestamp
		//which is not really testable
		resultList.pop()
		
		//Comparing both line collections		
		assertEquals expected.readLines(), resultList
	}
	
	void testGetAverageCycleTime() {
		def board = Board.findByName('myboard')
		//Our workflowStartColumn is 'wip' and every tasks needs 4 hours to be handled
		long expected = 4*60*60*1000					
		assertEquals expected, dashboardService.getAverageCycleTime(board)
		
	}
	
	void testGetLeadTimeData() {
		def expected = '''1301757193013,4
1301843593013,4
1301929993013,4
1302016393013,4
1302102793013,4
1302189193013,4
1302275593013,4
1302361993013,4
1302448393013,4
1302534793013,4
1302621193013,4
1302707593013,4
1302793993013,4
1302880393013,4
1302966793013,4
1303053193013,4
1303139593013,4
1303225993013,4
1303312393013,4
1303398793013,4
'''
		def board = Board.findByName('myboard')
		assertEquals expected, dashboardService.getLeadTimeData(board)
	}
	
	void testGetLeadTimeDataWithDateRestrictions() {
		def expected = '''1302016393013,4
1302102793013,4
1302189193013,4
1302275593013,4
1302361993013,4
1302448393013,4
1302534793013,4
1302621193013,4
'''
		def from
		def too
		use(TimeCategory) {
			//Including the 3rd day where we had 4 tasks in (as we start with 1 task on the day 0)
			from = startDate + 3.days - 20.seconds			
			//Including the 10th day where we had 11 tasks in
			too = startDate + 10.days + 4.hours + 20.seconds
		}
		
		def board = Board.findByName('myboard')
		assertEquals expected, dashboardService.getLeadTimeData(board, from, too)
	}
	
	void testGetTaskCountInWorkflowData() {
		//We just expect one task to be there - see our test data in TaskBoardUnitTest
		def expected = "${this.startDate.time},1"
		assertEquasl expected, dashboardService.getTaskCountInWorkflowData(Board.findByName('myboard'))
	}
}