package com.api.activiti.activiti.poc.rest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.activiti.activiti.poc.pojo.TaskInfoVo;

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

	@GetMapping("/create")
	public String getAllProcecess() {
		ProcessInstance startProcessInstanceByKey = runtimeService.startProcessInstanceByKey("hhlprocess");
		log.info("Created process instance {}", startProcessInstanceByKey);
		return startProcessInstanceByKey.getId();
	}

	@GetMapping("/task/{processInstanceId}")
	public List<TaskInfoVo> getTaskIdForInstanceId(@PathVariable String processInstanceId) {
		List<Execution> list = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list();
		return list.stream().filter(item -> Objects.nonNull(item.getParentId())).map(TaskInfoVo::mapToTaskInfo)
				.collect(Collectors.toList());
	}

	@GetMapping("/all-process/{definitionId}")
	public List<String> getActiveProcessInstanceByDefId(@PathVariable String definitionId) {
		List<Task> taskList = taskService.createTaskQuery().processDefinitionKey(definitionId).list();
		log.info("Task {}", taskList);
		List<Execution> list = runtimeService.createExecutionQuery().processDefinitionKey(definitionId).list();
		return list.stream().filter(item -> Objects.isNull(item.getParentId())).map(Execution::getId)
				.collect(Collectors.toList());
	}
}
