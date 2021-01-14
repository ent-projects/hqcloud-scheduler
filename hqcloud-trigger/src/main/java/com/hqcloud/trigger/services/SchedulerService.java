/**
 * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.trigger.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.httpfrk.core.HttpBodyHandler;
import com.github.kubesys.tools.annotations.ServiceDefinition;
import com.hqcloud.trigger.utils.ClientUtil;
import com.hqcloud.trigger.utils.KubeUtil;

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
		ArrayNode nodes = new ObjectMapper().createArrayNode();
		for (String name : KubeUtil.getNodenames()) {
			nodes.add(name);
		}
		return nodes;
	}
	
	public JsonNode addTriggerType(String type, String desc) throws Exception {
		JsonNode json = client.getResource("ConfigMap", "default", "hqcloud-trigger");
		ObjectNode data = (ObjectNode) json.get("data");
		data.put(type, desc);
		return client.updateResource(json);
	}
	
	public JsonNode removeTriggerType(String type) throws Exception {
		JsonNode json = client.getResource("ConfigMap", "default", "hqcloud-trigger");
		ObjectNode data = (ObjectNode) json.get("data");
		if (data.has(type)) {
			data.remove(type);
		}
		return client.updateResource(json);
	}
	
	public JsonNode updateSchedulerPolicy(String policy, String position) throws Exception {
		JsonNode json = client.getResource("ConfigMap", "default", "hqcloud-scheduler");
		ObjectNode data = (ObjectNode) json.get("data");
		data.remove("policy");
		data.remove("position");
		data.put("policy", policy);
		data.put("position", position);
		return client.updateResource(json);
	}
}
