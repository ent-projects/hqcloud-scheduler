/**
/¡° * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler;

import java.util.Arrays;
import java.util.Comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesWatcher;
import com.hqcloud.scheduler.utils.ClientUtil;
import com.hqcloud.scheduler.utils.KubeUtil;
import com.hqcloud.scheduler.utils.StringUtil;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * @since  2021.1.9
 *
 */

public class Scheduler {

	
	public void start() throws Exception {
		KubernetesClient client = ClientUtil.createDefaultKubernetesClient();
		
		// receive trigger events
		client.watchResources("Trigger", new KubernetesWatcher(client) {
			
			@Override
			public void doAdded(JsonNode trigger) {
				KubeUtil.updateTrggerLabel(trigger);
				doScheduling(trigger);
			}


			@Override
			public void doModified(JsonNode trigger) {
				JsonNode meta = trigger.get("metadata");
				JsonNode labels = meta.get("labels");
				String exec = labels.get("execute").asText();
				if (exec.equals("true")) {
					KubeUtil.updateTrggerLabel(trigger);
					doScheduling(trigger);
				}
			}

			@Override
			public void doDeleted(JsonNode trigger) {
				// ignore here
				System.out.println(trigger.toPrettyString());
			}
			
			void doScheduling(JsonNode trigger) {
				
				// Get algorithm using trigger.get("sink"),
				String algorithm = trigger.get("sink").asText();
				
				// Get the nodes can running this algorithm
				JsonNode[] nodes = KubeUtil.getAllNodes(algorithm);

				if (nodes == null || nodes.length == 0) {
					KubeUtil.updateTriggerStatus(trigger, "error", "cannot find any nodes "
							+ "supporting algorithm " + algorithm, null);
					return;
				}

				// Get policy
				String policy = getPolicy();
				
				// Sorting nodes according a specified scheduling policy
				Arrays.sort(nodes, new Comparator<JsonNode>() {

					@Override
					public int compare(JsonNode o1, JsonNode o2) {
						long lo1 = StringUtil.stringToLong(getValue(o1, policy));
						long lo2 = StringUtil.stringToLong(getValue(o2, policy));
						return (lo2 - lo1 < 0) ? -1 : 1;
					}

				});
				
				// Generating scheduling results
				JsonNode job = KubeUtil.triggerToPod(trigger, nodes[0].get("metadata").get("name").asText());
				try {
					JsonNode createResource = ClientUtil.createDefaultKubernetesClient().createResource(job);
					if (createResource.has("status") && createResource.get("status").asText().equals("Failure")) {
						KubeUtil.updateTriggerStatus(trigger, "error", createResource.toPrettyString(), null);
						throw new Exception(createResource.toPrettyString());
					} 
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				KubeUtil.updateTriggerStatus(trigger, "success", "executing using Pod " 
						+ job.get("metadata").get("name").asText(), null);
			}
			
			protected String getPolicy() {
				try {
					JsonNode policy = client.getResource(
							"ConfigMap", "default", "hqcloud-scheduler");
					return policy.get("data").get("position").asText();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			protected String getValue(JsonNode node, String policy) {
				JsonNode value = node;
				for (String key : policy.split(".")) {
					value = value.get(key);
				}
				return value.asText();
			}

			@Override
			public void doClose() {
			}
		});
	}


	public static void main(String[] args) throws Exception {
		Scheduler schd = new Scheduler();
		schd.start();
		
	}
	
}
