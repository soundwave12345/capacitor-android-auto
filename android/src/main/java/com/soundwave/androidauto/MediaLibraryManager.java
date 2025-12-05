package com.soundwave.androidauto;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaLibraryManager {
    private static final String TAG = "MediaLibraryManager";

    // Media IDs
    public static final String MEDIA_ROOT_ID = "root";
    public static final String MEDIA_RECENT_ID = "recent";
    public static final String MEDIA_PLAYLISTS_ID = "playlists";
    public static final String MEDIA_ALBUMS_ID = "albums";
    public static final String MEDIA_ARTISTS_ID = "artists";

    // Liste per la navigazione
    private final List<MediaBrowserCompat.MediaItem> recentTracks = new ArrayList<>();
    private final List<MediaBrowserCompat.MediaItem> playlistList = new ArrayList<>();
    private final List<MediaBrowserCompat.MediaItem> albumList = new ArrayList<>();
    private final List<MediaBrowserCompat.MediaItem> artistList = new ArrayList<>();
    private final List<MediaBrowserCompat.MediaItem> rootCategories = new ArrayList<>();

    // Mappe ID -> Contenuti
    private final Map<String, List<MediaBrowserCompat.MediaItem>> playlistContents = new HashMap<>();
    private final Map<String, List<MediaBrowserCompat.MediaItem>> albumContents = new HashMap<>();
    private final Map<String, List<MediaBrowserCompat.MediaItem>> artistContents = new HashMap<>();

    // Costanti per lo stile di visualizzazione (Grid vs List)
    public static final String CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT";
    public static final String CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT";
    public static final int CONTENT_STYLE_LIST_ITEM_HINT_VALUE = 1;
    public static final int CONTENT_STYLE_GRID_ITEM_HINT_VALUE = 2;

    public MediaLibraryManager() {
        initializeDefaultCategories();
    }

    private void initializeDefaultCategories() {
        rootCategories.clear();
        
        // Recenti: Griglia di canzoni
        addRootCategory(MEDIA_RECENT_ID, "Recenti", "Ultime canzoni ascoltate", 
            CONTENT_STYLE_GRID_ITEM_HINT_VALUE, CONTENT_STYLE_GRID_ITEM_HINT_VALUE);
            
        // Playlist: Griglia di playlist
        addRootCategory(MEDIA_PLAYLISTS_ID, "Playlist", "Le tue playlist", 
            CONTENT_STYLE_GRID_ITEM_HINT_VALUE, CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
            
        // Album: Griglia di album
        addRootCategory(MEDIA_ALBUMS_ID, "Album", "Tutti gli album", 
            CONTENT_STYLE_GRID_ITEM_HINT_VALUE, CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
            
        // Artisti: Griglia di artisti
        addRootCategory(MEDIA_ARTISTS_ID, "Artisti", "Tutti gli artisti", 
            CONTENT_STYLE_GRID_ITEM_HINT_VALUE, CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
    }

    private void addRootCategory(String id, String title, String subtitle, int browsableHint, int playableHint) {
        Bundle extras = new Bundle();
        extras.putInt(CONTENT_STYLE_BROWSABLE_HINT, browsableHint);
        extras.putInt(CONTENT_STYLE_PLAYABLE_HINT, playableHint);
        
        MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                .setMediaId(id)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setExtras(extras)
                .build();
        rootCategories.add(new MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
    }

    public List<MediaBrowserCompat.MediaItem> getRootItems() {
        return rootCategories;
    }

    public List<MediaBrowserCompat.MediaItem> getRecentTracks() {
        return recentTracks;
    }

    public List<MediaBrowserCompat.MediaItem> getPlaylists() {
        return playlistList;
    }

    public List<MediaBrowserCompat.MediaItem> getAlbums() {
        return albumList;
    }

    public List<MediaBrowserCompat.MediaItem> getArtists() {
        return artistList;
    }

    public List<MediaBrowserCompat.MediaItem> getPlaylistItems(String playlistId) {
        return playlistContents.get(playlistId);
    }

    public List<MediaBrowserCompat.MediaItem> getAlbumItems(String albumId) {
        return albumContents.get(albumId);
    }

    public List<MediaBrowserCompat.MediaItem> getArtistItems(String artistId) {
        return artistContents.get(artistId);
    }

    public void parseLibrary(String jsonLibrary) {
        try {
            JSONObject library = new JSONObject(jsonLibrary);

            // Recenti
            if (library.has("recentTracks")) {
                parseTracks(library.getJSONArray("recentTracks"), recentTracks);
            }

            // Playlist
            if (library.has("playlists")) {
                parseCategories(library.getJSONArray("playlists"), playlistList, playlistContents, "playlist_");
            }

            // Album
            if (library.has("albums")) {
                parseCategories(library.getJSONArray("albums"), albumList, albumContents, "album_");
            }

            // Artisti
            if (library.has("artists")) {
                parseCategories(library.getJSONArray("artists"), artistList, artistContents, "artist_");
            }

        } catch (JSONException e) {
            Log.e(TAG, "❌ Errore parsing libreria musicale", e);
        }
    }

    private void parseTracks(JSONArray jsonArray, List<MediaBrowserCompat.MediaItem> targetList) throws JSONException {
        targetList.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            targetList.add(createMediaItem(jsonArray.getJSONObject(i), true));
        }
    }

    private void parseCategories(JSONArray jsonArray, 
                               List<MediaBrowserCompat.MediaItem> categoryList,
                               Map<String, List<MediaBrowserCompat.MediaItem>> contentMap,
                               String idPrefix) throws JSONException {
        categoryList.clear();
        contentMap.clear();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            String id = json.getString("id");
            String title = json.getString("title");
            String subtitle = json.optString("subtitle", "");
            String artworkUrl = json.optString("artworkUrl", "");

            List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
            
            // Aggiungi pulsante Shuffle come primo elemento
            MediaBrowserCompat.MediaItem shuffleButton = createShuffleButton(idPrefix + id, artworkUrl);
            items.add(shuffleButton);
            
            // Aggiungi le canzoni con artwork
            if (json.has("items")) {
                JSONArray itemsArray = json.getJSONArray("items");
                for (int j = 0; j < itemsArray.length(); j++) {
                    items.add(createMediaItem(itemsArray.getJSONObject(j), true));
                }
            }

            // Salva contenuti (con shuffle button incluso)
            contentMap.put(id, items);

            // Imposta hint per visualizzare i contenuti come LISTA
            Bundle extras = new Bundle();
            extras.putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST_ITEM_HINT_VALUE);

            // Crea elemento navigabile (es. la card della Playlist)
            MediaDescriptionCompat.Builder descBuilder = new MediaDescriptionCompat.Builder()
                    .setMediaId(idPrefix + id)
                    .setTitle(title)
                    .setSubtitle(subtitle.isEmpty() ? (items.size() - 1) + " brani" : subtitle)
                    .setExtras(extras);
            
            if (!artworkUrl.isEmpty()) {
                descBuilder.setIconUri(android.net.Uri.parse(artworkUrl));
            }

            categoryList.add(new MediaBrowserCompat.MediaItem(descBuilder.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        }
    }

    private MediaBrowserCompat.MediaItem createShuffleButton(String parentId, String artworkUrl) {
        // Crea un ID speciale per il pulsante shuffle
        String shuffleId = "shuffle_" + parentId;
        
        MediaDescriptionCompat.Builder descBuilder = new MediaDescriptionCompat.Builder()
                .setMediaId(shuffleId)
                .setTitle("🔀 Riproduci in modo casuale")
                .setSubtitle("Shuffle");
        
        // Usa l'artwork della playlist/album se disponibile
        if (!artworkUrl.isEmpty()) {
            descBuilder.setIconUri(android.net.Uri.parse(artworkUrl));
        }
        
        // Rendi il pulsante PLAYABLE (non browsable)
        return new MediaBrowserCompat.MediaItem(
            descBuilder.build(), 
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        );
    }

    private MediaBrowserCompat.MediaItem createMediaItem(JSONObject json, boolean isPlayable) throws JSONException {
        String id = json.getString("id");
        String title = json.getString("title");
        String artist = json.optString("artist", "");
        String album = json.optString("album", "");
        String artworkUrl = json.optString("artworkUrl", "");

        MediaDescriptionCompat.Builder descBuilder = new MediaDescriptionCompat.Builder()
                .setMediaId(id)
                .setTitle(title)
                .setSubtitle(artist); // Sottotitolo: Artista

        // Se c'è l'album, lo mettiamo nella descrizione (potrebbe apparire come terza riga)
        if (!album.isEmpty()) {
            descBuilder.setDescription(album);
        }
        
        // Imposta Artwork URL
        if (!artworkUrl.isEmpty()) {
            descBuilder.setIconUri(android.net.Uri.parse(artworkUrl));
        }

        int flags = isPlayable ?
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE :
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE;

        return new MediaBrowserCompat.MediaItem(descBuilder.build(), flags);
    }

    public List<MediaBrowserCompat.MediaItem> search(String query) {
        List<MediaBrowserCompat.MediaItem> results = new ArrayList<>();
        String queryLower = query.toLowerCase();

        searchInList(recentTracks, queryLower, results);
        searchInMap(playlistContents, queryLower, results);
        searchInMap(albumContents, queryLower, results);
        searchInMap(artistContents, queryLower, results);

        return results;
    }

    private void searchInList(List<MediaBrowserCompat.MediaItem> list, String query, List<MediaBrowserCompat.MediaItem> results) {
        for (MediaBrowserCompat.MediaItem item : list) {
            if (matchesQuery(item, query)) results.add(item);
        }
    }

    private void searchInMap(Map<String, List<MediaBrowserCompat.MediaItem>> map, String query, List<MediaBrowserCompat.MediaItem> results) {
        for (List<MediaBrowserCompat.MediaItem> list : map.values()) {
            searchInList(list, query, results);
        }
    }

    private boolean matchesQuery(MediaBrowserCompat.MediaItem item, String query) {
        CharSequence title = item.getDescription().getTitle();
        CharSequence subtitle = item.getDescription().getSubtitle();

        return (title != null && title.toString().toLowerCase().contains(query)) ||
               (subtitle != null && subtitle.toString().toLowerCase().contains(query));
    }
}
