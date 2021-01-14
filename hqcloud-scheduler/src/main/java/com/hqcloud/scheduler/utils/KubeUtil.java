/**
 * Copyrigt (2021, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler.utils;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.KubernetesClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 *
 */
public class KubeUtil {

	@SuppressWarnings("deprecation")
	public static void updateTriggerStatus(JsonNode trigger, String status, String reason, JsonNode data) {
		try {
			ArrayNode results = trigger.has("results") ? (ArrayNode) trigger.get("results")
					: new ObjectMapper().createArrayNode();

			ObjectNode result = new ObjectMapper().createObjectNode();
			result.put("status", status);
			result.put("time", new Date().toLocaleString());
			result.put("reason", reason);
			result.set("result", data);
			results.add(result);
			((ObjectNode) trigger).set("results", results);
			ClientUtil.createDefaultKubernetesClient().updateResource(trigger);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateTrggerLabel(JsonNode trigger) {
		JsonNode meta = trigger.get("metadata");
		ObjectNode labels = meta.has("labels") ? 
					(ObjectNode) meta.get("labels")
					: new ObjectMapper().createObjectNode();
		if (labels.has("execute")) {
			labels.remove("execute");
		}
		labels.put("execute", "fasle");
		((ObjectNode) meta).set("labels", labels);
		try {
			KubernetesClient client = ClientUtil.createDefaultKubernetesClient();
			client.updateResource(trigger);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static JsonNode triggerToJob(JsonNode trigger, String nodeName) {

		String jobName = trigger.get("metadata").get("name").asText() 
							+ "-" + StringUtil.getRandomString();

		ObjectNode job = new ObjectMapper().createObjectNode();
		job.put("apiVersion", "batch/v1");
		job.put("kind", "Job");

		{
			ObjectNode meta = new ObjectMapper().createObjectNode();
			meta.put("name", jobName);
			{
				ObjectNode labels = new ObjectMapper().createObjectNode();
				labels.put("execute", "false");
				labels.put("app", jobName);
				labels.put("trigger", trigger.get("metadata").get("name").asText());
				meta.set("labels", labels);
			}
			job.set("metadata", meta);
		}

		{
			ObjectNode spec = new ObjectMapper().createObjectNode();
			{
				ObjectNode temp = new ObjectMapper().createObjectNode();

				{
					ObjectNode meta = new ObjectMapper().createObjectNode();
					{
						ObjectNode labels = new ObjectMapper().createObjectNode();
						labels.put("app", jobName);
						meta.set("labels", labels);
					}
					temp.set("metadata", meta);
				}

				{
					ObjectNode spec2 = new ObjectMapper().createObjectNode();

					{
						ArrayNode containers = new ObjectMapper().createArrayNode();

						{
							ObjectNode container = new ObjectMapper().createObjectNode();
							container.put("image", getJobImage(trigger.get("sink").asText()));
							container.put("name", jobName);
							container.put("imagePullPolicy", "IfNotPresent");

							if (trigger.get("info").has("env")) {
								container.set("env", trigger.get("info").get("env"));
							}

							if (trigger.get("info").has("command")) {
								container.set("command", trigger.get("info").get("command"));
							}

							containers.add(container);
						}
						spec2.set("containers", containers);
						spec2.put("restartPolicy", "Never");
					}

					temp.set("spec", spec2);
				}
				spec.set("template", temp);
			}

			job.set("spec", spec);
			job.put("nodeName", nodeName);
		}
		return job;
	}
	
	public static JsonNode triggerToPod(JsonNode trigger, String nodeName) {

		String podName = trigger.get("metadata").get("name").asText() 
							+ "-" + StringUtil.getRandomString();

		ObjectNode pod = new ObjectMapper().createObjectNode();
		pod.put("apiVersion", "v1");
		pod.put("kind", "Pod");

		{
			ObjectNode meta = new ObjectMapper().createObjectNode();
			meta.put("name", podName);
			{
				ObjectNode labels = new ObjectMapper().createObjectNode();
				labels.put("execute", "false");
				labels.put("app", podName);
				labels.put("trigger", trigger.get("metadata").get("name").asText());
				meta.set("labels", labels);
			}
			pod.set("metadata", meta);
		}

		{
			ObjectNode spec = new ObjectMapper().createObjectNode();
			{
				ArrayNode containers = new ObjectMapper().createArrayNode();

				{
					ObjectNode container = new ObjectMapper().createObjectNode();
					container.put("image", getJobImage(trigger.get("sink").asText()));
					container.put("name", podName);
					container.put("imagePullPolicy", "IfNotPresent");

					if (trigger.get("info").has("env")) {
						container.set("env", trigger.get("info").get("env"));
					}

					if (trigger.get("info").has("command")) {
						container.set("command", trigger.get("info").get("command"));
					}

					containers.add(container);
				}
				spec.set("containers", containers);
				spec.put("restartPolicy", "Never");
				spec.put("nodeName", nodeName);
			}

			pod.set("spec", spec);
			
		}
		return pod;
	}
	
	public static String getJobImage(String key) {
		try {
			JsonNode json = ClientUtil.createDefaultKubernetesClient()
					.getResource("ConfigMap", "default", "hqcloud-algorithm");
			return json.get("data").get(key).asText();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static JsonNode[] getAllNodes(String label) {
		try {
			JsonNode items = ClientUtil.createDefaultKubernetesClient()
					.listResources("Node", "default", null, label + "=" + label).get("items");
			JsonNode[] nodes = new JsonNode[items.size()];
			int i = 0;
			for (JsonNode node : items) {
				nodes[i++] = node;
			}
			return nodes;
		} catch (Exception ex) {
			return null;
		}
	}
}
