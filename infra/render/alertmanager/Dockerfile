FROM alpine:3.22

COPY --from=prom/alertmanager:v0.28.1 /bin/alertmanager /bin/alertmanager
COPY infra/render/alertmanager/alertmanager.yml.template /etc/alertmanager/alertmanager.yml.template
COPY infra/render/alertmanager/entrypoint.sh /entrypoint.sh

RUN chmod 755 /entrypoint.sh && mkdir -p /alertmanager && chown nobody:nobody /alertmanager

USER nobody
EXPOSE 9093
ENTRYPOINT ["/bin/sh", "/entrypoint.sh"]
CMD ["--storage.path=/alertmanager"]