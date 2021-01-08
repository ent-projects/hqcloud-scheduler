/**
 * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler.services;

import java.util.List;

import com.github.kubesys.httpfrk.core.HttpBodyHandler;
import com.github.kubesys.tools.annotations.ServiceDefinition;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * @author chace
 * @since 2019.10.29
 *
 */

@ServiceDefinition
public class TriggerService extends HttpBodyHandler {
	
	public void createTrigger(String name, String executor) {
		
	}
	
	public void removeTrigger(String name, String executor) {
		
	}
	
	public List<String> getTriggers() {
		return null;
	}
}
