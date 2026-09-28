# Metrics Monitoring and Alerting System

A local Java 17 and Spring Boot 3 monitoring stack. Docker Compose runs three sample applications, Prometheus, Grafana, Nginx, Alertmanager, a webhook receiver, Kafka, a Kafka producer, InfluxDB, and a Kafka consumer.

## Architecture

```text
Client
	|
	v
Nginx :8081 ----------------------> sample-app-1 :8080
															+---> sample-app-2 :8080
															+---> sample-app-3 :8080
																				 |
Prometheus :9090 <-----------------------+ (scrapes apps directly)
	|                                      |
	+--> Grafana :3000                     +--> HTTP/JVM metrics
	|
	+--> Alert rules --> Alertmanager :9093 --> alert-webhook :8081 (internal)

metrics-collector :8082 --> Kafka :9092, topic `metrics`
																			|
																			v
															metrics-consumer --> InfluxDB :8086
																			|
																			+--> Grafana InfluxDB datasource
```

All services share the Compose `monitoring` bridge network. Prometheus scrapes each `sample-app` container by its Docker service name, not through Nginx. The existing Grafana dashboard uses Prometheus; a separate InfluxDB datasource is provisioned for Kafka-ingested events.

## Technology Stack

- Java 17, Spring Boot 3.4.5, Maven multi-module build
- Spring Web, Actuator, Micrometer and Prometheus registry
- Prometheus, Grafana, Nginx, Alertmanager
- Apache Kafka 4.1.1 in single-node KRaft mode (no ZooKeeper)
- InfluxDB 2.7 and the InfluxDB Java client
- Docker Compose with persistent volumes for Prometheus, Grafana, Kafka, and InfluxDB

## Directory Structure

```text
metrics-monitoring/
├── pom.xml
├── docker-compose.yml
├── sample-app/                 # HTTP demo and Prometheus metrics
├── alert-webhook/              # Alertmanager POST receiver
├── metrics-collector/          # POST /metrics producer for Kafka
├── metrics-consumer/           # Kafka consumer writing to InfluxDB
└── infra/
		├── alertmanager/           # Webhook route
		├── grafana/                # Datasources and Prometheus dashboard
		├── nginx/                  # Round-robin upstreams
		└── prometheus/              # Scrape configuration and alert rules
```

The local `.env` file supplies InfluxDB initialization credentials and is ignored by Git. Do not commit it or share its token.

## Start and Stop

Run from the repository root. The local `.env` file must be present.

```powershell
docker compose config
docker compose build
docker compose up -d
docker compose ps
```

Stop without removing containers:

```powershell
docker compose stop
```

Stop and remove containers/network while preserving named data volumes:

```powershell
docker compose down
```

Do not use `docker compose down -v` unless you intend to delete Prometheus, Grafana, Kafka, and InfluxDB data.

## Local URLs

| Service | URL | Notes |
|---|---|---|
| Nginx / sample app | http://localhost:8081 | App traffic and load balancing |
| Metrics collector | http://localhost:8082 | `POST /metrics` |
| Grafana | http://localhost:3000 | Prometheus dashboard and InfluxDB datasource |
| Prometheus | http://localhost:9090 | Targets: `/targets`; rules: `/rules`; alerts: `/alerts` |
| Alertmanager | http://localhost:9093 | Alert status |
| InfluxDB | http://localhost:8086 | UI and API |
| Kafka external listener | localhost:9092 | Internal application listener is `kafka:9092` |

Application replicas, the alert webhook, and metrics-consumer are not published directly to the host. Inside Docker, the webhook is `alert-webhook:8081`, and metrics-consumer is a background process with no HTTP API.

## Services

| Service | Role |
|---|---|
| `sample-app-1`, `sample-app-2`, `sample-app-3` | Demo endpoints and JVM/HTTP metrics |
| `nginx` | Round-robin reverse proxy for browser/client traffic |
| `prometheus` | Directly scrapes the three apps and evaluates alert rules |
| `grafana` | Visualizes Prometheus metrics and can query InfluxDB |
| `alertmanager` | Groups/routes Prometheus alerts |
| `alert-webhook` | Logs Alertmanager webhook payloads |
| `kafka` | Single-node KRaft broker; topic `metrics` has three partitions and one replica |
| `metrics-collector` | Accepts metric JSON and publishes it to Kafka |
| `influxdb` | Persistent time-series storage, organization `metrics-org`, bucket `metrics` |
| `metrics-consumer` | Consumes Kafka events in group `metrics-consumer-group` and writes InfluxDB points |

## Data Flows

**Application metrics:** Nginx forwards client traffic to the three apps. Prometheus independently scrapes each app at `/actuator/prometheus`; it does not scrape through Nginx. Grafana's existing six-panel dashboard queries Prometheus.

**Alerts:** Prometheus evaluates `infra/prometheus/alerts.yml`, sends firing/resolved alerts to Alertmanager, and Alertmanager posts them to `http://alert-webhook:8081/alerts`.

**Kafka and InfluxDB:** `POST /metrics` at the collector publishes JSON events to the existing Kafka topic `metrics`. `metrics-consumer` reads the topic and writes each event to InfluxDB measurement `metrics`, field `value`, with tags `metricName`, `source`, and `instance`.

## Example Requests

Application traffic through Nginx:

```bash
curl http://localhost:8081/work
curl http://localhost:8081/slow
curl -i http://localhost:8081/error
```

`/error` intentionally returns HTTP 500. The response includes `X-Upstream-Server` to identify the selected app instance.

Publish a metric event:

```bash
curl -X POST http://localhost:8082/metrics \
	-H "Content-Type: application/json" \
	-d '{"metricName":"http_requests_total","value":1,"timestamp":"2026-09-28T10:00:00Z","source":"sample-app","instance":"sample-app-1"}'
```

Prometheus expressions to try:

```promql
up{job="sample-app"}
rate(http_server_requests_seconds_count{uri!="/actuator/prometheus"}[1m])
```

## Grafana

Open the provisioned **Metrics Monitoring Dashboard** to inspect request rate, 5xx rate, p95 latency, heap memory, CPU, and per-instance request rate. The default Prometheus datasource remains in place. The additional `InfluxDB` datasource is non-default and points to `http://influxdb:8086`, organization `metrics-org`, bucket `metrics`; its token is supplied from the ignored environment file.

## Troubleshooting

- If Compose reports a missing InfluxDB variable, check that the ignored root `.env` contains the required `INFLUXDB_INIT_*` values. Never paste the token into tracked source or logs.
- If a service is not ready, inspect `docker compose ps` and `docker compose logs --tail=100 <service>`.
- Kafka and InfluxDB have healthchecks. The collector and consumer wait for their dependencies' health before starting.
- Check `http://localhost:9090/targets` if an application target is down. Prometheus must target `sample-app-1:8080`, `sample-app-2:8080`, and `sample-app-3:8080` directly.
- If Nginx returns 502 after recreating app containers, run `docker compose restart nginx` to refresh upstream DNS resolution.
- If changing InfluxDB bootstrap credentials after its data volume has been initialized, changing `.env` alone does not reinitialize the existing volume.
- A host port conflict will prevent the affected service from starting; check ports 3000, 8081, 8082, 8086, 9090, 9092, and 9093.

## Useful Docker Commands

```bash
docker compose config --quiet
docker compose build
docker compose up -d
docker compose ps
docker compose logs --tail=100
docker compose logs -f metrics-consumer
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --describe --topic metrics
docker compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka:9092 --describe --group metrics-consumer-group
docker compose stop
docker compose down
```
