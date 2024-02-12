package ru.krsmon.zabbixrouterbridge.clients.config;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record ClientCfg(@NonNull String ip,
                        int port,
                        @NonNull String login,
                        @NonNull String password,
                        @Nullable String invite) {}
