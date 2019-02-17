package com.github.edgar615.opentracing.lesson02;

import com.google.common.collect.ImmutableMap;
import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.UUID;

public class Hello {

    private final Tracer tracer;

    private Hello(Tracer tracer) {
        this.tracer = tracer;
    }

    private void sayHello(String helloTo) {
        Span span = tracer.buildSpan("say-hello").start();
        span.setTag("hello-to", helloTo);
        System.out.println(formatString(span, helloTo));
        printHello(span, helloTo);
        span.finish();
    }

    private  String formatString(Span rootSpan, String helloTo) {
        Span span = tracer.buildSpan("formatString").asChildOf(rootSpan).start();
        try {
            String helloStr = String.format("Hello, %s!", helloTo);
            span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private void printHello(Span rootSpan, String helloStr) {
        Span span = tracer.buildSpan("printHello").asChildOf(rootSpan).start();
        try {
            System.out.println(helloStr);
            span.log(ImmutableMap.of("event", "println"));
        } finally {
            span.finish();
        }
    }

    public static void main(String[] args) {
        Tracer tracer = initTracer("hello-world");
        new Hello(tracer).sayHello(UUID.randomUUID().toString());
    }

    public static JaegerTracer initTracer(String service) {
        SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv().withType("const").withParam(1);
//        ReporterConfiguration reporterConfig = new Configuration.ReporterConfiguration(
//            false, "localhost", null, 1000, 10000)
        ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }
}
