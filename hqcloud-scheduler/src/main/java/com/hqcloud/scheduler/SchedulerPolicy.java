/**
/ * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesWatcher;
import com.hqcloud.scheduler.utils.ClientUtil;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * @since 2021.1.9
 *
 */

public class SchedulerPolicy {

	protected final static String DEFAULT_POLICY = "NodeWithMinimumMemoryFirst";

	protected final static String DEFAULT_POSITION = "status.allocatable.memory";

	public String policy = DEFAULT_POLICY;

	public String position = DEFAULT_POSITION;

	public SchedulerPolicy() throws Exception {

		KubernetesClient client = ClientUtil.createDefaultKubernetesClient();
		client.watchResource("ConfigMap", "default", "hqcloud-scheduler", new KubernetesWatcher(client) {

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
		});
		

	}

	public String getPolicy() {
		return policy;
	}

	public String getPosition() {
		return position;
	}

}
