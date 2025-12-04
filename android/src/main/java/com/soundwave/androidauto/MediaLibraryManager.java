package com.soundwave.androidauto;

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

    public MediaLibraryManager() {
        initializeDefaultCategories();
    }

    private void initializeDefaultCategories() {
        rootCategories.clear();
        addRootCategory(MEDIA_RECENT_ID, "Recenti", "Ultime canzoni ascoltate");
        addRootCategory(MEDIA_PLAYLISTS_ID, "Playlist", "Le tue playlist");
        addRootCategory(MEDIA_ALBUMS_ID, "Album", "Tutti gli album");
        addRootCategory(MEDIA_ARTISTS_ID, "Artisti", "Tutti gli artisti");
    }

    private void addRootCategory(String id, String title, String subtitle) {
        MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                .setMediaId(id)
                .setTitle(title)
                .setSubtitle(subtitle)
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

            List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
            if (json.has("items")) {
                JSONArray itemsArray = json.getJSONArray("items");
                for (int j = 0; j < itemsArray.length(); j++) {
                    items.add(createMediaItem(itemsArray.getJSONObject(j), true));
                }
            }

            // Salva contenuti
            contentMap.put(id, items);

            // Crea elemento navigabile
            MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                    .setMediaId(idPrefix + id)
                    .setTitle(title)
                    .setSubtitle(subtitle.isEmpty() ? items.size() + " brani" : subtitle)
                    .build();
            categoryList.add(new MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        }
    }

    private MediaBrowserCompat.MediaItem createMediaItem(JSONObject json, boolean isPlayable) throws JSONException {
        String id = json.getString("id");
        String title = json.getString("title");
        String artist = json.optString("artist", "");
        String album = json.optString("album", "");

        MediaDescriptionCompat.Builder descBuilder = new MediaDescriptionCompat.Builder()
                .setMediaId(id)
                .setTitle(title)
                .setSubtitle(artist);

        if (!album.isEmpty()) {
            descBuilder.setDescription(album);
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
