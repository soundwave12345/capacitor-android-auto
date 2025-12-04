/**
 * Esempio completo di utilizzo del plugin con libreria musicale navigabile
 */

import { AndroidAuto } from 'capacitor-android-auto';
import type { PluginListenerHandle } from '@capacitor/core';

class MusicPlayerWithLibrary {
    private buttonListener?: PluginListenerHandle;
    private mediaItemListener?: PluginListenerHandle;
    private currentTrack = 0;
    private isPlaying = false;

    // Esempio di libreria musicale
    private library = {
        // Ultime canzoni ascoltate
        recentTracks: [
            {
                id: 'track_1',
                title: 'Bohemian Rhapsody',
                artist: 'Queen',
                album: 'A Night at the Opera',
                artworkUrl: 'https://example.com/queen.jpg',
                duration: 354000,
                isPlayable: true
            },
            {
                id: 'track_2',
                title: 'Stairway to Heaven',
                artist: 'Led Zeppelin',
                album: 'Led Zeppelin IV',
                artworkUrl: 'https://example.com/ledzeppelin.jpg',
                duration: 482000,
                isPlayable: true
            },
            {
                id: 'track_3',
                title: 'Hotel California',
                artist: 'Eagles',
                album: 'Hotel California',
                artworkUrl: 'https://example.com/eagles.jpg',
                duration: 391000,
                isPlayable: true
            }
        ],

        // Playlist
        playlists: [
            {
                id: 'playlist_rock',
                title: 'Rock Classics',
                subtitle: '50 brani',
                artworkUrl: 'https://example.com/rock.jpg',
                items: [
                    {
                        id: 'track_4',
                        title: 'Sweet Child O\' Mine',
                        artist: 'Guns N\' Roses',
                        album: 'Appetite for Destruction',
                        duration: 356000,
                        isPlayable: true
                    },
                    {
                        id: 'track_5',
                        title: 'Smoke on the Water',
                        artist: 'Deep Purple',
                        album: 'Machine Head',
                        duration: 340000,
                        isPlayable: true
                    }
                ]
            },
            {
                id: 'playlist_chill',
                title: 'Chill Vibes',
                subtitle: '30 brani',
                artworkUrl: 'https://example.com/chill.jpg',
                items: [
                    {
                        id: 'track_6',
                        title: 'Wonderwall',
                        artist: 'Oasis',
                        album: '(What\'s the Story) Morning Glory?',
                        duration: 258000,
                        isPlayable: true
                    }
                ]
            }
        ],

        // Album
        albums: [
            {
                id: 'album_queen',
                title: 'A Night at the Opera',
                subtitle: 'Queen',
                artworkUrl: 'https://example.com/queen-album.jpg',
                items: [
                    {
                        id: 'track_1',
                        title: 'Bohemian Rhapsody',
                        artist: 'Queen',
                        album: 'A Night at the Opera',
                        duration: 354000,
                        isPlayable: true
                    },
                    {
                        id: 'track_7',
                        title: 'You\'re My Best Friend',
                        artist: 'Queen',
                        album: 'A Night at the Opera',
                        duration: 172000,
                        isPlayable: true
                    }
                ]
            }
        ],

        // Artisti
        artists: [
            {
                id: 'artist_queen',
                title: 'Queen',
                subtitle: '20 brani',
                artworkUrl: 'https://example.com/queen-artist.jpg',
                items: [
                    {
                        id: 'track_1',
                        title: 'Bohemian Rhapsody',
                        artist: 'Queen',
                        duration: 354000,
                        isPlayable: true
                    },
                    {
                        id: 'track_8',
                        title: 'We Will Rock You',
                        artist: 'Queen',
                        duration: 122000,
                        isPlayable: true
                    }
                ]
            }
        ]
    };

    async initialize() {
        console.log('🎵 Inizializzazione Android Auto con libreria musicale');

        // 1. Registra listener per i controlli
        this.buttonListener = await AndroidAuto.addListener('buttonPressed', (event) => {
            console.log(`🎯 Button pressed: ${event.button}`);
            this.handleMediaControl(event.button);
        });

        // 2. Registra listener per selezione elementi dalla libreria
        this.mediaItemListener = await AndroidAuto.addListener('mediaItemSelected', (event) => {
            console.log(`🎵 Media item selected: ${event.mediaId}`);
            this.playMediaItem(event.mediaId);
        });

        // 3. Registra listener per ricerca vocale
        await AndroidAuto.addListener('searchRequest', (event) => {
            console.log(`🔍 Search request: ${event.query}`);
            this.handleSearch(event.query);
        });

        // 4. Imposta la libreria musicale
        await this.setLibrary();

        // 5. Mostra lo stato iniziale
        await this.updateAndroidAutoUI();

        console.log('✅ Android Auto inizializzato con libreria');
    }

    private handleSearch(query: string) {
        console.log(`🔍 Ricerca per: "${query}"`);
        const queryLower = query.toLowerCase();

        // Cerca in tutte le tracce
        let foundTrack = null;

        // Cerca nei recenti
        foundTrack = this.library.recentTracks.find(t =>
            t.title.toLowerCase().includes(queryLower) ||
            t.artist?.toLowerCase().includes(queryLower)
        );

        // Cerca nelle playlist
        if (!foundTrack) {
            for (const playlist of this.library.playlists) {
                foundTrack = playlist.items?.find(t =>
                    t.title.toLowerCase().includes(queryLower) ||
                    t.artist?.toLowerCase().includes(queryLower)
                );
                if (foundTrack) break;
            }
        }

        if (foundTrack) {
            console.log(`✅ Trovato: ${foundTrack.title}`);
            this.playMediaItem(foundTrack.id);
        } else {
            console.log('❌ Nessun risultato trovato');
        }
    }

    async setLibrary() {
        console.log('📚 Impostazione libreria musicale...');

        try {
            await AndroidAuto.setMediaLibrary(this.library);
            console.log('✅ Libreria musicale impostata');
        } catch (error) {
            console.error('❌ Errore impostazione libreria:', error);
        }
    }

    private playMediaItem(mediaId: string) {
        console.log(`▶️ Riproduzione elemento: ${mediaId}`);

        // Cerca l'elemento in tutte le categorie
        let track = null;

        // Cerca nei recenti
        track = this.library.recentTracks.find(t => t.id === mediaId);

        // Cerca nelle playlist
        if (!track) {
            for (const playlist of this.library.playlists) {
                track = playlist.items?.find(t => t.id === mediaId);
                if (track) break;
            }
        }

        // Cerca negli album
        if (!track) {
            for (const album of this.library.albums) {
                track = album.items?.find(t => t.id === mediaId);
                if (track) break;
            }
        }

        // Cerca negli artisti
        if (!track) {
            for (const artist of this.library.artists) {
                track = artist.items?.find(t => t.id === mediaId);
                if (track) break;
            }
        }

        if (track) {
            console.log(`🎵 Trovata traccia: ${track.title} - ${track.artist}`);

            // Avvia riproduzione
            this.isPlaying = true;

            // Aggiorna UI
            this.updateAndroidAutoUI(track);

            // Qui avvieresti la riproduzione effettiva
            // es. audioPlayer.play(track.url);
        } else {
            console.warn(`⚠️ Traccia non trovata: ${mediaId}`);
        }
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
    }

    async pause() {
        console.log('⏸️ Pause');
        this.isPlaying = false;
        await this.updateAndroidAutoUI();
    }

    async stop() {
        console.log('⏹️ Stop');
        this.isPlaying = false;
        await this.updateAndroidAutoUI();
    }

    async nextTrack() {
        console.log('⏭️ Next Track');
        this.currentTrack = (this.currentTrack + 1) % this.library.recentTracks.length;
        await this.updateAndroidAutoUI();
    }

    async previousTrack() {
        console.log('⏮️ Previous Track');
        this.currentTrack = this.currentTrack === 0
            ? this.library.recentTracks.length - 1
            : this.currentTrack - 1;
        await this.updateAndroidAutoUI();
    }

    private async updateAndroidAutoUI(customTrack?: any) {
        const track = customTrack || this.library.recentTracks[this.currentTrack];

        try {
            await AndroidAuto.updatePlayerState({
                title: track.title,
                artist: track.artist,
                album: track.album || '',
                artworkUrl: track.artworkUrl || '',
                isPlaying: this.isPlaying,
                duration: track.duration || 0,
                position: 0
            });

            console.log(`✅ UI aggiornata: ${track.title}`);
        } catch (error) {
            console.error('❌ Errore aggiornamento UI:', error);
        }
    }

    async updateLibraryDynamically() {
        // Esempio: aggiorna la libreria con nuovi dati
        // (es. dopo aver caricato dati dal server)

        console.log('🔄 Aggiornamento dinamico libreria...');

        // Aggiungi una nuova canzone ai recenti
        this.library.recentTracks.unshift({
            id: 'track_new',
            title: 'New Song',
            artist: 'New Artist',
            album: 'New Album',
            artworkUrl: 'https://example.com/new.jpg',
            duration: 200000,
            isPlayable: true
        });

        // Limita a 10 recenti
        if (this.library.recentTracks.length > 10) {
            this.library.recentTracks = this.library.recentTracks.slice(0, 10);
        }

        // Aggiorna la libreria
        await this.setLibrary();

        console.log('✅ Libreria aggiornata dinamicamente');
    }

    async cleanup() {
        console.log('🧹 Cleanup Android Auto');

        if (this.buttonListener) {
            await this.buttonListener.remove();
        }

        if (this.mediaItemListener) {
            await this.mediaItemListener.remove();
        }
    }
}

// ============================================
// ESEMPIO DI UTILIZZO
// ============================================

export async function initializeAndroidAutoWithLibrary() {
    const player = new MusicPlayerWithLibrary();

    // Inizializza
    await player.initialize();

    // Esempio: aggiorna libreria dopo 10 secondi
    setTimeout(async () => {
        await player.updateLibraryDynamically();
    }, 10000);

    // Cleanup quando l'app viene chiusa
    window.addEventListener('beforeunload', async () => {
        await player.cleanup();
    });

    return player;
}

// ============================================
// ESEMPIO CON DATI DA API
// ============================================

export async function initializeWithAPIData() {
    const player = new MusicPlayerWithLibrary();

    // Carica dati da API
    const libraryData = await fetch('/api/music/library').then(r => r.json());

    // Imposta libreria
    await AndroidAuto.setMediaLibrary(libraryData);

    // Inizializza listener
    await player.initialize();

    return player;
}

// Uso:
// const musicPlayer = await initializeAndroidAutoWithLibrary();
