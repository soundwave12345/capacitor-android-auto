# Capacitor 7 Android Auto Plugin (MediaBrowserService)

Plugin Capacitor 7 per integrare Android Auto con **MediaBrowserService** per app musicali.

[![Build](https://github.com/YOUR_USERNAME/capacitor-android-auto/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/capacitor-android-auto/actions)

## 🎯 Features

- ✅ **Capacitor 7** compatible
- ✅ **MediaBrowserService** per app musicali
- ✅ **MediaSession** per controlli multimediali
- ✅ Supporto completo Android Auto
- ✅ Notifiche con controlli multimediali
- ✅ Eventi per bottoni (play, pause, next, previous, stop)
- ✅ Aggiornamento stato player da JavaScript
- ✅ Supporto metadata (titolo, artista, album, artwork)
- ✅ Logging estensivo per debug
- ✅ Solo Android (plugin nativo)

## 📋 Requisiti

- Capacitor 7.x
- Android API 23+ (Android 6.0+)
- Java 17+
- Node.js 18+
- Gradle 8.11.1+

## 📦 Installazione

```bash
npm install capacitor-android-auto
npx cap sync android
```

## 🔧 Configurazione

### Android

Il plugin include già la configurazione necessaria. Nella tua app principale, assicurati di avere:

**android/app/src/main/AndroidManifest.xml:**
```xml
<application>
    <!-- ... altre configurazioni ... -->
</application>

<uses-feature
    android:name="android.hardware.type.automotive"
    android:required="false" />
```

### Capacitor 7 Configuration

**capacitor.config.ts:**
```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.yourcompany.musicapp',
  appName: 'Your Music App',
  webDir: 'dist',
  plugins: {
    AndroidAuto: {
      // Configurazioni opzionali future
    }
  }
};

export default config;
```

## 💻 Utilizzo

### TypeScript (raccomandato)

```typescript
import { AndroidAuto } from 'capacitor-android-auto';
import type { PluginListenerHandle } from '@capacitor/core';

class MusicPlayerService {
  private buttonListener?: PluginListenerHandle;

  async initialize() {
    // Aggiungi listener per eventi UI
    this.buttonListener = await AndroidAuto.addListener('buttonPressed', (event) => {
      console.log('Button pressed:', event.button);
      console.log('Timestamp:', event.timestamp);
      
      this.handleButton(event.button);
    });

    // Stato iniziale
    await this.updateUI();
  }

  private handleButton(button: string) {
    switch(button) {
      case 'play':
        this.play();
        break;
      case 'pause':
        this.pause();
        break;
      case 'next':
        this.nextTrack();
        break;
      case 'previous':
        this.previousTrack();
        break;
      case 'stop':
        this.stop();
        break;
    }
  }

  async updateUI() {
    await AndroidAuto.updatePlayerState({
      title: 'Nome Canzone',
      artist: 'Nome Artista',
      album: 'Nome Album',
      artworkUrl: 'https://example.com/artwork.jpg', // Opzionale
      isPlaying: true,
      duration: 240000, // ms
      position: 30000   // ms
    });
  }

  async cleanup() {
    // Importante: rimuovi listener quando non serve più
    await this.buttonListener?.remove();
  }
}
```

### JavaScript

```javascript
import { AndroidAuto } from 'capacitor-android-auto';

// Aggiungi listener
const listener = await AndroidAuto.addListener('buttonPressed', (data) => {
  console.log('Button:', data.button);
  
  if (data.button === 'play') {
    // Avvia riproduzione
  }
});

// Aggiorna UI
await AndroidAuto.updatePlayerState({
  title: 'Song Title',
  artist: 'Artist Name',
  album: 'Album Name',
  artworkUrl: 'https://example.com/cover.jpg',
  isPlaying: true,
  duration: 180000,
  position: 30000
});

// Cleanup
await listener.remove();
```

## 🧪 Testing

### Con Android Auto Desktop Head Unit (DHU)

1. Scarica [Android Auto DHU](https://github.com/google/android-auto-desktop-head-unit)
2. Connetti dispositivo via ADB:
   ```bash
   adb forward tcp:5277 tcp:5277
   ```
3. Avvia DHU:
   ```bash
   ./desktop-head-unit
   ```
4. Avvia la tua app e riproduci musica

### Con Auto Reale

1. Collega smartphone all'auto con cavo USB
2. Avvia Android Auto sull'auto
3. Apri la tua app e riproduci musica
4. L'app apparirà nella sezione **Media** di Android Auto

### Debug Logs

```bash
# Filtra solo log del plugin
adb logcat | grep AndroidAuto

# Logs dettagliati con livelli
adb logcat AndroidAutoPlugin:V AndroidAutoService:V *:S

# Salva logs su file
adb logcat | grep AndroidAuto > android-auto-logs.txt
```

## 🏗️ Build da Source

```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/capacitor-android-auto.git
cd capacitor-android-auto

# Installa dipendenze
npm install

# Build TypeScript
npm run build

# Verifica
npm run verify:web

# Build Android
cd android
./gradlew clean build --info
cd ..

# Crea package
npm pack
```

## 🔄 GitHub Actions

Il progetto include CI/CD automatico:
- ✅ Build TypeScript automatica
- ✅ Build Android con Gradle 8.11.1
- ✅ Test su ogni push/PR
- ✅ Upload artifacts (AAR files)
- ✅ Build reports su failure

## 📝 API Reference

### Methods

#### `updatePlayerState(options: PlayerState): Promise<void>`

Aggiorna lo stato del player su Android Auto.

**Parameters:**
```typescript
interface PlayerState {
  title: string;        // Titolo traccia
  artist: string;       // Nome artista
  album?: string;       // Nome album (opzionale)
  artworkUrl?: string;  // URL copertina album (opzionale)
  isPlaying: boolean;   // Stato riproduzione
  duration?: number;    // Durata in ms (opzionale)
  position?: number;    // Posizione in ms (opzionale)
}
```

**Example:**
```typescript
await AndroidAuto.updatePlayerState({
  title: 'Bohemian Rhapsody',
  artist: 'Queen',
  album: 'A Night at the Opera',
  artworkUrl: 'https://example.com/queen-cover.jpg',
  isPlaying: true,
  duration: 354000,
  position: 45000
});
```

#### `startService(): Promise<void>`

Avvia manualmente il servizio Android Auto (raramente necessario).

```typescript
await AndroidAuto.startService();
```

#### `stopService(): Promise<void>`

Ferma il servizio Android Auto.

```typescript
await AndroidAuto.stopService();
```

### Events

#### `buttonPressed`

Emesso quando l'utente preme un bottone nell'UI Android Auto o nella notifica.

**Event Data:**
```typescript
interface ButtonPressedEvent {
  button: 'play' | 'pause' | 'next' | 'previous' | 'stop';
  timestamp: number; // Unix timestamp in ms
}
```

**Example:**
```typescript
await AndroidAuto.addListener('buttonPressed', (event) => {
  console.log(`Button ${event.button} pressed at ${new Date(event.timestamp)}`);
});
```

## 🎵 Come Funziona

Questo plugin utilizza **MediaBrowserService** e **MediaSession**, l'approccio standard per app musicali su Android Auto:

1. **MediaBrowserService**: Espone la tua libreria musicale ad Android Auto
2. **MediaSession**: Gestisce i controlli multimediali (play, pause, next, etc.)
3. **Notifiche**: Mostra controlli multimediali nella barra delle notifiche
4. **Android Auto**: Si connette automaticamente al servizio quando disponibile

### Architettura

```
┌─────────────────┐
│   Capacitor     │
│   JavaScript    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ AndroidAuto     │
│    Plugin       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ MediaBrowser    │
│    Service      │◄──── Android Auto
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  MediaSession   │◄──── Notifiche
└─────────────────┘
```

## 🔧 Configurazione Avanzata

### Custom Package Name

Modifica `android/build.gradle`:
```gradle
android {
    namespace "com.tuodominio.androidauto"
    // ...
}
```

E tutti i file Java:
```java
package com.tuodominio.androidauto;
```

## 🐛 Troubleshooting

### Plugin non si carica

**Problema:** Plugin non trovato in Capacitor 7

**Soluzione:**
```bash
# Verifica installazione
npm list capacitor-android-auto

# Sync forzato
npx cap sync android --force

# Ricompila
cd android && ./gradlew clean build
```

### Android Auto non mostra l'app

**Problema:** App non appare nella sezione Media di Android Auto

**Soluzione:**
1. Verifica che Android Auto sia installato e aggiornato
2. Controlla che il servizio sia dichiarato correttamente nel Manifest
3. Assicurati che l'app stia riproducendo musica
4. Log: `adb logcat | grep MediaBrowser`

### Notifiche non appaiono

**Problema:** Controlli multimediali non visibili

**Soluzione:**
1. Verifica permessi notifiche
2. Assicurati di chiamare `updatePlayerState` con `isPlaying: true`
3. Controlla log: `adb logcat | grep AndroidAutoService`

### Eventi non ricevuti in JavaScript

**Problema:** Listener non riceve eventi

**Soluzione:**
```typescript
// ✅ Corretto - await addListener
const listener = await AndroidAuto.addListener('buttonPressed', handler);

// ❌ Sbagliato - no await
AndroidAuto.addListener('buttonPressed', handler);
```

## 📊 Versioning

- **2.0.0** - Migrazione a MediaBrowserService
  - ✅ MediaBrowserService invece di CarAppService
  - ✅ MediaSession per controlli multimediali
  - ✅ Notifiche con controlli
  - ✅ Supporto artwork
  - ✅ Compatibile con standard Android Auto per app musicali
  
- **1.0.0** - Release iniziale (deprecata)
  - ⚠️ Usava CarAppService (non adatto per app musicali)

## 📄 License

MIT © [Your Name]

## 🤝 Contributing

Contributi benvenuti! Per modifiche importanti:

1. Apri prima un issue
2. Fork del progetto
3. Crea feature branch (`git checkout -b feature/amazing`)
4. Commit (`git commit -m 'Add amazing feature'`)
5. Push (`git push origin feature/amazing`)
6. Apri Pull Request

## 🙏 Credits

- Capacitor Team
- Android Media Team
- Contributors

## ⚠️ Limitazioni

- ❌ Solo Android (no iOS/Web)
- ⚠️ Richiede Android 6.0+ (API 23)
- ℹ️ L'UI è gestita automaticamente da Android Auto (standard per app musicali)
- ℹ️ La libreria musicale deve essere gestita dalla tua app

## 📚 Risorse Utili

- [Android Media API](https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice)
- [Android Auto Media Apps](https://developer.android.com/training/cars/media)
- [MediaSession Guide](https://developer.android.com/guide/topics/media-apps/working-with-a-media-session)
- [Capacitor Plugins](https://capacitorjs.com/docs/plugins)
