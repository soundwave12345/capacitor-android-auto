# Guida alla Migrazione: da CarAppService a MediaBrowserService

## 📋 Panoramica

Questo documento spiega come migrare dalla versione 1.x (CarAppService) alla versione 2.x (MediaBrowserService) del plugin Capacitor Android Auto.

## ❓ Perché Migrare?

**CarAppService** è progettato per app di navigazione e altre categorie, **NON per app musicali**.

**MediaBrowserService** è lo standard Android per app musicali e offre:
- ✅ Integrazione nativa con Android Auto per musica
- ✅ Controlli multimediali nelle notifiche
- ✅ Supporto per comandi vocali ("Ok Google, riproduci...")
- ✅ Gestione automatica dell'UI da parte di Android Auto
- ✅ Compatibilità con altri sistemi (Bluetooth, cuffie, ecc.)

## 🔄 Cosa Cambia

### 1. Architettura

**Prima (v1.x - CarAppService):**
```
App → Plugin → CarAppService → AndroidAutoScreen (UI custom)
```

**Dopo (v2.x - MediaBrowserService):**
```
App → Plugin → MediaBrowserService → MediaSession → Android Auto (UI automatica)
```

### 2. Codice JavaScript/TypeScript

#### Aggiornamento PlayerState

**Prima:**
```typescript
await AndroidAuto.updatePlayerState({
  title: 'Song Title',
  artist: 'Artist Name',
  album: 'Album Name',
  isPlaying: true,
  duration: 240000,
  position: 30000
});
```

**Dopo (aggiunto artworkUrl):**
```typescript
await AndroidAuto.updatePlayerState({
  title: 'Song Title',
  artist: 'Artist Name',
  album: 'Album Name',
  artworkUrl: 'https://example.com/cover.jpg', // ← NUOVO
  isPlaying: true,
  duration: 240000,
  position: 30000
});
```

#### Eventi (invariati)

Gli eventi `buttonPressed` funzionano esattamente come prima:

```typescript
await AndroidAuto.addListener('buttonPressed', (event) => {
  console.log('Button:', event.button); // 'play', 'pause', 'next', 'previous', 'stop'
});
```

### 3. Configurazione Android

#### AndroidManifest.xml

**Prima (v1.x):**
```xml
<service
    android:name=".AndroidAutoService"
    android:exported="true"
    android:label="AndroidAutoCapacitorPlugin">
    <intent-filter>
        <action android:name="androidx.car.app.CarAppService" />
        <category android:name="androidx.car.app.category.NAVIGATION"/>
    </intent-filter>
</service>

<meta-data
    android:name="androidx.car.app.minCarApiLevel"
    android:value="1" />
<meta-data
    android:name="androidx.car.app"
    android:resource="@xml/automotive_app_desc" />
```

**Dopo (v2.x):**
```xml
<service
    android:name=".AndroidAutoService"
    android:exported="true"
    android:label="Music Player">
    <intent-filter>
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>

<receiver 
    android:name="androidx.media.session.MediaButtonReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MEDIA_BUTTON" />
    </intent-filter>
</receiver>
```

#### build.gradle

**Prima (v1.x):**
```gradle
dependencies {
    implementation 'androidx.media:media:1.7.0'
    implementation 'androidx.car.app:app:1.7.0-beta01'
    implementation 'androidx.car.app:app-projected:1.7.0-beta01'
}
```

**Dopo (v2.x):**
```gradle
dependencies {
    implementation 'androidx.media:media:1.7.0'
    // Rimossi car.app - non più necessari
}
```

## 📝 Passi per la Migrazione

### 1. Aggiorna il Plugin

```bash
npm update capacitor-android-auto
npx cap sync android
```

### 2. Aggiorna il Codice TypeScript

Aggiungi il campo `artworkUrl` dove chiami `updatePlayerState`:

```typescript
// Aggiungi questo campo
artworkUrl: track.albumArtUrl || 'https://default-cover.jpg'
```

### 3. Rimuovi File Obsoleti (se presenti nella tua app)

Se hai copiato file del plugin nella tua app:

```bash
# Rimuovi questi file se esistono nella tua app
rm android/app/src/main/res/xml/automotive_app_desc.xml
```

### 4. Pulisci e Ricompila

```bash
cd android
./gradlew clean
cd ..
npx cap sync android
```

### 5. Testa

1. Avvia l'app
2. Riproduci musica
3. Verifica che i controlli appaiano:
   - Nella notifica
   - In Android Auto (se connesso)
   - Sui controlli Bluetooth (se disponibili)

## 🎯 Nuove Funzionalità

### 1. Notifiche con Controlli

Ora i controlli multimediali appaiono automaticamente nelle notifiche quando la musica è in riproduzione.

### 2. Artwork/Copertine

Puoi mostrare la copertina dell'album:

```typescript
await AndroidAuto.updatePlayerState({
  // ... altri campi
  artworkUrl: 'https://example.com/album-cover.jpg'
});
```

### 3. Integrazione Automatica

Android Auto riconosce automaticamente l'app come app musicale e la mostra nella sezione **Media**.

## ⚠️ Breaking Changes

### 1. UI Non Più Personalizzabile

**Prima:** Potevi personalizzare l'UI tramite `AndroidAutoScreen`

**Dopo:** L'UI è gestita automaticamente da Android Auto secondo le linee guida Google

**Motivo:** Le app musicali devono seguire un'interfaccia standard per sicurezza e coerenza.

### 2. Categoria Cambiata

**Prima:** App appariva in categoria NAVIGATION

**Dopo:** App appare in categoria MEDIA

### 3. File Rimossi

- `AndroidAutoScreen.java` - Non più necessario
- `automotive_app_desc.xml` - Non più necessario

## 🐛 Troubleshooting

### L'app non appare in Android Auto

**Soluzione:**
1. Assicurati che l'app stia riproducendo musica
2. Chiama `updatePlayerState` con `isPlaying: true`
3. Verifica i log: `adb logcat | grep MediaBrowser`

### Le notifiche non appaiono

**Soluzione:**
1. Verifica permessi notifiche
2. Assicurati di chiamare `updatePlayerState` regolarmente
3. Controlla che `isPlaying: true` quando la musica è in riproduzione

### Eventi non ricevuti

**Soluzione:**
Assicurati di usare `await` con `addListener`:

```typescript
// ✅ Corretto
const listener = await AndroidAuto.addListener('buttonPressed', handler);

// ❌ Sbagliato
AndroidAuto.addListener('buttonPressed', handler);
```

## 📊 Checklist Migrazione

- [ ] Aggiornato plugin a v2.x
- [ ] Aggiunto campo `artworkUrl` nelle chiamate a `updatePlayerState`
- [ ] Rimossi file obsoleti (se presenti)
- [ ] Pulito e ricompilato progetto Android
- [ ] Testato controlli in notifica
- [ ] Testato integrazione Android Auto
- [ ] Verificato che eventi `buttonPressed` funzionino
- [ ] Aggiornata documentazione interna

## 📚 Risorse

- [Android Media Apps Guide](https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice)
- [Android Auto Media Apps](https://developer.android.com/training/cars/media)
- [MediaSession Documentation](https://developer.android.com/guide/topics/media-apps/working-with-a-media-session)

## 💬 Supporto

Se hai problemi con la migrazione:

1. Controlla i log: `adb logcat | grep AndroidAuto`
2. Verifica la [documentazione](README.md)
3. Apri un issue su GitHub

## ✅ Vantaggi della Migrazione

- ✅ **Standard Android**: Usa le API ufficiali per app musicali
- ✅ **Più funzionalità**: Notifiche, comandi vocali, Bluetooth
- ✅ **Meno codice**: Android Auto gestisce l'UI automaticamente
- ✅ **Migliore UX**: Interfaccia coerente con altre app musicali
- ✅ **Future-proof**: Compatibile con futuri aggiornamenti Android Auto
