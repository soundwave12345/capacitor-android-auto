# Capacitor 7 Android Auto Plugin

Plugin Capacitor 7 per integrare Android Auto con UI per player musicale.

[![Build](https://github.com/YOUR_USERNAME/capacitor-android-auto/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/capacitor-android-auto/actions)

## 🎯 Features

- ✅ **Capacitor 7** compatible
- ✅ UI base per player musicale su Android Auto
- ✅ Eventi per bottoni (play, pause, next, previous, stop)
- ✅ Aggiornamento stato player da JavaScript
- ✅ Logging estensivo per debug
- ✅ Solo Android (plugin nativo)
- ✅ Android Auto SDK 1.7.0-beta01

## 📋 Requisiti

- Capacitor 7.x
- Android API 23+ (Android 6.0+)
- Java 17
- Node.js 18+
- Gradle 8.11.1

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
    
    <!-- Opzionale: se vuoi personalizzare il nome -->
    <meta-data
        android:name="androidx.car.app.minCarApiLevel"
        android:value="1" />
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
  appId: 'com.yourcompany.app',
  appName: 'Your App',
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
  isPlaying: true
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
4. Avvia la tua app

### Con Auto Reale

1. Collega smartphone all'auto con cavo USB
2. Avvia Android Auto sull'auto
3. Apri la tua app
4. Naviga alla sezione media

### Debug Logs

```bash
# Filtra solo log del plugin
adb logcat | grep AndroidAuto

# Logs dettagliati con livelli
adb logcat AndroidAutoPlugin:V AndroidAutoService:V AndroidAutoScreen:V *:S

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

Emesso quando l'utente preme un bottone nell'UI Android Auto.

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

## 🔧 Configurazione Avanzata

### Custom Package Name

Modifica `android/build.gradle`:
```gradle
android {
    namespace "com.tuodominio.androidauto"
    // ...
}
```

E `AndroidAutoPlugin.java`:
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

### Android Auto non si connette

**Problema:** Servizio non appare in Android Auto

**Soluzione:**
1. Verifica che Android Auto sia installato
2. Controlla permessi in AndroidManifest.xml
3. Log: `adb logcat | grep CarAppService`

### Build fallisce su GitHub Actions

**Problema:** Gradle build error

**Soluzione:**
- Verifica Java 17 installato
- Controlla `gradle-wrapper.properties`
- Vedi artifacts per build reports dettagliati

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

- **1.0.0** - Release iniziale per Capacitor 7
  - Android Auto SDK 1.7.0-beta01
  - Gradle 8.11.1
  - Java 17
  - compileSdk 35

## 🔒 Sicurezza

⚠️ **Nota di Sicurezza**: Questo plugin usa `HostValidator.ALLOW_ALL_HOSTS_VALIDATOR` per debug. In produzione, considera di implementare una whitelist di host autorizzati.

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
- Android Auto SDK Team
- Contributors

## ⚠️ Limitazioni

- ❌ Solo Android (no iOS/Web)
- ⚠️ Android Auto SDK in beta
- ⚠️ Richiede Android 6.0+ (API 23)
- ℹ️ UI limitata dalle guidelines Android Auto
