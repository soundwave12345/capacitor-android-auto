import type { PluginListenerHandle } from '@capacitor/core';

export interface AndroidAutoPlugin {
  /**
   * Aggiorna lo stato del player musicale
   */
  updatePlayerState(options: PlayerState): Promise<void>;

  /**
   * Imposta la libreria musicale (playlist, canzoni recenti, ecc.)
   */
  setMediaLibrary(options: MediaLibrary): Promise<void>;

  /**
   * Avvia il servizio Android Auto
   */
  startService(): Promise<void>;

  /**
   * Ferma il servizio Android Auto
   */
  stopService(): Promise<void>;

  /**
   * Aggiungi listener per eventi UI
   */
  addListener(
    eventName: 'buttonPressed',
    listenerFunc: (event: ButtonPressedEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Aggiungi listener per quando l'utente seleziona un elemento dalla libreria
   */
  addListener(
    eventName: 'mediaItemSelected',
    listenerFunc: (event: MediaItemSelectedEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Aggiungi listener per richieste di ricerca vocale (es. "Suona Queen")
   */
  addListener(
    eventName: 'searchRequest',
    listenerFunc: (event: SearchRequestEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Rimuovi tutti i listener
   */
  removeAllListeners(): Promise<void>;
}

export interface PlayerState {
  title: string;
  artist: string;
  album?: string;
  artworkUrl?: string;
  isPlaying: boolean;
  duration?: number;
  position?: number;
}

export interface MediaLibrary {
  recentTracks?: MediaItem[];
  playlists?: MediaCategory[];
  albums?: MediaCategory[];
  artists?: MediaCategory[];
}

export interface MediaCategory {
  id: string;
  title: string;
  subtitle?: string;
  artworkUrl?: string;
  items?: MediaItem[];
}

export interface MediaItem {
  id: string;
  title: string;
  artist?: string;
  album?: string;
  artworkUrl?: string;
  duration?: number;
  isPlayable: boolean;
}

export interface ButtonPressedEvent {
  button: 'play' | 'pause' | 'next' | 'previous' | 'stop';
  timestamp: number;
}

export interface MediaItemSelectedEvent {
  mediaId: string;
  timestamp: number;
}

export interface SearchRequestEvent {
  query: string;
  timestamp: number;
}
