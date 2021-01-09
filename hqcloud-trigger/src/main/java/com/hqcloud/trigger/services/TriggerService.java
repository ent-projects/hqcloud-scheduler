/**
 * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.trigger.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.httpfrk.core.HttpBodyHandler;
import com.github.kubesys.tools.annotations.ServiceDefinition;
import com.hqcloud.trigger.utils.ClientUtil;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * @author chace
 * @since 2019.10.29
 *
 */

@ServiceDefinition
public class TriggerService extends HttpBodyHandler {
	
	public final static String KIND = "Trigger";
	
	protected KubernetesClient client = ClientUtil.createDefaultKubernetesClient();
	
	public JsonNode createTrigger(JsonNode json) throws Exception {
		String type = json.has("type") ? json.get("type").asText() : "";
		if (!getTriggers().contains(type)) {
			throw new Exception("missing valid type, please see getTriggers.");
		}
		return client.createResource(json);
	}
	
	public JsonNode removeTrigger(String name) throws Exception {
		return client.deleteResource(KIND, "default", name);
	}
	
	public JsonNode newExecTrigger(String name) throws Exception {
		JsonNode json = client.getResource("Trigger", "default", name);
		JsonNode meta = json.get("metadata");
		ObjectNode labels = meta.has("labels") ? 
					(ObjectNode) meta.get("labels")
					: new ObjectMapper().createObjectNode();
		if (labels.has("execute")) {
			labels.remove("execute");
		}
		labels.put("execute", "true");
		((ObjectNode) meta).set("labels", labels);
		return client.updateResource(json);
	}
	
	public Set<String> getTriggers() throws Exception {
		Set<String> triggers = new HashSet<>();
		JsonNode json = client.getResource("ConfigMap", "default", "hqcloud-trigger");
		for (Iterator<String> iter = json.get("data").fieldNames();iter.hasNext();) {
			triggers.add(iter.next());
		}
		return triggers;
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
}
