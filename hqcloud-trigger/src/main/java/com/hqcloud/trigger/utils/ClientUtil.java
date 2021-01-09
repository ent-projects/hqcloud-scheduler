/**
 * Copyrigt (2021, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.trigger.utils;

import com.github.kubesys.KubernetesClient;
import com.github.kubesys.watchers.AutoDiscoverCustomizedResourcesWacther;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 *
 */
public class ClientUtil {

	protected static KubernetesClient kubeClient = new KubernetesClient(
					System.getenv("kubeUrl"), System.getenv("token"));

	static {
		try {
			kubeClient.watchResources("CustomResourceDefinition", 
					new AutoDiscoverCustomizedResourcesWacther(kubeClient));
		} catch (Exception e) {
		}
	}
	
	private ClientUtil() {
		
	}
	
	public static KubernetesClient createDefaultKubernetesClient() {
		return kubeClient;
	}
}
