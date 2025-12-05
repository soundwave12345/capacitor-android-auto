# Modifiche alla Visualizzazione di Playlist e Album

## Modifiche Implementate

### 1. **Artwork per Ogni Canzone** ✅
Ogni canzone nella lista di una playlist o album ora mostra l'artwork a sinistra della riga. Questo era già implementato tramite `setIconUri()` nel metodo `createMediaItem()`, che imposta l'immagine per ogni brano.

### 2. **Pulsante Shuffle/Random** ✅
Aggiunto un pulsante "🔀 Riproduci in modo casuale" come **prima riga** in ogni playlist e album.

#### Caratteristiche del Pulsante Shuffle:
- **Posizione**: Prima riga della lista
- **Titolo**: "🔀 Riproduci in modo casuale"
- **Sottotitolo**: "Shuffle"
- **Artwork**: Usa l'artwork della playlist/album
- **Media ID**: `shuffle_playlist_[id]` o `shuffle_album_[id]`
- **Tipo**: PLAYABLE (può essere selezionato per avviare la riproduzione)

## Come Funziona

### Struttura della Lista
Quando apri una playlist o un album, vedrai:

```
┌─────────────────────────────────────┐
│ 🔀 Riproduci in modo casuale        │ ← Pulsante Shuffle
│ [Artwork] Shuffle                   │
├─────────────────────────────────────┤
│ [Artwork] Canzone 1                 │ ← Canzoni con artwork
│           Artista 1                 │
├─────────────────────────────────────┤
│ [Artwork] Canzone 2                 │
│           Artista 2                 │
├─────────────────────────────────────┤
│ ...                                 │
└─────────────────────────────────────┘
```

### Gestione del Click sul Pulsante Shuffle

Quando l'utente seleziona il pulsante shuffle, il sistema chiamerà `onPlayFromMediaId()` con un media ID che inizia con `"shuffle_"`.

**Esempio di Media ID**:
- `shuffle_playlist_my_favorites`
- `shuffle_album_greatest_hits`

### Implementazione Lato App

Nel tuo codice TypeScript/JavaScript, dovrai gestire questo evento:

```typescript
// Ascolta gli eventi di selezione media
AndroidAuto.addListener('mediaItemSelected', (event) => {
  const mediaId = event.mediaId;
  
  if (mediaId.startsWith('shuffle_playlist_')) {
    // Estrai l'ID della playlist
    const playlistId = mediaId.replace('shuffle_playlist_', '');
    
    // Avvia riproduzione random della playlist
    playRandomFromPlaylist(playlistId);
    
  } else if (mediaId.startsWith('shuffle_album_')) {
    // Estrai l'ID dell'album
    const albumId = mediaId.replace('shuffle_album_', '');
    
    // Avvia riproduzione random dell'album
    playRandomFromAlbum(albumId);
    
  } else {
    // Gestione normale: riproduci la canzone specifica
    playSong(mediaId);
  }
});
```

## Codice Modificato

### File: `MediaLibraryManager.java`

#### 1. Metodo `parseCategories()` (linee 151-199)
- Aggiunto il pulsante shuffle come primo elemento della lista
- Corretto il conteggio dei brani (escluso il pulsante shuffle)

#### 2. Nuovo Metodo `createShuffleButton()` (linee 201-220)
- Crea il pulsante shuffle con l'artwork della playlist/album
- Imposta il media ID con prefisso `shuffle_`
- Rende il pulsante PLAYABLE

#### 3. Metodo `createMediaItem()` (linee 222-248)
- Già esistente, imposta l'artwork per ogni canzone tramite `setIconUri()`

## Visualizzazione

### Android Auto mostrerà:
- **Lista verticale** con artwork a sinistra di ogni riga
- **Prima riga**: Pulsante shuffle con icona 🔀
- **Righe successive**: Canzoni con il loro artwork individuale

### Ogni riga canzone mostra:
- **Artwork** (a sinistra)
- **Titolo** della canzone
- **Artista** (sottotitolo)

## Note Tecniche

1. **Content Style**: Impostato su `CONTENT_STYLE_LIST_ITEM_HINT_VALUE` per visualizzazione a lista
2. **Artwork**: Caricato tramite `setIconUri()` per ogni elemento
3. **Flag**: Il pulsante shuffle usa `FLAG_PLAYABLE`, le canzoni usano `FLAG_PLAYABLE`
4. **Compatibilità**: Funziona con Android Auto e Android Automotive OS

## Testing

Per testare:
1. Compila e installa l'app
2. Connetti ad Android Auto
3. Naviga in una playlist o album
4. Verifica che:
   - Il pulsante shuffle appaia come prima riga
   - Ogni canzone mostri il suo artwork
   - Cliccando sul pulsante shuffle venga chiamato il listener con il media ID corretto
