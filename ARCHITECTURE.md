# Architecture — Metrics Monitoring and Alerting System

This document is the source of truth for how the system is designed. Implementation happens in phases. Later phases must not be built until the current phase is complete and tested.

## Purpose

This is a beginner-friendly Java project that shows how application metrics are collected, stored, visualized, and turned into alerts.

We will run everything **locally** first using Docker. The goal is to understand a real monitoring stack:

- an application that exposes Prometheus metrics
- Prometheus that scrapes those metrics
- Grafana that draws dashboards
- Nginx that load-balances user traffic
- Alertmanager that handles firing alerts
- a small Java webhook service that receives those alerts

Kafka and InfluxDB are optional later. They are **not** part of the core local stack.

## High-level architecture

```text
                         User / browser / curl
                                    |
                                    |  HTTP (port 80)
                                    v
                                 [ Nginx ]
                                    |
                    +---------------+---------------+
                    |               |               |
                    v               v               v
             sample-app-1    sample-app-2    sample-app-3
             (8080 inside     (8080 inside     (8080 inside
              each container)  each container)  each container)
                    ^               ^               ^
                    |               |               |
                    +-------+-------+-------+-------+
                            |  scrape /metrics
                            |  (NOT through Nginx)
                            v
                      [ Prometheus :9090 ]
                            |
                            | queries / alerts
                            +------------------+
                            |                  |
                            v                  v
                      [ Grafana :3000 ]  [ Alertmanager :9093 ]
                                               |
                                               | webhook
                                               v
                                        [ alert-webhook :8081 ]
```

Two traffic paths exist on purpose:

1. **Normal application traffic** goes through Nginx.
2. **Monitoring traffic** goes from Prometheus **directly** to each `sample-app` replica.

## Components

### Real infrastructure tools (we configure them; we do not write them)

| Component | Role |
|-----------|------|
| **Nginx** | Reverse proxy / load balancer for normal HTTP traffic to three `sample-app` replicas. |
| **Prometheus** | Pulls (scrapes) time-series metrics from each replica, stores them, evaluates alert rules. |
| **Grafana** | Visualizes metrics by querying Prometheus. |
| **Alertmanager** | Receives alerts from Prometheus, then groups, deduplicates, and routes them. |

### Our Java implementations (Spring Boot)

| Component | Role |
|-----------|------|
| **sample-app** | Demo Spring Boot app. Exposes application endpoints and Actuator Prometheus metrics. |
| **alert-webhook** | Receives Alertmanager webhook HTTP requests and, at first, logs them. |
| **metrics-collector** | Placeholder Maven module for an optional later collector. Not used in early phases. |
| **metrics-consumer** | Placeholder Maven module for an optional later consumer. Not used in early phases. |

### Optional later (do not implement until asked)

- **Kafka** — message bus for a more advanced pipeline
- **InfluxDB** — alternative or extra time-series store

## Data flow

1. A client calls Nginx on port **80** (for example `GET /work`).
2. Nginx forwards the request to one of the three `sample-app` replicas.
3. Each replica keeps its own in-process metrics (request counts, latency, errors, JVM stats).
4. Prometheus periodically **scrapes** `/actuator/prometheus` on **each replica**, not on Nginx.
5. Grafana queries Prometheus to show graphs.
6. When a Prometheus alert rule fires, Prometheus sends the alert to Alertmanager.
7. Alertmanager routes the alert to `alert-webhook`, which logs the payload.

## Planned ports

| Service | Port | Notes |
|---------|------|--------|
| Nginx | 80 | Public entry for application traffic |
| sample-app | 8080 | Internal container port only |
| alert-webhook | 8081 | Webhook HTTP listener |
| Prometheus | 9090 | UI and API |
| Grafana | 3000 | Dashboards |
| Alertmanager | 9093 | Alert UI and API |

## Application endpoints (later phases)

These are **not** implemented in Phase 0:

- `GET /work` — normal successful work
- `GET /slow` — slower response (useful for latency metrics)
- `GET /error` — error path (useful for error-rate alerts)
- `GET /actuator/prometheus` — Prometheus scrape endpoint

## Application traffic vs monitoring traffic

**Application traffic** is what a user or load test would send: `/work`, `/slow`, `/error`. That traffic should go through **Nginx** so we can practice load balancing across three replicas.

**Monitoring traffic** is Prometheus pulling metrics from `/actuator/prometheus`. That traffic must **not** go through Nginx.

### Why Prometheus scrapes replicas directly

If Prometheus scraped only Nginx:

- Nginx would hide which replica produced a metric.
- One replica could be unhealthy while another is fine, and Prometheus would see a mixed or incomplete picture.
- Scrape failures would look like “the load balancer failed,” not “replica 2 failed.”
- Per-instance labels (`instance`, replica name) would be wrong or missing.

Prometheus needs a **target per replica**. Nginx remains the front door for people; Prometheus talks to each app instance itself.

## Folder structure

```text
metrics-monitoring/
├── pom.xml                 # Maven parent (packaging pom)
├── ARCHITECTURE.md         # This file
├── README.md
├── .gitignore
├── .cursor/rules/          # Cursor agent rules
├── sample-app/             # Java module (code in a later phase)
├── alert-webhook/          # Java module (code in a later phase)
├── metrics-collector/      # Placeholder module
├── metrics-consumer/       # Placeholder module
└── infra/
    ├── prometheus/         # Prometheus config (later)
    ├── alertmanager/       # Alertmanager config (later)
    ├── grafana/            # Grafana provisioning (later)
    └── nginx/              # Nginx config (later)
```

Phase 0 creates this layout. It does **not** add `docker-compose.yml`, infra YAML, or Java source files.

## Phase plan

### Phase 0 — Project setup (complete)

- Multi-module Maven parent
- Empty module POMs
- Architecture and README
- Cursor rules
- Infra folder placeholders
- No running services yet

### Phase 1 — sample-app (current: single instance, no Docker Compose)

- Spring Boot app with `/work`, `/slow`, `/error`
- Actuator + Prometheus registry
- Prove metrics appear at `/actuator/prometheus`

### Phase 2 — Dockerize sample-app and add Prometheus

- Dockerfile for `sample-app`
- Prometheus scrape config targeting the app **directly**
- Confirm Prometheus UI shows the target as UP

### Phase 3 — Replicas and Nginx

- Three `sample-app` replicas
- Nginx on port 80 for application traffic
- Prometheus scrape jobs for **each replica**, still not through Nginx

### Phase 4 — Grafana

- Grafana connected to Prometheus
- Basic dashboard (request rate, errors, latency)

### Phase 5 — Alerting

- Prometheus alert rules
- Alertmanager
- `alert-webhook` logs incoming alerts

### Phase 6 — Hardening and local runbook

- Health checks, documentation of how to run and test the full local stack
- Confirm the two traffic paths still hold

### Phase 7 — Optional advanced (only if requested)

- Kafka and/or InfluxDB
- `metrics-collector` / `metrics-consumer` if they are needed for that design

Do not start a later phase until the current phase is tested.

## What Phase 0 does not include

- Java source code
- `docker-compose.yml`
- Prometheus, Grafana, Nginx, or Alertmanager configuration files
- Kafka or InfluxDB
