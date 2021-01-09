/**
 * Copyrigt (2021, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler.utils;

import com.github.kubesys.KubernetesClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 *
 */
public class ClientUtil {

	protected static KubernetesClient kubeClient = new KubernetesClient(
					System.getenv("kubeUrl"), System.getenv("token"));

	private ClientUtil() {
		
	}
	
	public static KubernetesClient createDefaultKubernetesClient() {
		return kubeClient;
	}
}
