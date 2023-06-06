package com.api.activiti.activiti.poc.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.activiti.activiti.poc.pojo.TaskInfoVo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessRestController {

	// @Autowired
	private final RuntimeService runtimeService;
	private final TaskService taskService;
	private final TaskRuntime taskRuntime;
	private final ProcessRuntime processRuntime;
//	private final Variablese
	
	@Value("classpath:stepconfig.json")
	private Resource stepconfig;
	
	private static ObjectNode configNode;
	
	@PostConstruct
	public void createJsonNode() {
		try {
			configNode = (ObjectNode) new ObjectMapper().readTree(stepconfig.getFile());
		} catch (IOException e) {
			log.error("Error loading json from classpath");
		}
	}

	@PostMapping("/create")
	public String createProcessWithData(@RequestBody String requestDetails) {
		ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("hhlprocess")
		.variable("requestDetails", requestDetails).variable("nextStep", "continue")
		.variable("stepId", "1").start();
		log.info("Created process instance {}", processInstance);
		return processInstance.getId();
	}

	@GetMapping("/task/{processInstanceId}")
	public List<TaskInfoVo> getTaskIdForInstanceId(@PathVariable String processInstanceId) {
		List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
		// List<Execution> list = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list();
		return taskList.stream().map(TaskInfoVo::mapToTaskInfo)
				.collect(Collectors.toList());
	}

	@GetMapping("/all-process/{processDefinitionKey}")
	public List<String> getActiveProcessInstanceByDefId(@PathVariable String processDefinitionKey) {
//		List<Task> taskList = taskService.createTaskQuery().processDefinitionKey(processDefinitionKey).list();
//		log.info("Task {}", taskList);
		List<Execution> list = runtimeService.createExecutionQuery().processDefinitionKey(processDefinitionKey).list();
		return list.stream().filter(item -> Objects.isNull(item.getParentId())).map(Execution::getId)
				.collect(Collectors.toList());
	}
	
	@PostMapping("/finish-task/{taskId}/{action}")
	public void finishTaskWithData(@PathVariable String taskId, @PathVariable String action,
			@RequestBody String requestDetails) {
		
		Task singleResult = taskService.createTaskQuery().includeProcessVariables().includeTaskLocalVariables()
				.taskId(taskId).singleResult();
		//Execution processInstanceInfo = runtimeService.createExecutionQuery().processInstanceId(singleResult.getProcessInstanceId()).singleResult();
		Map<String, Object> taskLocalVariables = singleResult.getTaskLocalVariables();
		log.info("variables {}", taskLocalVariables);
		Map<String, Object> processVariables = singleResult.getProcessVariables();
		String stepId = (String) processVariables.get("stepId");
		ArrayNode stepNodes = configNode.withArray("steps");
		for(JsonNode stepNd: stepNodes) 
		{
			boolean finishedTask = false;
			if(stepNd.get("id").asText().equals(stepId)) {
				final ArrayNode actionInfoArray = stepNd.withArray("actions");
				for(JsonNode actionnode: actionInfoArray) {
					if(actionnode.get("uiAction").asText().equals(action)) {
						final String nextStepId = actionnode.get("nextStepId").asText();
						final String nextStep = actionnode.get("nextStep").asText();
						// Map<String, Object> copyMap = new HashMap<>(processVariables);
						processVariables.put("stepId", nextStepId);
						processVariables.put("nextStep", nextStep);
						taskService.complete(taskId, processVariables);
						finishedTask = true;
						break;
					}
				}
			}
			if(finishedTask) {
				break;
			}
		}
	}
}
