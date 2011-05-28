<jq:jquery>
	/**
	 * Callback function for task create form submit.
	 */
	var submitCallback = function(data, textStatus, jqXHR) {
		//See utils.js for definition of checkForSuccess
		if (checkForSuccess(data, '#createTaskForm')) {						
			$('div.column:first > ul').append(data);
			$('div.column:first > ul > li:last >').scroll();
			setElementCountOnColumn();	
			$('#createTaskForm').dialog('close');			
			$('div.column:first > ul > li:last > .task-header').effect('highlight', {}, 600);
					
		}
		return false;
	}	
	
	/**
	 * Submit event overwrite for the task create form.
	 */	
	$('#taskForm').submit(function() {		
		$.ajax({		
			type: 'POST',
			url: $(this).attr('action'),
			data: $(this).serialize(),				
			success: submitCallback
		 });					 
		 //Don't really submit
		 return false;
	});
	
	$('#color > option').each(function() {
		$(this).attr("style", "background-color:"+this.value);
	});
	var updateSelectColor = function() {		
		$('#color').attr("style", $('#color > option:selected').attr("style"));
	}
	updateSelectColor();
	$('#color').change(updateSelectColor);
	
</jq:jquery>
<div id="createTaskForm" title="new task">	
    <g:hasErrors bean="${boardInstance}">
	    <div class="errors">
	    <g:renderErrors bean="${boardInstance}" as="list" />
	    </div>
    </g:hasErrors>
    <g:render template="/info" model="[messagecode: 'form.required.fields']"/>  
    <g:form controller="task" action="save" name="taskForm">
    <div>
    	<table>
    		<tbody>
    			<tr>
    				<td>
    					<label for="name" alt="A short task name">name *</label>
    				</td>
    				<td>
    					<g:textField name="name" maxlength="25"/>
    				</td>
    			</tr>
    			<tr>
    				<td>
    					<label for="description" alt="A description of the Task" class="optional">description</label>
    				</td>
    				<td>
    					<g:textArea name="description" cols="30" rows="3"/>
    				</td>
    			</tr>
 			    <tr>
    				<td>
    					<label for="assignee" alt="The assignee for this task" class="optional">assignee</label>
    				</td>
    				<td>
    					<g:set var="assignees" value="${(boardInstance.users +  boardInstance.admins).unique()}"/>
    					<g:select name="assignee" 
    						from="${assignees}" 
    						noSelection="${['':'']}"
    						value=""
    						optionKey="id"
						/>
    				</td>
    			</tr>
    			<tr>
    				<td>
    					<label for="duration" alt="The duration of the task (in hours)" class="optional">duration (h)</label>
    				</td>
    				<td>
    					<g:textField name="duration" size="3"/>
    				</td>
    			</tr>
    			<tr>
    				<td>
    					<label for="priority" alt="The priority of this task">priority *</label>
    				</td>
    				<td>
    					<g:select from="${priorities}" value="${grailsApplication.config.taskboard.default.priority}" name="priority"></g:select>
    				</td>
    			</tr>
    			<tr>
    				<td>
    					<label for="color" alt="The color with this this task gets displayed">color *</label>
    				</td>
    				<td>
    					<g:select from="${colors}" value="${grailsApplication.config.taskboard.default.color}" name="color"></g:select>
    				</td>
    			</tr>
    		</tbody>
    	</table>
    </div>
    <g:hiddenField name="board" value="${boardInstance.id}"/>
    </g:form>
</div>