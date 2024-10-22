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
	"errors"
	"fmt"
	"strings"
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

func getAppNameAndVersion(ctx context.Context, src *dagger.File) (string, string, error) {
	out, err := dag.Container().From("mikefarah/yq").WithMountedFile("pom.xml", src).
		WithExec([]string{"yq", `"\(.project.artifactId):\(.project.version)"`, "pom.xml"}).Stdout(ctx)

	if err != nil {
		return "", "", err
	}

	nameVersion := strings.Split(out, ":")
	if len(nameVersion) != 2 {
		return "", "", errors.New("invalid name/version output")
	}

	return nameVersion[0], nameVersion[1], nil
}

func Build(
	ctx context.Context,
	appName, appVersion string,
	src *dagger.Directory,
) (*dagger.File, error) {
	name, version, err := getAppNameAndVersion(ctx, src.File("pom.xml"))
	if err != nil {
		return nil, err
	}
	return dag.Java().WithJdk("17").WithProject(src).
		Ctr().WithMountedCache("/root/.m2", dag.CacheVolume("kubecon-mvn-cache")).
		WithExec([]string{"./mvnw", "package", "-DskipTests"}).File(fmt.Sprintf("target/%s-%s.jar", name, version)), nil
}

func rabbitMQ() *dagger.Container {
	return dag.Container().From("rabbitmq:3.7.25-management-alpine").
		WithExposedPort(5672)
}
func dapr(ctx context.Context, app string, components *dagger.Directory) func(*dagger.Container) *dagger.Container {
	return func(c *dagger.Container) *dagger.Container {
		dapr := dag.Dapr(dagger.DaprOpts{Image: "daprio/daprd:1.14.1"}).
			Dapr(app, dagger.DaprDaprOpts{
				AppPort:           8080,
				AppChannelAddress: app,
				ComponentsPath:    components,
			}).WithServiceBinding("rabbitmq", rabbitMQ().AsService()).
			WithExposedPort(50001).AsService().WithHostname("dapr")
		dapr.Start(ctx)
		return c.WithEnvVariable("DAPR_GRPC_ENDPOINT", "http://dapr:50001")
	}
}

func Test(
	ctx context.Context,
	src *dagger.Directory,
	daprComponents *dagger.Directory,
) (*dagger.Service, error) {

	app, _, err := getAppNameAndVersion(ctx, src.File("pom.xml"))
	if err != nil {
		return nil, err
	}

	return dag.Java().WithJdk("17").
		WithProject(src).
		Ctr().
		WithMountedCache("/root/.m2", dag.CacheVolume("kubecon-mvn-cache")).
		With(dapr(ctx, app, daprComponents)).
		WithExec([]string{"./mvnw", "test"}).
		WithExposedPort(8080).
		AsService().WithHostname(app), nil
}

func base(
	ctx context.Context,
	src *dagger.File,
	appName string,
	daprComponents *dagger.Directory,
) (*dagger.Container, error) {
	return dag.Container().From("ubuntu/jre:17-22.04_edge").
		With(dapr(ctx, appName, daprComponents)).
		WithWorkdir("/usr/src/app").WithFile("app.jar", src).
		WithMountedTemp("/tmp").
		WithEntrypoint([]string{"java", "-Dserver.port=8080", "-jar", "app.jar"}).
		WithExposedPort(8080), nil
}
