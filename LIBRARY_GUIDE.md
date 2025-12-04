# 🎵 Guida: Popolare la Libreria Musicale in Android Auto

## 📋 Panoramica

Quando apri l'app da Android Auto, ora puoi mostrare:
- ✅ **Canzoni Recenti** - Ultime tracce ascoltate
- ✅ **Playlist** - Le tue playlist organizzate
- ✅ **Album** - Navigazione per album
- ✅ **Artisti** - Navigazione per artista

## 🚀 Quick Start

### 1. Imposta la Libreria Musicale

```typescript
import { AndroidAuto } from 'capacitor-android-auto';

await AndroidAuto.setMediaLibrary({
  // Canzoni recenti (mostrate subito)
  recentTracks: [
    {
      id: 'track_1',
      title: 'Bohemian Rhapsody',
      artist: 'Queen',
      album: 'A Night at the Opera',
      artworkUrl: 'https://example.com/cover.jpg',
      duration: 354000, // in millisecondi
      isPlayable: true
    },
    {
      id: 'track_2',
      title: 'Stairway to Heaven',
      artist: 'Led Zeppelin',
      duration: 482000,
      isPlayable: true
    }
  ],
  
  // Playlist
  playlists: [
    {
      id: 'playlist_1',
      title: 'Rock Classics',
      subtitle: '50 brani',
      items: [
        {
          id: 'track_3',
          title: 'Sweet Child O\' Mine',
          artist: 'Guns N\' Roses',
          isPlayable: true
        }
      ]
    }
  ],
  
  // Album (opzionale)
  albums: [
    {
      id: 'album_1',
      title: 'A Night at the Opera',
      subtitle: 'Queen',
      items: [/* tracce dell'album */]
    }
  ],
  
  // Artisti (opzionale)
  artists: [
    {
      id: 'artist_1',
      title: 'Queen',
      subtitle: '20 brani',
      items: [/* tracce dell'artista */]
    }
  ]
});
```

### 2. Ascolta Quando l'Utente Seleziona una Canzone

```typescript
await AndroidAuto.addListener('mediaItemSelected', (event) => {
  console.log('Utente ha selezionato:', event.mediaId);
  
  // Trova la canzone e riproducila
  const track = findTrackById(event.mediaId);
  if (track) {
    playTrack(track);
  }
});
```

### 3. Aggiorna lo Stato del Player

```typescript
await AndroidAuto.updatePlayerState({
  title: track.title,
  artist: track.artist,
  isPlaying: true,
  duration: track.duration,
  position: 0
});
```

## 📱 Cosa Vede l'Utente in Android Auto

### Schermata Principale
```
┌─────────────────────────────┐
│  🎵 Your Music App          │
├─────────────────────────────┤
│  📂 Recenti                 │
│     Ultime canzoni ascoltate│
│                             │
│  📂 Playlist                │
│     Le tue playlist         │
│                             │
│  📂 Album                   │
│     Tutti gli album         │
│                             │
│  📂 Artisti                 │
│     Tutti gli artisti       │
└─────────────────────────────┘
```

### Quando Clicca su "Recenti"
```
┌─────────────────────────────┐
│  ← Recenti                  │
├─────────────────────────────┤
│  🎵 Bohemian Rhapsody       │
│     Queen                   │
│                             │
│  🎵 Stairway to Heaven      │
│     Led Zeppelin            │
│                             │
│  🎵 Hotel California        │
│     Eagles                  │
└─────────────────────────────┘
```

## 💡 Esempi Pratici

### Esempio 1: Libreria Semplice (Solo Recenti)

```typescript
// Perfetto per iniziare
await AndroidAuto.setMediaLibrary({
  recentTracks: [
    {
      id: '1',
      title: 'Song 1',
      artist: 'Artist 1',
      isPlayable: true
    },
    {
      id: '2',
      title: 'Song 2',
      artist: 'Artist 2',
      isPlayable: true
    }
  ]
});
```

### Esempio 2: Con Playlist

```typescript
await AndroidAuto.setMediaLibrary({
  recentTracks: [/* ... */],
  playlists: [
    {
      id: 'favorites',
      title: 'I Miei Preferiti',
      subtitle: '25 brani',
      artworkUrl: 'https://example.com/favorites.jpg',
      items: [
        {
          id: 'fav_1',
          title: 'Favorite Song 1',
          artist: 'Artist',
          isPlayable: true
        }
      ]
    },
    {
      id: 'workout',
      title: 'Workout Mix',
      subtitle: '30 brani',
      items: [/* ... */]
    }
  ]
});
```

### Esempio 3: Dati da API

```typescript
async function loadLibraryFromAPI() {
  // Carica dati dal tuo backend
  const response = await fetch('/api/music/library');
  const data = await response.json();
  
  // Trasforma nel formato richiesto
  const library = {
    recentTracks: data.recent.map(track => ({
      id: track.id,
      title: track.name,
      artist: track.artist_name,
      album: track.album_name,
      artworkUrl: track.cover_url,
      duration: track.duration_ms,
      isPlayable: true
    })),
    playlists: data.playlists.map(playlist => ({
      id: playlist.id,
      title: playlist.name,
      subtitle: `${playlist.tracks.length} brani`,
      items: playlist.tracks.map(track => ({
        id: track.id,
        title: track.name,
        artist: track.artist_name,
        isPlayable: true
      }))
    }))
  };
  
  // Imposta in Android Auto
  await AndroidAuto.setMediaLibrary(library);
}
```

### Esempio 4: Aggiornamento Dinamico

```typescript
class MusicLibraryManager {
  async addToRecent(track) {
    // Aggiungi ai recenti
    this.recentTracks.unshift(track);
    
    // Mantieni solo le ultime 20
    if (this.recentTracks.length > 20) {
      this.recentTracks = this.recentTracks.slice(0, 20);
    }
    
    // Aggiorna Android Auto
    await AndroidAuto.setMediaLibrary({
      recentTracks: this.recentTracks,
      playlists: this.playlists
    });
  }
  
  async createPlaylist(name, tracks) {
    this.playlists.push({
      id: `playlist_${Date.now()}`,
      title: name,
      subtitle: `${tracks.length} brani`,
      items: tracks
    });
    
    await AndroidAuto.setMediaLibrary({
      recentTracks: this.recentTracks,
      playlists: this.playlists
    });
  }
}
```

## 🎯 Best Practices

### 1. **Limita il Numero di Elementi**
```typescript
// ✅ Buono - 10-20 recenti
recentTracks: last20Tracks

// ❌ Evita - troppi elementi
recentTracks: allTracksEver // Migliaia di tracce
```

### 2. **Usa ID Univoci**
```typescript
// ✅ Buono
id: 'track_12345'
id: 'playlist_favorites'

// ❌ Evita
id: '1' // Troppo generico
```

### 3. **Fornisci Artwork**
```typescript
// ✅ Migliore esperienza
artworkUrl: 'https://cdn.example.com/covers/album123.jpg'

// ⚠️ Funziona ma meno bello
artworkUrl: '' // Nessuna copertina
```

### 4. **Aggiorna Regolarmente**
```typescript
// Aggiorna quando cambia qualcosa
async onTrackPlayed(track) {
  await this.addToRecent(track);
  await AndroidAuto.setMediaLibrary(this.library);
}
```

## 🔍 Debugging

### Verifica che la Libreria sia Caricata

```bash
# Controlla i log
adb logcat | grep "AndroidAutoService"

# Dovresti vedere:
# 📚 Impostazione libreria musicale
# ✅ Caricate X canzoni recenti
# ✅ Caricate X playlist
```

### Testa la Navigazione

```bash
# Quando navighi in Android Auto, dovresti vedere:
# 📂 onLoadChildren chiamato per: root
# 📋 Ritorno 4 categorie root
# 
# 📂 onLoadChildren chiamato per: recent
# 🕐 Ritorno 5 canzoni recenti
```

## ⚠️ Problemi Comuni

### "Nessun elemento" in Android Auto

**Causa:** Libreria non impostata o vuota

**Soluzione:**
```typescript
// Assicurati di chiamare setMediaLibrary
await AndroidAuto.setMediaLibrary({
  recentTracks: [/* almeno 1 elemento */]
});
```

### Elementi non cliccabili

**Causa:** `isPlayable: false` o mancante

**Soluzione:**
```typescript
{
  id: 'track_1',
  title: 'Song',
  isPlayable: true // ← Importante!
}
```

### Libreria non si aggiorna

**Causa:** Non richiami `setMediaLibrary` dopo le modifiche

**Soluzione:**
```typescript
// Dopo ogni modifica
this.library.recentTracks.push(newTrack);
await AndroidAuto.setMediaLibrary(this.library); // ← Richiama sempre
```

## 📚 Struttura Dati Completa

```typescript
interface MediaLibrary {
  recentTracks?: MediaItem[];
  playlists?: MediaCategory[];
  albums?: MediaCategory[];
  artists?: MediaCategory[];
}

interface MediaCategory {
  id: string;              // Univoco
  title: string;           // Nome visualizzato
  subtitle?: string;       // Info aggiuntive
  artworkUrl?: string;     // URL copertina
  items?: MediaItem[];     // Contenuti
}

interface MediaItem {
  id: string;              // Univoco
  title: string;           // Titolo canzone
  artist?: string;         // Nome artista
  album?: string;          // Nome album
  artworkUrl?: string;     // URL copertina
  duration?: number;       // Durata in ms
  isPlayable: boolean;     // DEVE essere true
}
```

## 🎉 Risultato Finale

Con questa configurazione, quando l'utente apre la tua app da Android Auto:

1. ✅ Vede le categorie (Recenti, Playlist, ecc.)
2. ✅ Può navigare tra le categorie
3. ✅ Può selezionare una canzone
4. ✅ La tua app riceve l'evento `mediaItemSelected`
5. ✅ Riproduci la canzone e aggiorna lo stato
6. ✅ L'utente vede i controlli e le info della canzone

**Nessuna schermata vuota!** 🎵
