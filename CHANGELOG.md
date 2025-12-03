# Changelog

## [2.0.0] - 2025-12-03

### 🔄 BREAKING CHANGES - Migrazione a MediaBrowserService

Questa versione trasforma completamente il plugin da **CarAppService** a **MediaBrowserService**, l'approccio corretto per app musicali su Android Auto.

### ✨ Nuove Funzionalità

- **MediaBrowserService**: Implementato servizio standard Android per app musicali
- **MediaSession**: Gestione completa dei controlli multimediali
- **Notifiche**: Controlli multimediali automatici nella barra notifiche
- **Artwork Support**: Supporto per copertine album tramite `artworkUrl`
- **Integrazione Automatica**: Android Auto riconosce automaticamente l'app come app musicale

### 🗑️ Rimosso

- **CarAppService**: Rimossa implementazione (non adatta per app musicali)
- **AndroidAutoScreen**: Rimosso (UI gestita automaticamente da Android Auto)
- **automotive_app_desc.xml**: Non più necessario
- **Dipendenze car.app**: Rimosse `androidx.car.app:app` e `androidx.car.app:app-projected`

### 🔧 Modifiche

#### API TypeScript
- Aggiunto campo opzionale `artworkUrl` a `PlayerState`
- Eventi `buttonPressed` invariati (compatibilità retroattiva)

#### Android
- `AndroidAutoService` ora estende `MediaBrowserServiceCompat`
- Implementata `MediaSessionCompat` per controlli multimediali
- Aggiunto supporto per notifiche con controlli
- Cambiato intent-filter da `CarAppService` a `MediaBrowserService`
- Aggiunto `MediaButtonReceiver` per gestire eventi multimediali

### 📝 Migrazione

Per migrare dalla v1.x alla v2.x:

1. Aggiorna il plugin:
   ```bash
   npm update capacitor-android-auto
   npx cap sync android
   ```

2. Aggiungi `artworkUrl` nelle chiamate a `updatePlayerState`:
   ```typescript
   await AndroidAuto.updatePlayerState({
     // ... altri campi
     artworkUrl: 'https://example.com/cover.jpg' // ← NUOVO
   });
   ```

3. Pulisci e ricompila:
   ```bash
   cd android && ./gradlew clean && cd ..
   npx cap sync android
   ```

Vedi [MIGRATION.md](MIGRATION.md) per dettagli completi.

### 📚 Documentazione

- Aggiornato README con nuova architettura
- Creato MIGRATION.md per guida migrazione
- Aggiunto example-usage.ts con esempi pratici

### 🐛 Bug Fixes

- Risolto problema di categoria errata (era NAVIGATION, ora MEDIA)
- Corretta integrazione con Android Auto per app musicali

---

## [1.0.0] - 2025-12-03 (DEPRECATO)

### ⚠️ NOTA: Questa versione è deprecata

Usava `CarAppService` che non è l'approccio corretto per app musicali.
Si prega di aggiornare alla v2.0.0 che usa `MediaBrowserService`.

### Features (v1.0.0)

- Implementazione iniziale con CarAppService
- UI custom tramite AndroidAutoScreen
- Supporto base per controlli multimediali
- Eventi buttonPressed
- Integrazione Capacitor 7
