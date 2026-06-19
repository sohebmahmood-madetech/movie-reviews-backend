FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Remove the broken Yarn repository so 'apt-get update' succeeds during feature installation
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    && rm -f /etc/apt/sources.list.d/yarnpkg.list || true
