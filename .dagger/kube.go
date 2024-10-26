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

	dag.Container().From("alpine/helm").
		WithMountedDirectory("/app", manifests).
		WithWorkdir("/app").
		WithExec([]string{"apk", "add", "kubectl"}).
		WithEnvVariable("KUBECONFIG", "/.kube/config").
		WithFile("/.kube/config", k.K3s.Config()).
		WithExec([]string{"helm", "install" /*"--wait",*/, "rabbitmq", "oci://registry-1.docker.io/bitnamicharts/rabbitmq"}).
		WithExec([]string{"helm", "install", "--wait", "dapr", "dapr", "--repo", "https://dapr.github.io/helm-charts/", "--version=1.14.1", "--namespace", "dapr", "--create-namespace"}).
		WithExec([]string{"kubectl", "apply", "-f", "."}).
		Sync(ctx)

	// TODO use proxy to export apiserver consumer and producer endpoints

	return kServer, nil

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
