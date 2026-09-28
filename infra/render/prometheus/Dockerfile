FROM prom/prometheus:v3.15.0

COPY infra/render/prometheus/prometheus.yml /etc/prometheus/prometheus.yml.template
COPY infra/prometheus/alerts.yml /etc/prometheus/alerts.yml
COPY infra/render/prometheus/entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/bin/sh", "/entrypoint.sh"]
CMD ["--storage.tsdb.path=/prometheus"]
EXPOSE 9090