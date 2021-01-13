/**
/¡° * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesWatcher;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * @since  2021.1.9
 *
 */

public class Test {

	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient("https://182.92.208.39:6443", "eyJhbGciOiJSUzI1NiIsImtpZCI6InpKdHdTbXFHWlZTLThlRUMtMW5yY0NQNGFuc3YtaThmR1RoZi1ZZU9FZFUifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi1xZjU4ayIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6Ijc3MGM5MTI4LWU2MTMtNDNiNS05Y2NlLTU4NzI1NWFjOWFmMSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.gtI_ht9M6rHdIzmDHV5vU9vQ3clpQCMKz7Qg4UlSHulVUj0XZAnQIslf0QOu65y64iC55lHgzNgJz0drYBJoIhqUi64Ceso6Ry374m2aZOajZ3VUxh6nU2l9rU1bfXp_gHt6BonLb8ZkyhiwnuW1S1bacDRzrolPC6bChqUU9pT3KXpDdRKF9-4mh-sOfjaVGqkZPic5i7-QolWicGDSDSBUfFUy1BbNQPSSggKQd1xYGGJcsRUgYUlcJqdMEGm5RR8vqbwcAbfnyPU21zuqloMtb1BTtZxAk9j35o7fY6XRKWn3QfDKftd86YdevbaxeEvzQBIKiqprSPpx1iGhkw");
		client.watchResources("Trigger", new KubernetesWatcher(client) {
			
			@Override
			public void doModified(JsonNode node) {
				System.out.println(node.toPrettyString());
			}
			
			@Override
			public void doDeleted(JsonNode node) {
				System.out.println(node.toPrettyString());
			}
			
			@Override
			public void doClose() {
				
			}
			
			@Override
			public void doAdded(JsonNode node) {
				System.out.println(node.toPrettyString());
			}
		});
	}
	
}
