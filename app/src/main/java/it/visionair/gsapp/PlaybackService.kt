package it.visionair.gsapp

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Servizio media in foreground. Tiene la radio in riproduzione
 * anche con app in background o schermo spento, e si integra con:
 * - Notifica media nella status bar
 * - Lockscreen
 * - Android Auto
 * - Auricolari Bluetooth (controlli hardware)
 */
@UnstableApi
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        const val STREAM_URL = "https://visionair.ddns.net/stream/main"
        const val STATION_NAME = "Visionair Golden Stream"
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build().apply {
            // Live streaming: niente seek, niente loop
            repeatMode = ExoPlayer.REPEAT_MODE_OFF
        }

        val mediaItem = MediaItem.Builder()
            .setMediaId("visionair_main")
            .setUri(STREAM_URL)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(STATION_NAME)
                    .setArtist("Radio in diretta")
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_RADIO_STATION)
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    /**
     * Quando l'utente rimuove l'app dai recenti, fermiamo il servizio
     * (evita riproduzione fantasma).
     */
    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val player = mediaSession?.player
        if (player != null && (!player.playWhenReady || player.mediaItemCount == 0)) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
