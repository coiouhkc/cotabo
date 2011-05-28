<%@ page import="app.taskboard.Board" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <link  href="//fonts.googleapis.com/css?family=Reenie+Beanie:regular" rel="stylesheet" type="text/css" >
        <link rel="stylesheet" href="${resource(dir:'css',file:'board.css')}" />
        <title>TaskBoard - ${boardInstance?.name}</title>
        <jq:jquery>  
    		/**
			 * Expand/Collapse tasks on click of the exapnd icon.
			 *
			 */
			var handleClickHeader = function(event) {
				//console.time('native'); 				
				var north = $(this).hasClass('ui-icon-carat-1-n');
				if (north) {
					$(this).removeClass('ui-icon-carat-1-n')
						.addClass('ui-icon-carat-1-s')
						.parent('div').next('div').css('display', 'none')					
				}
				else {
					$(this).removeClass('ui-icon-carat-1-s')
						.addClass('ui-icon-carat-1-n')
						.parent('div').next('div').css('display', 'block');										
				}		
				//console.timeEnd('native'); 
				return false;
			}	  
        
        	/**
        	 * Update all column task counts.
        	 * Setting this on the window as we need it in a different script block (task creation)
        	 */   
        	window.setElementCountOnColumn = function() {        		
    			$(".column").each(function(index){	    			
					var size = $(this).children("ul").children("li").size();
					if (size == null) {
						size = 0 ;
					}
					//Set the current number of elements to the element count for each column
					var pValueDom = $(this).children("span").children("span").children("p.value");
					var pLimitDom = $(this).children("span").children("span").children("p.limit");
					pValueDom.text(size);
					if (pValueDom.text() == pLimitDom.text() || parseInt(pValueDom.text()) > parseInt(pLimitDom.text())) {
						$(pValueDom).addClass('red-font');
					}
					else {
						if ($(pValueDom.hasClass('red-font'))) {
							pValueDom.removeClass('red-font');
						}
					}
				});
				
        	}  
  
        	
        	/**
        	 * Callback for a connected sortable receiving a new Task object.
        	 * Updating the server side and taking case of updating the column counts. 
        	 *
        	 */	        	             
        	var updateConnectedColumn = function(event, ui) {
        		var toColumnId = $(this).attr('id').split('_')[1];
				var taskId = $(ui.item).attr('id').split('_')[1]; 
        		
   				var fromColumnId = $(ui.sender).attr('id').split('_')[1];				
											
				//Post onto controller "column" and action "updatetasks"
				$.ajax({
					type: 'POST',
					url: '<g:createLink controller="column" action="updatetasks"/>',
					data: {
						'fromColumn': fromColumnId, 
						'toColumn': toColumnId,
						'taskid': taskId,
						'order': $(this).sortable("toArray")
					},
					success: function(data) {
						//Update the task counts on each column
						setElementCountOnColumn();
						//If an element is moved to the last column
						if (toColumnId == $(".column:last > ul").attr('id').split('_')[1]) {
							//and it it ellapsed			
							if ($(ui.item).children('div:first').children('span').hasClass('ui-icon-carat-1-n')) {
								//Collapse it
								$(ui.item).children('div:first').children('span').click()
							}
						}
					},
					error: function(xhr, errorType, exception) {
						$(ui.sender).sortable('cancel');
						alert('Error updateing the column: ' + errorType);
					}
				});				
			}

			
			/**
			 * Can be used for the stop event. Updating only the column sort order
			 */ 
			var updateColumn = function(event, ui) {
       			var toColumnId = $(this).attr('id').split('_')[1];
				var taskId = $(ui.item).attr('id').split('_')[1]; 
				$.ajax({
					type: 'POST',
					url: '<g:createLink controller="column" action="updatesortorder"/>/'+toColumnId,
					data: {
						order: $(this).sortable("toArray")
					},
					error: function(xhr, errorType, exception) {
						$(this).sortable('cancel');
						alert('Error updateing the column: ' + errorType);
					}
				});
			}
				
			//Sortable definition for the connected columns	
			$(".column > ul").each(function(index) {			
				$(this).sortable({			
					//Connect the current sortable only with the next column
					//connectWith:'.column ul:gt('+index+'):first',
					connectWith:'.column ul',
					appendTo: 'body',
					containment:"#board",
					cursor:"move",
					distance:30,
					opacity:0.7,
					placeholder:'ui-state-highlight',
					receive: updateConnectedColumn,
					stop: updateColumn
					
				});
			});	
			
			$(".task-content").enableSelection();
        	//Update on document load time.
        	setElementCountOnColumn();	 
			//Apply the click handle to all expand/collapse icons
			$('.task-header .ui-icon').live('click', handleClickHeader);			

        </jq:jquery>
    </head>
    <body>    	
    	<tb:board board="${boardInstance}">
    		<g:render template="menu"/>
    		<g:each in="${boardInstance.columns}" var="column">
    		<tb:column column="${column}">
    			<g:each in="${column.tasks}" var="task">
    			<g:if test="${(column == boardInstance.columns.first()) || (column == boardInstance.columns.last()) }">
    				<g:set var="hide" value="${true}" />  				
    			</g:if>
    			<g:else>
    				<g:set var="hide" value="${false}" />
    			</g:else>
    			<tb:task task="${task}" hide="${hide}"/>
    			</g:each>
    		</tb:column>
    		</g:each>
    	</tb:board>      	    
    </body>
</html>
