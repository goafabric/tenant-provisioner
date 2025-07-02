package org.goafabric.tenant.logic;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

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
                imageName -> execute(imageName, "42", true));
    }

    private void execute(String imageName, String tenantId, boolean async) {
        log.info("executing ... {}", imageName);
        try {
            String podName =  imageName.split(":")[0].split("/")[1];
            Runtime.getRuntime().exec("kubectl delete pods " + podName).waitFor();

            var pb = new ProcessBuilder(
                    "kubectl", "run", podName,
                    "--image=" + imageName,
                    "--restart=Never",
                    "--env", "database.provisioning.goals=-migrate -terminate",
                    "--env", "multi-tenancy.tenants=" + tenantId
                    , async ? "" : "-it"
            );

            pb.redirectErrorStream(true); // merge stdout + stderr

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("kubectl exited with code: " + exitCode);
            if (exitCode != 0) {
                throw new IllegalStateException("error on execution");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
