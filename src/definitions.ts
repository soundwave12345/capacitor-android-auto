import type { PluginListenerHandle } from '@capacitor/core';

export interface AndroidAutoPlugin {
  /**
   * Aggiorna lo stato del player musicale
   */
  updatePlayerState(options: PlayerState): Promise<void>;

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
   * Rimuovi tutti i listener
   */
  removeAllListeners(): Promise<void>;
}

export interface PlayerState {
  title: string;
  artist: string;
  album?: string;
  isPlaying: boolean;
  duration?: number;
  position?: number;
}

export interface ButtonPressedEvent {
  button: 'play' | 'pause' | 'next' | 'previous' | 'stop';
  timestamp: number;
}
