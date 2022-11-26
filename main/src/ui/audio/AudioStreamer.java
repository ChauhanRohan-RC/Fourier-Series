package ui.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Log;
import util.async.Async;
import util.live.Listeners;
import util.live.ListenersI;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;


public class AudioStreamer extends AbstractLinePlayer implements Runnable {

    public static final String TAG = "AudioStreamer";

    private static int bufferSize(@NotNull AudioFormat format) {
        return ((int) (format.getFrameRate() / 2)) * format.getFrameSize();     // 0.5 seconds worth of data
    }

    /**
     * Creates Sound player for the given URL
     *
     * @param id id of the player
     * @param url url of the audio file
     *
     * @return Sound player for the given URL
     * @throws CreationException if there is an error creating the sound player. Use its {@link CreationException#getCause() cause}
     * to get actual exception
     * */
    @NotNull
    public static AudioStreamer create(long id, @NotNull URL url) throws CreationException {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(url);
            final AudioFormat baseFormat = stream.getFormat();
            final AudioFormat newFormat = AudioPlayer.transformAudioFormat(baseFormat);
            if (newFormat != baseFormat) {
                stream = AudioSystem.getAudioInputStream(newFormat, stream);
            }

            final SourceDataLine clip = AudioSystem.getSourceDataLine(newFormat);

            return new AudioStreamer(id, stream, clip);
        } catch (UnsupportedAudioFileException e) {
            throw new CreationException("Unsupported Audio File format\nURL: " + url, e);
        } catch (LineUnavailableException e) {
            throw new CreationException("Sound clip could not be initialised due to system restrictions\nURL: " + url, e);
        } catch (IOException e) {
            throw new CreationException("I/O error in initialising Audio Stream\nURL: " + url, e);
        } catch (IllegalArgumentException e) {
            throw new CreationException("No installed mixer supports sound clip\nURL: " + url, e);
        } catch (Throwable t) {
            throw new CreationException("Unknown error in initialising Audio Clip Player\nURL: " + url, t);
        }
    }

    @Nullable
    public static AudioStreamer createNoThrow(long id, @NotNull URL url) {
        try {
            return create(id, url);
        } catch (CreationException e) {
            Log.e(TAG, e.getMessage(), e.getCause());
        }

        return null;
    }



    @NotNull
    private final AudioInputStream stream;
    @NotNull
    private final SourceDataLine line;

    private volatile boolean run;
    private volatile byte[] buff;

    private AudioStreamer(long id, @NotNull AudioInputStream stream, @NotNull SourceDataLine line) {
        super(id);
        this.stream = stream;
        this.line = line;

        this.line.addLineListener(this);
    }

    private byte[] ensureBuffer() {
        byte[] b = buff;
        if (b == null) {
            synchronized (this) {
                b = buff;
                if (b == null) {
                    b = new byte[isOpen()? line.getBufferSize(): bufferSize(getFormat())];
                    buff = b;
                }
            }
        }

        return b;
    }

    @Override
    public void run() {
        final byte[] buffer = ensureBuffer();

        while (run) {
            try {
                final int read = stream.read(buffer);
                if (read != -1) {
                    line.write(buffer, 0, read);
                } else {
                    end();
                    break;
                }
            } catch (IOException e) {
                Log.e(TAG, "failed to write data to line", e);
                stop();
                break;
            }
        }
    }

    protected synchronized void onStateChanged(@NotNull State old, @NotNull State newState) {
        if (newState == State.PAUSED || newState == State.STOPPED || newState == State.ENDED || newState == State.CLOSED) {
            run = false;

            if (newState == State.STOPPED) {
                // todo reset input stream
            }
        }
    }


    @Override
    protected @NotNull DataLine getLine() {
        return line;
    }

    @Override
    protected @NotNull AudioFormat getFormat() {
        return stream.getFormat();
    }

    public long getFrameLength() {
        return stream.getFrameLength();
    }


    @Override
    public boolean isSeekSupported() {
        return false;
    }

    @Override
    public void setFramePosition(int frames) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMicrosecondPosition(long microseconds) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isLoopSupported() {
        return false;
    }

    @Override
    public void setLoopPointFrames(int start, int end) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public synchronized void considerOpen() throws PlayerException {
        if (isOpen())
            return;

        try {
            final AudioFormat format = getFormat();
            line.open(format, bufferSize(format));
            ensureBuffer();
        } catch (Throwable t) {
            throw new PlayerException("Failed to open SourceDataLine", t);
        }
    }

    private void considerRun() {
        if (!run) {
            run = true;
            Async.execute(this);
        }
    }

    @Override
    public synchronized void play() throws PlayerException {
        if (isPlaying())
            return;

        considerOpen();
        considerRun();
        line.start();
    }

}
