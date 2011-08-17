<%@ page import="org.cotabo.RoleEnum" %>
<div id="usermanagement">
    <div id="users_selected" class="um_div">
        <g:each in="${RoleEnum.values()}" var="role">            	       
	        <div>
	           <h4><tb:printRole role="${role}"/></h4>
	           <ul id="${role}" class="user_droppable user_list ui-state-default" 
	               title='<g:message code="user.role.${role}" default=""/>'>
	               <g:if test="${edit}">
	               <g:each in="${boardInstance.getUsers(role)}" var="user" status="i">
	               <li class="user_item ui-state-default ">
	                   <div class="div_user">
	                   ${user}
	                   <g:hiddenField name="${role}" value="${user.id}"/>
	                   </div>
	               </li>
	               </g:each>
	               </g:if>
	           </ul>       
	        </div>
        </g:each>
    </div>
    
    
    <div id="users_selectable" class="um_div">        
        <ul id="list_selectable" class="user_list ui-state-highlight">
            <g:each in="${allUsers}" var="user" status="i">            
            <li class="user_item ui-state-default ">
                <div class="div_user">
                    ${user}
                    <g:hiddenField name="user[${i}]" value="${user.id}"/>
                </div>
            </li>
            </g:each>
            
        </ul>
    </div>
</div>