FROM caddy:alpine-builder AS builder

RUN xcaddy build \
    --with github.com/chalabi2/caddy-usage \
    --with github.com/caddyserver/cache-handler

FROM caddy:alpine

COPY --from=builder /usr/bin/caddy /usr/bin/caddy