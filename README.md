# Battery Monitor

Uma aplicação Android que monitora o nível de bateria do dispositivo em tempo real.

## Features

- 📊 Exibição do nível de bateria atual
- 🔔 Notificações de bateria fraca
- 🔊 Alerta sonoro quando bateria atinge 5%
- ⏸️ Controle de monitoramento (iniciar/parar)
- 🎨 Interface limpa e intuitiva

## Requisitos

- Android 5.0+ (API Level 21)
- Android 14+ (API Level 34) compilado

## Permissões

- `BATTERY_STATS` - Para ler informações de bateria
- `FOREGROUND_SERVICE` - Para executar serviço em foreground
- `FOREGROUND_SERVICE_DATA_SYNC` - Para sincronização de dados

## Build

```bash
./gradlew assembleRelease
```

O APK será gerado em `app/build/outputs/apk/release/app-release.apk`

## Desenvolvimento

```bash
./gradlew assembleDebug
```

## Arquitetura

- **MainActivity** - Interface principal
- **BatteryMonitorService** - Serviço de monitoramento em foreground
- **BatteryReceiver** - Broadcast receiver para mudanças de bateria
- **BatteryService** - Serviço auxiliar
