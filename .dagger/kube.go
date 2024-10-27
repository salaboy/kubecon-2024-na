package main

import (
	"context"
	"dagger/kubecon-2024-na/internal/dagger"
)

type Kube struct {
	// +private
	K3s *dagger.K3S
}

// a kubernetes service with all the applications deployed
func (k *Kube) Service(
	ctx context.Context,
	// +defaultPath="k8s"
	manifests *dagger.Directory,
) (*dagger.Service, error) {
	kServer := k.K3s.Server()

	kServer, err := kServer.Start(ctx)
	if err != nil {
		return nil, err
	}

	_, err = kServer.Endpoint(ctx, dagger.ServiceEndpointOpts{Port: 80, Scheme: "http"})
	if err != nil {
		return nil, err
	}

	err = k.Deploy(ctx, k.K3s.Config(), "", manifests, nil)
	if err != nil {
		return nil, err
	}

	return dag.Proxy().
		WithService(kServer, "kube", 6443, 6443).
		WithService(kServer, "producer", 8080, 31000).
		WithService(kServer, "consumer", 8081, 31001).
		Service(), nil

}

// we have to do this since gke-gcloud-auth-plugin binary is not possible
// to be downloaded from anywhere.
func gCloud(cfg *dagger.Directory) func(*dagger.Container) *dagger.Container {
	return func(c *dagger.Container) *dagger.Container {
		return c.WithExec([]string{"apk", "add", "curl", "python3"}).
			WithEnvVariable("PATH", "/google-cloud-sdk/bin:${PATH}", dagger.ContainerWithEnvVariableOpts{Expand: true}).
			WithExec([]string{"sh", "-c", `curl -fsSLO https://dl.google.com/dl/cloudsdk/channels/rapid/google-cloud-sdk.tar.gz
tar xzf google-cloud-sdk.tar.gz -C /
rm google-cloud-sdk.tar.gz
gcloud config set core/disable_usage_reporting true
gcloud config set component_manager/disable_update_check true
gcloud config set metrics/environment github_docker_image
gcloud components install alpha beta gke-gcloud-auth-plugin`}).
			With(func(c *dagger.Container) *dagger.Container {
				if cfg != nil {
					c = c.WithMountedDirectory("/root/.config/gcloud", cfg)
				}
				return c
			})
	}

}

// deploys the apps to the target k8s cluster
func (k *Kube) Deploy(
	ctx context.Context,
	kubeCfg *dagger.File,
	// +optional
	contextName string,
	manifests *dagger.Directory,
	// +optional
	gcloudConfig *dagger.Directory,
) error {
	_, err := dag.Container().From("alpine/helm").
		With(gCloud(gcloudConfig)).
		WithMountedDirectory("/app", manifests).
		WithWorkdir("/app").
		WithExec([]string{"apk", "add", "kubectl"}).
		WithEnvVariable("KUBECONFIG", "/.kube/config").
		WithFile("/.kube/config", kubeCfg).
		With(func(c *dagger.Container) *dagger.Container {
			if len(contextName) > 0 {
				c = c.WithExec([]string{"kubectl", "config", "set-context", contextName})
			}
			return c
		}).
		// we don't wait for rabitmq since dapr will take some time to become ready which will give
		// rabbitmq enough time to finish its setup
		WithExec([]string{"helm", "install", "rabbitmq", "oci://registry-1.docker.io/bitnamicharts/rabbitmq"}).
		WithExec([]string{"helm", "install", "--wait", "dapr", "dapr", "--repo", "https://dapr.github.io/helm-charts/", "--version=1.14.1", "--namespace", "dapr", "--create-namespace"}).
		WithExec([]string{"kubectl", "apply", "-f", "."}).
		Sync(ctx)
	return err
}

func (k *Kube) Config(
	ctx context.Context,
	// +default="false"
	local bool,
) *dagger.File {
	return k.K3s.Config(dagger.K3SConfigOpts{Local: local})
}

func (k *Kube) Kubectl(
	ctx context.Context,
	// +optional
	args string,
) *dagger.Container {
	return k.K3s.Kubectl(args)
}

func (k *Kube) Kns(
	ctx context.Context,
) *dagger.Container {
	return k.K3s.Kns().Terminal()
}
