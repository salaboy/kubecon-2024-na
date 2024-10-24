// A generated module for Kubecon2024Na functions
//
// This module has been generated via dagger init and serves as a reference to
// basic module structure as you get started with Dagger.
//
// Two functions have been pre-created. You can modify, delete, or add to them,
// as needed. They demonstrate usage of arguments and return types using simple
// echo and grep commands. The functions can be called from the dagger CLI or
// from one of the SDKs.
//
// The first line in this comment block is a short description line and the
// rest is a long description with more detail on the module's purpose or usage,
// if appropriate. All modules should have a short description.

package main

import (
	"context"
	"dagger/kubecon-2024-na/internal/dagger"
)

type Kubecon2024Na struct{}

// producer app operations
func (m *Kubecon2024Na) Producer(
	ctx context.Context,
	// +defaultPath="/producer-app"
	// +ignore=["target"]
	src *dagger.Directory,
	// +defaultPath="/components"
	daprComponents *dagger.Directory,
) (*Producer, error) {

	app, version, err := getAppNameAndVersion(ctx, src.File("pom.xml"))

	if err != nil {
		return nil, err
	}
	return &Producer{
		AppName:        app,
		AppVersion:     version,
		Src:            src,
		DaprComponents: daprComponents,
	}, nil
}

// consumer app operations
func (m *Kubecon2024Na) Consumer(
	ctx context.Context,
	// +defaultPath="/consumer-app"
	// +ignore=["target"]
	src *dagger.Directory,
	// +defaultPath="/components"
	daprComponents *dagger.Directory,
) (*Consumer, error) {
	app, version, err := getAppNameAndVersion(ctx, src.File("pom.xml"))

	if err != nil {
		return nil, err
	}
	return &Consumer{
		AppName:        app,
		AppVersion:     version,
		Src:            src,
		DaprComponents: daprComponents,
	}, nil
}

func (m *Kubecon2024Na) Kube() *Kube {
	return &Kube{
		K3s: dag.K3S("kubecon2024"),
	}
}
