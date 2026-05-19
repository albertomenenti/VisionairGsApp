# Visionair Golden Stream — App Android

App Android per ascoltare lo stream live di **Visionair Golden Stream** con:

- Streaming audio HTTPS continuo
- Controlli media in notifica, lockscreen e Bluetooth
- Supporto **Android Auto**
- Supporto **Android TV** / Fire TV (Leanback launcher)
- Visualizzazione del programma in onda e bio del conduttore (palinsesto editabile)
- Compatibilità: Android 5.0 (API 21) fino ad Android 14 e oltre

L'app è **distribuita fuori dal Play Store**: l'APK viene compilato e firmato automaticamente da GitHub Actions e pubblicato come release scaricabile.

## Distribuzione

Ogni `push` sul branch `main` produce automaticamente un APK firmato disponibile nella sezione **Releases** del repository. La build dura ~5 minuti.

## Aggiornare il palinsesto

Il palinsesto è in `app/src/main/res/raw/programs.json`. Per aggiungere o modificare un programma, basta editare quel file e fare commit: la nuova versione dell'app conterrà il palinsesto aggiornato.

Formato di uno slot:
```json
{
  "id": "frequenze_nomadi",
  "title": "Frequenze Nomadi",
  "host": "Alberto Menenti",
  "description": "Border music...",
  "hostBio": "...",
  "slots": [
    {"day": "THURSDAY", "start": "21:00", "durationMinutes": 60}
  ]
}
```

Giorni validi: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`. Orari in formato `HH:MM` (24h), interpretati in **Europe/Rome**.

## Cambiare l'URL dello stream

Modifica la costante `STREAM_URL` in `app/src/main/java/it/visionair/gsapp/PlaybackService.kt`.

## Versioning

Per pubblicare una nuova versione visibile come aggiornamento agli utenti:

1. Apri `app/build.gradle.kts`
2. Incrementa `versionCode` (intero, +1 ad ogni release)
3. Aggiorna `versionName` (stringa, es. `1.0.1`)
4. Commit + push

## Struttura

```
VisionairGsApp/
├── .github/workflows/build-apk.yml    Workflow CI/CD
├── app/
│   ├── build.gradle.kts               Configurazione build app
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/it/visionair/gsapp/
│       │   ├── VisionairApp.kt        Application class
│       │   ├── PlaybackService.kt     Media3 streaming service
│       │   ├── ProgramSchedule.kt     Lettura palinsesto
│       │   ├── model/Program.kt
│       │   └── ui/MainActivity.kt     UI principale
│       └── res/
│           ├── layout/                Layout XML
│           ├── values/                Stringhe, colori, temi
│           ├── raw/programs.json      Palinsesto
│           └── xml/                   Manifest aux files
├── build.gradle.kts                   Build top-level
└── settings.gradle.kts
```

## Licenza

Tutti i diritti riservati — Alberto Menenti / Visionair.
