/**
 * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesWatcher;
import com.hqcloud.scheduler.utils.ClientUtil;


/**
 * @author wuheng@otcaix.iscas.ac.cn
 * @author chace
 * @since 2019.10.29
 *
 */

public class Scheduler {
	
	protected final static KubernetesClient client = ClientUtil.createDefaultKubernetesClient();

	protected final static String DEFAULT_POLICY = "NodeWithMinimumMemoryFirst";
	
	protected final static String DEFAULT_POSITION = "status.allocatable.memory";
	
	protected static String policy = DEFAULT_POLICY;
	
	protected static String position = DEFAULT_POSITION;
	
	public Scheduler() throws Exception {
		client.watchResource("ConfigMap", "default", "hqcloud-scheduler", 
									new SchedulerConfigWatcher(client));
		
		client.watchResources("Trigger", "default", new TriggerToJob(client));
	}
	
	public static class SchedulerConfigWatcher extends KubernetesWatcher {

		public SchedulerConfigWatcher(KubernetesClient kubeClient) {
			super(kubeClient);
		}

		@Override
		public void doAdded(JsonNode node) {
			try {
				JsonNode data = node.get("data");
				policy = data.get("policy").asText();
				position = data.get("position").asText();
			} catch (Exception ex) {
				policy = DEFAULT_POLICY;
				position = DEFAULT_POSITION;
			}
		}

		@Override
		public void doModified(JsonNode node) {
			try {
				JsonNode data = node.get("data");
				policy = data.get("policy").asText();
				position = data.get("position").asText();
			} catch (Exception ex) {
				policy = DEFAULT_POLICY;
				position = DEFAULT_POSITION;
			}
		}

		@Override
		public void doDeleted(JsonNode node) {
			policy = DEFAULT_POLICY;
			position = DEFAULT_POSITION;
		}

		@Override
		public void doClose() {
			
		}
		
	}
	
	public static class TriggerToJob extends KubernetesWatcher {

		public TriggerToJob(KubernetesClient kubeClient) {
			super(kubeClient);
		}

		@Override
		public void doAdded(JsonNode trigger) {
			JsonNode[] nodes = getAllNodes();
			Arrays.sort(nodes, new Comparator<JsonNode>() {

				@Override
				public int compare(JsonNode o1, JsonNode o2) {
					long lo1 = stringToLong(getValue(o1));
					long lo2 = stringToLong(getValue(o2));
					return (lo2 - lo1 < 0) ? -1 : 1;
				}
				
			});
			JsonNode job = toJob(trigger, nodes[0].get("metadata").get("name").asText());
			try {
				JsonNode createResource = client.createResource(job);
				if (createResource.has("status") && createResource.get("status")
						.asText().equals("Failure")) {
					throw new Exception(createResource.toPrettyString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void doModified(JsonNode node) {
			
		}

		@Override
		public void doDeleted(JsonNode node) {
			// ignore here
		}

		@Override
		public void doClose() {
			// ignore here
		}
		
	}

	public String getPolicy() {
		return policy;
	}
	

	protected static JsonNode[] getAllNodes() {
		try {
			JsonNode items = client.listResources("Node").get("items");
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
	
	protected static long stringToLong(String value) {
		long weight = 1;
		if (value.endsWith("Ki")) {
			value = value.substring(0, value.length() - 2);
			weight = 1;
		} else if (value.endsWith("Mi")) {
			value = value.substring(0, value.length() - 2);
			weight = 1024;
		} else if (value.endsWith("Gi")) {
			value = value.substring(0, value.length() - 2);
			weight = 1024*1024;
		} else if (value.endsWith("Ti")) {
			value = value.substring(0, value.length() - 2);
			weight = 1024*1024*1024;
		} 
		
		return Long.parseLong(value) * weight;
	}
	
	protected static String getValue(JsonNode node) {
		JsonNode value = node;
		for (String key : position.split(".")) {
			value = value.get(key);
		}
		return value.asText();
	}
	

	protected static JsonNode toJob(JsonNode trigger, String nodeName) {
		
		String name = trigger.get("metadata").get("name").asText()
				      + "-" + getRandomString();
		
		ObjectNode job = new ObjectMapper().createObjectNode();
		job.put("apiVersion", "batch/v1");
		job.put("kind", "Job");
		
		{
			ObjectNode meta = new ObjectMapper().createObjectNode();
			meta.put("name", name);
			{
				ObjectNode labels = new ObjectMapper().createObjectNode();
				labels.put("app", name);
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
						labels.put("app", name);
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
							container.put("image", getImage(trigger.get("sink").asText()));
							container.put("name", name);
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
		}
		return job;
	}
	
	 protected static String getRandomString(){
	     String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	     Random random=new Random();
	     StringBuffer sb=new StringBuffer();
	     for(int i=0;i<6;i++){
	       int number=random.nextInt(62);
	       sb.append(str.charAt(number));
	     }
	     return sb.toString().toLowerCase();
	 }
	 
	 protected static String getImage(String key) {
		 try {
			JsonNode json = client.getResource("ConfigMap", "default", "hqcloud-algorithm");
			return json.get("data").get(key).asText();
		} catch (Exception e) {
			return null;
		}
	 }

	 public static void main(String[] args) throws Exception {
		 new Scheduler();
	 }
}
