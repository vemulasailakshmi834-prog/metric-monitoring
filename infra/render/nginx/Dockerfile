FROM nginx:1.31.3-alpine

COPY infra/render/nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY infra/render/nginx/19-render-resolver.envsh /docker-entrypoint.d/19-render-resolver.envsh
RUN chmod 755 /docker-entrypoint.d/19-render-resolver.envsh

ENV NGINX_ENVSUBST_FILTER=^(PORT|SAMPLE_APP_1_HOSTPORT|SAMPLE_APP_2_HOSTPORT|SAMPLE_APP_3_HOSTPORT|NGINX_DNS_RESOLVER)$

EXPOSE 10000