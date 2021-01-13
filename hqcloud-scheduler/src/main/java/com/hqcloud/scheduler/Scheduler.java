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

	protected final KubernetesClient client = ClientUtil.createDefaultKubernetesClient();
	
	protected final SchedulerPolicy policy;

	public Scheduler(SchedulerPolicy policy) throws Exception {
		this.policy = policy;
	}
	
	public void run() throws Exception {
		// receive trigger events
		client.watchResources("Trigger", "", new TriggerToJob(client, policy));
	}

	public static class TriggerToJob extends KubernetesWatcher {

		protected final SchedulerPolicy policy;
		
		public TriggerToJob(KubernetesClient kubeClient, SchedulerPolicy policy) {
			super(kubeClient);
			this.policy = policy;
		}

		@Override
		public void doAdded(JsonNode trigger) {
			KubeUtil.updateTrggerLabel(trigger);
			doScheduling(trigger);
		}


		@Override
		public void doModified(JsonNode trigger) {
			if (trigger.get("metadata").has("lables") && 
					trigger.get("metadata").get("lables").has("execute") &&
					trigger.get("metadata").get("lables").get("execute").asText().equals("true")) {
				KubeUtil.updateTrggerLabel(trigger);
				doScheduling(trigger);
			}
		}

		@Override
		public void doDeleted(JsonNode node) {
			// ignore here
		}
		
		void doScheduling(JsonNode trigger) {
			
			// Get algorithm using trigger.get("sink"),
			String algorithm = trigger.get("sink").asText();
			
			// Get the nodes can running this algorithm
			JsonNode[] nodes = KubeUtil.getAllNodes(algorithm);

			if (nodes == null) {
				KubeUtil.updateTriggerStatus(trigger, "error", "cannot find any nodes "
						+ "supporting algorithm  " + algorithm, null);
				return;
			}

			// Sorting nodes according a specified scheduling policy
			Arrays.sort(nodes, new Comparator<JsonNode>() {

				@Override
				public int compare(JsonNode o1, JsonNode o2) {
					long lo1 = StringUtil.stringToLong(getValue(o1));
					long lo2 = StringUtil.stringToLong(getValue(o2));
					return (lo2 - lo1 < 0) ? -1 : 1;
				}

			});
			
			// Generating scheduling results
			JsonNode job = KubeUtil.triggerToJob(trigger, nodes[0].get("metadata").get("name").asText());
			try {
				JsonNode createResource = ClientUtil.createDefaultKubernetesClient().createResource(job);
				if (createResource.has("status") && createResource.get("status").asText().equals("Failure")) {
					KubeUtil.updateTriggerStatus(trigger, "error", createResource.toPrettyString(), null);
					throw new Exception(createResource.toPrettyString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		protected String getValue(JsonNode node) {
			JsonNode value = node;
			for (String key : policy.getPosition().split(".")) {
				value = value.get(key);
			}
			return value.asText();
		}

		@Override
		public void doClose() {
		}
		
	}

	public static void main(String[] args) throws Exception {
		Scheduler schd = new Scheduler(new SchedulerPolicy());
		schd.run();
		
	}
	
}
