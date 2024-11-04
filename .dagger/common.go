package main

import (
	"context"
	"dagger/kubecon-2024-na/internal/dagger"
	"errors"
	"fmt"
	"strings"
)

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
	return dag.Java().
		WithJdk("17").
		WithProject(src).
		Ctr().
		WithMountedCache("/root/.m2", dag.CacheVolume("kubecon-mvn-cache")).
		WithExec([]string{"./mvnw", "package", "-DskipTests"}).File(fmt.Sprintf("target/%s-%s.jar", name, version)), nil
}

func rabbitMQ() *dagger.Container {
	return dag.Container().From("rabbitmq:3.7.25-management-alpine").
		WithExposedPort(5672)
}
func dapr(ctx context.Context, app string, components *dagger.Directory, pRabbit *dagger.Service) func(*dagger.Container) *dagger.Container {
	return func(c *dagger.Container) *dagger.Container {
		dapr := dag.Dapr(dagger.DaprOpts{Image: "daprio/daprd:1.14.1"}).
			Dapr(app, dagger.DaprDaprOpts{
				AppPort:           8080,
				AppChannelAddress: app,
				ComponentsPath:    components,
			}).
			With(func(c *dagger.Container) *dagger.Container {
				rabbitSvc := rabbitMQ().AsService()
				if pRabbit != nil {
					rabbitSvc = pRabbit
				}

				return c.WithServiceBinding("rabbitmq", rabbitSvc)
			}).
			WithExposedPort(50001).WithExposedPort(3500).AsService().WithHostname(fmt.Sprintf("%s-dapr", app))
		dapr.Start(ctx)
		return c.WithEnvVariable("DAPR_GRPC_ENDPOINT", fmt.Sprintf("http://%s-dapr:50001", app)).WithEnvVariable("DAPR_HTTP_ENDPOINT", fmt.Sprintf("http://%s-dapr:3500", app))
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
		With(dapr(ctx, app, daprComponents, nil)).
		WithExec([]string{"./mvnw", "test"}).
		WithExposedPort(8080).
		AsService().WithHostname(app), nil
}

func base(
	ctx context.Context,
	src *dagger.File,
	appName string,
) (*dagger.Container, error) {
	return dag.Container().From("ubuntu/jre:17-22.04_edge").
		WithWorkdir("/usr/src/app").WithFile("app.jar", src).
		WithMountedTemp("/tmp").
		WithEntrypoint([]string{"java", "-Dserver.port=8080", "-jar", "app.jar"}).
		WithExposedPort(8080), nil
}
