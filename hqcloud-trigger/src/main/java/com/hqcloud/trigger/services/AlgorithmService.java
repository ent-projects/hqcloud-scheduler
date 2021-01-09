/**
 * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.trigger.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
public class AlgorithmService extends HttpBodyHandler {
	
	protected KubernetesClient client = ClientUtil.createDefaultKubernetesClient();
	
	public JsonNode createAlgorithm(JsonNode json) throws Exception {
		return client.createResource(json);
	}
	
	public JsonNode removeAlgorithm(String name) throws Exception {
		return client.deleteResource("Algorithm", "default", name);
	}
	
	public JsonNode queryAlgorithmOn(List<String> algs) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (String alg : algs) {
			sb.append(alg).append("=").append(alg).append(",");
		}
		String labels = sb.substring(0, sb.length() - 1);
		return client.listResources("Node", "default", null, labels);
	}
	
	public JsonNode bindAlgorithm(String alg, String node) throws Exception {
		if (!getAlgorithms().contains(alg)) {
			throw new Exception("unsupport  algorithm" + alg + ", please see createAlgorithms");
		}
		
		if (!getNodenames().contains(node)) {
			throw new Exception("node " + node + " not exit, please see getNodenames");
		}
		JsonNode json = client.getResource("Node", node);
		ObjectNode meta = (ObjectNode) json.get("metadata");
		ObjectNode labels = meta.has("labels") ? (ObjectNode) meta.get("labels") 
						: new ObjectMapper().createObjectNode();
		labels.put(alg, alg);
		meta.set("labels", labels);
		return client.updateResource(json);
	}
	
	public JsonNode unbindAlgorithm(String alg, String node) throws Exception {
		if (!getAlgorithms().contains(alg)) {
			throw new Exception("unsupport  algorithm" + alg + ", please see createAlgorithms");
		}
		
		if (!getNodenames().contains(node)) {
			throw new Exception("node " + node + " not exit, please see getNodenames");
		}
		JsonNode json = client.getResource("Node", node);
		ObjectNode meta = (ObjectNode) json.get("metadata");
		if (meta.has("labels")) {
			((ObjectNode) meta.get("labels")).remove(alg);
		}
		return client.updateResource(json);
	}
	
	public Set<String> getNodenames() throws Exception {
		Set<String> nodes = new HashSet<>();
		JsonNode json = client.listResources("Node");
		for (Iterator<JsonNode> iter = json.get("items").iterator();iter.hasNext();) {
			JsonNode item = iter.next();
			nodes.add(item.get("metadata").get("name").asText());
		}
		return nodes;
	}
	
	public Set<String> getAlgorithms() throws Exception {
		Set<String> algs = new HashSet<>();
		JsonNode json = client.getResource("ConfigMap", "default", "hqcloud-algorithm");
		for (Iterator<String> iter = json.get("data").fieldNames();iter.hasNext();) {
			algs.add(iter.next());
		}
		return algs;
	}
}
