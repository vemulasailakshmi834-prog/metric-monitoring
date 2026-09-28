FROM grafana/grafana:13.2.2

USER root
COPY infra/render/grafana/datasources/ /etc/grafana/provisioning/datasources/
COPY infra/grafana/dashboards/dashboard.yml /etc/grafana/provisioning/dashboards/dashboard.yml
COPY infra/grafana/dashboards/metrics-monitoring.json /etc/grafana/dashboards/metrics-monitoring.json
COPY infra/render/grafana/entrypoint.sh /usr/local/bin/render-entrypoint.sh
RUN chmod 755 /usr/local/bin/render-entrypoint.sh

USER grafana
EXPOSE 10000
ENTRYPOINT ["/usr/local/bin/render-entrypoint.sh"]