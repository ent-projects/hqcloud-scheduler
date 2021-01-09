/**
 * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.httpfrk.core.HttpBodyHandler;
import com.github.kubesys.tools.annotations.ServiceDefinition;
import com.github.utils.ClientUtil;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * @author chace
 * @since 2019.10.29
 *
 */

@ServiceDefinition
public class SchedulerService extends HttpBodyHandler {
	
	protected KubernetesClient client = ClientUtil.createDefaultKubernetesClient();
	
	public JsonNode getNodes() throws Exception {
		return client.listResources("Node");
	}
}
