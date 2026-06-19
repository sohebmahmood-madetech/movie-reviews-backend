# Use a stable Debian-based image that includes SDKMAN
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Switch to the vscode user to use SDKMAN (it's installed in the user's home directory)
USER vscode

# Install Java 25 using SDKMAN and set it as the default.
# We are using '25-open' for OpenJDK. 
# If you want the Microsoft Build of OpenJDK, change '25-open' to '25-ms'.
RUN bash -c "source /usr/local/sdkman/bin/sdkman-init.sh && \
    sdk install java 25-open && \
    sdk default java 25-open"

# Switch back to root to fix the Yarn issue and allow features to install
USER root

# Remove the broken Yarn repository so 'apt-get update' succeeds during feature installation
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    && rm -f /etc/apt/sources.list.d/yarnpkg.list || true
