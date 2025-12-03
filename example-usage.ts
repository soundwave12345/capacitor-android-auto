/**
 * Esempio di utilizzo del plugin Capacitor Android Auto
 * con MediaBrowserService per app musicali
 */

import { AndroidAuto } from 'capacitor-android-auto';
import type { PluginListenerHandle } from '@capacitor/core';

class MusicPlayerController {
    private buttonListener?: PluginListenerHandle;
    private currentTrack = 0;
    private isPlaying = false;

    private playlist = [
        {
            title: 'Bohemian Rhapsody',
            artist: 'Queen',
            album: 'A Night at the Opera',
            artworkUrl: 'https://example.com/queen.jpg',
            duration: 354000
        },
        {
            title: 'Stairway to Heaven',
            artist: 'Led Zeppelin',
            album: 'Led Zeppelin IV',
            artworkUrl: 'https://example.com/ledzeppelin.jpg',
            duration: 482000
        },
        {
            title: 'Hotel California',
            artist: 'Eagles',
            album: 'Hotel California',
            artworkUrl: 'https://example.com/eagles.jpg',
            duration: 391000
        }
    ];

    async initialize() {
        console.log('🎵 Inizializzazione Android Auto Music Player');

        // Registra listener per i controlli
        this.buttonListener = await AndroidAuto.addListener('buttonPressed', (event) => {
            console.log(`🎯 Button pressed: ${event.button} at ${new Date(event.timestamp)}`);
            this.handleMediaControl(event.button);
        });

        // Mostra lo stato iniziale
        await this.updateAndroidAutoUI();

        console.log('✅ Android Auto inizializzato');
    }

    private handleMediaControl(button: string) {
        switch (button) {
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

    async play() {
        console.log('▶️ Play');
        this.isPlaying = true;
        await this.updateAndroidAutoUI();

        // Qui avvieresti la riproduzione effettiva
        // es. audioElement.play()
    }

    async pause() {
        console.log('⏸️ Pause');
        this.isPlaying = false;
        await this.updateAndroidAutoUI();

        // Qui metteresti in pausa la riproduzione
        // es. audioElement.pause()
    }

    async stop() {
        console.log('⏹️ Stop');
        this.isPlaying = false;
        this.currentTrack = 0;
        await this.updateAndroidAutoUI();

        // Qui fermeresti la riproduzione
        // es. audioElement.pause(); audioElement.currentTime = 0;
    }

    async nextTrack() {
        console.log('⏭️ Next Track');
        this.currentTrack = (this.currentTrack + 1) % this.playlist.length;
        await this.updateAndroidAutoUI();

        // Qui cambieresti traccia
        // es. loadAndPlayTrack(this.playlist[this.currentTrack])
    }

    async previousTrack() {
        console.log('⏮️ Previous Track');
        this.currentTrack = this.currentTrack === 0
            ? this.playlist.length - 1
            : this.currentTrack - 1;
        await this.updateAndroidAutoUI();

        // Qui cambieresti traccia
        // es. loadAndPlayTrack(this.playlist[this.currentTrack])
    }

    private async updateAndroidAutoUI() {
        const track = this.playlist[this.currentTrack];

        try {
            await AndroidAuto.updatePlayerState({
                title: track.title,
                artist: track.artist,
                album: track.album,
                artworkUrl: track.artworkUrl,
                isPlaying: this.isPlaying,
                duration: track.duration,
                position: 0 // Qui dovresti passare la posizione reale
            });

            console.log(`✅ UI aggiornata: ${track.title} - ${this.isPlaying ? 'Playing' : 'Paused'}`);
        } catch (error) {
            console.error('❌ Errore aggiornamento UI:', error);
        }
    }

    async cleanup() {
        console.log('🧹 Cleanup Android Auto');

        // Rimuovi listener
        if (this.buttonListener) {
            await this.buttonListener.remove();
        }

        // Ferma il servizio se necessario
        try {
            await AndroidAuto.stopService();
        } catch (error) {
            console.error('Errore stop service:', error);
        }
    }
}

// Esempio di utilizzo in un'app Capacitor
export async function initializeAndroidAutoForMusicApp() {
    const player = new MusicPlayerController();

    // Inizializza quando l'app è pronta
    await player.initialize();

    // Cleanup quando l'app viene chiusa
    window.addEventListener('beforeunload', async () => {
        await player.cleanup();
    });

    return player;
}

// Uso:
// const musicPlayer = await initializeAndroidAutoForMusicApp();
// musicPlayer.play();
