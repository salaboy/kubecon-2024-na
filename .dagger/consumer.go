package main

import (
	"context"
	"dagger/kubecon-2024-na/internal/dagger"
)

type Consumer struct {
	// +private
	AppName string
	// +private
	AppVersion string
	// +private
	Src *dagger.Directory
	// +private
	DaprComponents *dagger.Directory
}

func (m *Consumer) Build(
	ctx context.Context,
) (*dagger.File, error) {

	return Build(ctx, m.AppName, m.AppVersion, m.Src)
}

func (m *Consumer) Container(
	ctx context.Context,
) (*dagger.Container, error) {

	f, err := Build(ctx, m.AppName, m.AppVersion, m.Src)
	if err != nil {
		return nil, err
	}

	return base(ctx, f, m.AppName, m.DaprComponents)
}

func (m *Consumer) Test(
	ctx context.Context,
) (*dagger.Service, error) {

	return Test(ctx, m.Src, m.DaprComponents)
}
