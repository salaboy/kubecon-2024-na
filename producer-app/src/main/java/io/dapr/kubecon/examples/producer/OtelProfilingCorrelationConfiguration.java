package io.dapr.kubecon.examples.producer;

import co.elastic.otel.UniversalProfilingProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.tracing.SdkTracerProviderBuilderCustomizer;
import org.springframework.boot.actuate.autoconfigure.tracing.SpanProcessors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
public class OtelProfilingCorrelationConfiguration {

    // This overrides the tracer configuration from OpenTelemetryAutoConfiguration.otelSdkTracerProvider()
    @Bean
    public SdkTracerProvider tracerProviderWithProfilingCorrelation(Resource resource, SpanProcessors spanProcessors, Sampler sampler, ObjectProvider<SdkTracerProviderBuilderCustomizer> customizers) {
        SdkTracerProviderBuilder builder = SdkTracerProvider.builder().setSampler(sampler).setResource(resource);
        Objects.requireNonNull(builder);

        List<SpanProcessor> exportingProcessors = new ArrayList<>();

        for (SpanProcessor proc : spanProcessors) {
            if (proc instanceof BatchSpanProcessor || proc instanceof SimpleSpanProcessor) {
                exportingProcessors.add(proc);
            } else {
                builder.addSpanProcessor(proc);
            }
        }

        SpanProcessor exportSpanProcessor = SpanProcessor.composite(exportingProcessors);
        UniversalProfilingProcessor profilingCorrelation = UniversalProfilingProcessor
                .builder(exportSpanProcessor, resource)
                .delayActivationAfterProfilerRegistration(false)
                .build();
        builder.addSpanProcessor(profilingCorrelation);

        customizers.orderedStream().forEach((customizer) -> {
            customizer.customize(builder);
        });
        return builder.build();
    }
}
