/**
 * Copyrigt (2021, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.trigger.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 *
 */
public class KubeUtil {

	public static Set<String> getNodenames() throws Exception {
		Set<String> nodes = new HashSet<>();
		JsonNode json = ClientUtil.createDefaultKubernetesClient().listResources("Node");
		for (Iterator<JsonNode> iter = json.get("items").iterator();iter.hasNext();) {
			JsonNode item = iter.next();
			nodes.add(item.get("metadata").get("name").asText());
		}
		return nodes;
	}
}
