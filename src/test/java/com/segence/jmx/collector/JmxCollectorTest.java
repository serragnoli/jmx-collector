package com.segence.jmx.collector;

import org.junit.Test;

import javax.management.Attribute;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class JmxCollectorTest {

    private final static String CURRENT_SYSTEM_ARCHITECTURE = System.getProperty("os.arch");
    private final static int NUMBER_OF_CPU_CORES = Runtime.getRuntime().availableProcessors();

    @Test
    public void shouldReportInvalidMbeansAndAttributeValues() {

        Map<ObjectName, Set<String>> mbeansAndAttributesToQuery = new HashMap<ObjectName, Set<String>>() {{
            try {
                put(
                    new ObjectName("java.lang:type=OperatingSystem"),
                    new HashSet<String>() {{
                        add("non-valid-attribute");
                    }}
                );
                put(
                    new ObjectName("java.lang:type=not-valid-object"),
                    new HashSet<String>() {{
                        add("another-non-valid-attribute");
                    }}
                );
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        }};

        Set<MBeanMetricResult> expectedResult = Stream.of(
            new MBeanMetricResult(
                new MBeanMetric(
                    "com.sun.management.internal.OperatingSystemImpl",
                    Collections.emptyList()
                )
            )).collect(Collectors.toSet());

        Set<MBeanMetricResult> result = JmxCollector.queryAsSet(mbeansAndAttributesToQuery);

        assertThat(result, is(expectedResult));
    }

    @Test
    public void shouldReturnAllValidMbeansAndAttributeValues() {

        Map<ObjectName, Set<String>> mbeansAndAttributesToQuery = new HashMap<ObjectName, Set<String>>() {{
            try {
                put(
                    new ObjectName("java.lang:type=OperatingSystem"),
                    new HashSet<String>() {{
                        add("Arch");
                        add("AvailableProcessors");
                    }}
                );
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        }};

        Set<MBeanMetricResult> expectedResult = Stream.of(
            new MBeanMetricResult(
                new MBeanMetric(
                    "com.sun.management.internal.OperatingSystemImpl",
                    Arrays.asList(
                        new Attribute("AvailableProcessors", NUMBER_OF_CPU_CORES),
                        new Attribute("Arch", CURRENT_SYSTEM_ARCHITECTURE)
                    )
                )
        )).collect(Collectors.toSet());

        Set<MBeanMetricResult> result = JmxCollector.queryAsSet(mbeansAndAttributesToQuery);

        assertThat(result, is(expectedResult));
    }
}