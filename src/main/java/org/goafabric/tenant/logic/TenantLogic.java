package org.goafabric.tenant.logic;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class TenantLogic {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Value("${multi-tenancy.tenants}")
    private String tenants;

    @Value("${application.images}")
    private String applicationImages;

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        Arrays.asList(tenants.split(",")).forEach(tenant -> log.info("#tenant: {}", tenant));
        Arrays.asList(applicationImages.split(",")).forEach(
                imageName -> execute("example", imageName, "42", false));
    }

    private void execute(String nameSpace, String imageName, String tenantId, boolean async) {
        log.info("executing ... {}", imageName);

        String podName = imageName.split(":")[0].split("/")[1];

        try (KubernetesClient client = new DefaultKubernetesClient()) {
            // Delete existing Pod if it exists
            client.pods().inNamespace(nameSpace).withName(podName).delete();

            // Build new Pod spec
            Pod pod = new PodBuilder()
                    .withNewMetadata()
                    .withName(podName)
                    .endMetadata()
                    .withNewSpec()
                    .withRestartPolicy("Never")
                    .addNewContainer()
                    .withName(podName + "-container")
                    .withImage(imageName)
                    .withEnv(
                            new EnvVar("database.provisioning.goals", "-migrate -terminate", null),
                            new EnvVar("multi-tenancy.tenants", tenantId, null)
                    )
                    .endContainer()
                    .endSpec()
                    .build();

            // Create the Pod
            client.pods().inNamespace(nameSpace).create(pod);

            if (!async) {
                waitToFinish(nameSpace, client, podName);
            }

        }
    }

    private void waitToFinish(String nameSpace, KubernetesClient client, String podName) {
        client.pods()
                .inNamespace(nameSpace)
                .withName(podName)
                .waitUntilCondition(
                        p -> {
                            if (p == null || p.getStatus() == null) return false;
                            String phase = p.getStatus().getPhase();
                            log.info("Pod phase: {}", phase);
                            return "Succeeded".equals(phase) || "Failed".equals(phase);
                        },
                        30, TimeUnit.SECONDS
                );
    }
}
