package main

import (
	"context"
	"dagger/kubecon-2024-na/internal/dagger"
)

type Producer struct {
	// +private
	AppName string
	// +private
	AppVersion string
	// +private
	Src *dagger.Directory
	// +private
	DaprComponents *dagger.Directory
}

func (m *Producer) Build(
	ctx context.Context,
) (*dagger.File, error) {

	return Build(ctx, m.AppName, m.AppVersion, m.Src)
}

func (m *Producer) Test(
	ctx context.Context,
) (*dagger.Service, error) {

	return Test(ctx, m.Src, m.DaprComponents)
}

func (m *Producer) Container(
	ctx context.Context,
) (*dagger.Container, error) {

	f, err := Build(ctx, m.AppName, m.AppVersion, m.Src)
	if err != nil {
		return nil, err
	}

	return base(ctx, f, m.AppName, m.DaprComponents)
}

func (m *Producer) Service(
	ctx context.Context,
) (*dagger.Service, error) {
	c, err := m.Container(ctx)
	if err != nil {
		return nil, err
	}

	return c.AsService().WithHostname("producer"), nil

}
