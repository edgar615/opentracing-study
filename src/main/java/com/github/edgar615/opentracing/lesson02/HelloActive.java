package com.github.edgar615.opentracing.lesson02;

import com.google.common.collect.ImmutableMap;
import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HelloActive {

    private final Tracer tracer;

    private HelloActive(Tracer tracer) {
        this.tracer = tracer;
    }

    private void sayHello(String helloTo) {
        try (Scope scope = tracer.buildSpan("say-hello").startActive(true)) {
            scope.span().setTag("hello-to", helloTo);

            String helloStr = formatString(helloTo);
            printHello(helloStr);
        }
    }

    private  String formatString(String helloTo) {
        try (Scope scope = tracer.buildSpan("formatString").startActive(true)) {
            String helloStr = String.format("Hello, %s!", helloTo);
            scope.span().log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        }
    }

    private void printHello(String helloStr) {
        try (Scope scope = tracer.buildSpan("printHello").startActive(true)) {
            System.out.println(helloStr);
            scope.span().log(ImmutableMap.of("event", "println"));
        }
    }

    public static void main(String[] args) {
        Tracer tracer = initTracer("hello-world");
        new HelloActive(tracer).sayHello(UUID.randomUUID().toString());
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
