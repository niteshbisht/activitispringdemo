package com.api.activiti.activiti.poc.pojo;

import java.util.Date;

import org.activiti.engine.runtime.Execution;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TaskInfoVo {
	private String taskId;
	private String taskName;
	private Date createdDate;
	
	public static TaskInfoVo mapToTaskInfo(Execution exe) {
		return TaskInfoVo.builder().taskId(exe.getId()).taskName(exe.getName()).build();
	}
}
