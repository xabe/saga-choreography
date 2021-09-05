package com.xabe.choreography.integration;

import static java.lang.String.format;

public class UrlUtil {

  private static final String SCHEMA_REGISTRY = "http://%s:%s/subjects/%s-value/versions";

  private static final String SCHEMA_REGISTRY_COMPATIBILITY = "http://%s:%s/config/%s-value";

  private static final UrlUtil INSTANCE = new UrlUtil();

  private final String urlSchemaRegistryPayment;

  private final String urlSchemaRegistryCompatibilityPayment;

  private final String urlSchemaRegistryShipping;

  private final String urlSchemaRegistryCompatibilityShipping;

  private final String urlSchemaRegistryStatus;

  private final String urlSchemaRegistryCompatibilityStatus;

  private UrlUtil() {
    final String registryHost = System.getProperty("schemaregistry.host", "localhost");
    final String registryPort = System.getProperty("schemaregistry.port", "8081");
    this.urlSchemaRegistryPayment = format(SCHEMA_REGISTRY, registryHost, registryPort, "payments.v1");
    this.urlSchemaRegistryShipping = format(SCHEMA_REGISTRY, registryHost, registryPort, "shipments.v1");
    this.urlSchemaRegistryStatus = format(SCHEMA_REGISTRY, registryHost, registryPort, "status.v1");
    this.urlSchemaRegistryCompatibilityPayment = format(SCHEMA_REGISTRY_COMPATIBILITY, registryHost, registryPort, "payments.v1");
    this.urlSchemaRegistryCompatibilityShipping = format(SCHEMA_REGISTRY_COMPATIBILITY, registryHost, registryPort, "shipments.v1");
    this.urlSchemaRegistryCompatibilityStatus = format(SCHEMA_REGISTRY_COMPATIBILITY, registryHost, registryPort, "status.v1");
  }

  public static UrlUtil getInstance() {
    return INSTANCE;
  }

  public String getUrlSchemaRegistryStatus() {
    return this.urlSchemaRegistryStatus;
  }

  public String getUrlSchemaRegistryCompatibilityStatus() {
    return this.urlSchemaRegistryCompatibilityStatus;
  }

  public String getSchemaRegistryCompatibilityPayment() {
    return this.urlSchemaRegistryCompatibilityPayment;
  }

  public String getSchemaRegistryPayment() {
    return this.urlSchemaRegistryPayment;
  }

  public String getUrlSchemaRegistryShipping() {
    return this.urlSchemaRegistryShipping;
  }

  public String getUrlSchemaRegistryCompatibilityShipping() {
    return this.urlSchemaRegistryCompatibilityShipping;
  }
}

