package player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import source.AudioSource;
import async.Async;

import javax.sound.sampled.*;
import java.io.IOException;

public class AudioStreamer extends AbstractLinePlayer implements Runnable {

    public static final String TAG = "AudioStreamer";

    private static final int DEFAULT_BUFFER_SIZE = 102400;      // 100 kb

    private static int bufferSize(@Nullable AudioFormat format) {
        if (format == null)
            return DEFAULT_BUFFER_SIZE;

        final float secs = 0.5f;

        if (format.getSampleRate() != AudioSystem.NOT_SPECIFIED) {
            int sampleBits = format.getSampleSizeInBits();
            if (sampleBits == AudioSystem.NOT_SPECIFIED) {
                sampleBits = 16;        // 2 bytes
            }

            return ((int) (format.getSampleRate() * (sampleBits / 8f) * secs));
        }

        if (format.getFrameRate() != AudioSystem.NOT_SPECIFIED && format.getFrameSize() != AudioSystem.NOT_SPECIFIED) {
            return ((int) (format.getFrameRate() * format.getFrameSize() * secs));
        }

        return DEFAULT_BUFFER_SIZE;
    }


//    /**
//     * Creates Sound player for the given URL
//     *
//     * @param id id of the player
//     * @param url url of the audio file
//     *
//     * @return Sound player for the given URL
//     * @throws CreationException if there is an error creating the sound player. Use its {@link CreationException#getCause() cause}
//     * to get actual exception
//     * */
//    @NotNull
//    public static AudioStreamer create(long id, @NotNull URL url) throws CreationException {
//
//    }
//
//    @Nullable
//    public static AudioStreamer createNoThrow(long id, @NotNull URL url) {
//        try {
//            return create(id, url);
//        } catch (CreationException e) {
//            Log.e(TAG, e.getMessage(), e.getCause());
//        }
//
//        return null;
//    }



    @NotNull
    private final AudioSource source;

    @Nullable
    private volatile AudioInputStream mStream;
    @Nullable
    private volatile SourceDataLine mLine;

    private volatile boolean run;
    private volatile byte[] buff;

    public AudioStreamer(long id, @NotNull AudioSource source) {
        super(id);
        this.source = source;
    }

    @Override
    public @NotNull String logTag() {
        return TAG;
    }

    @NotNull
    @Override
    public AudioSource getSource() {
        return source;
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    protected @Nullable DataLine getLine() {
        return mLine;
    }

    @Override
    protected @Nullable AudioFormat getFormat() {
        final AudioInputStream stream = mStream;
        if (stream != null) {
            return stream.getFormat();
        }

        return null;
    }

    public long getFrameLength() {
        final AudioInputStream stream = mStream;
        if (stream != null) {
            return stream.getFrameLength();
        }

        return AudioSystem.NOT_SPECIFIED;
    }



    private byte @NotNull[] createBuffer() {
        final SourceDataLine _line = mLine;
        return new byte[_line != null? _line.getBufferSize(): bufferSize(getFormat())];
    }

    private byte[] ensureBuffer() {
        byte[] b = buff;
        if (b == null) {
            synchronized (this) {
                b = buff;
                if (b == null) {
                    b = createBuffer();
                    buff = b;
                }
            }
        }

        return b;
    }

    @Override
    public void run() {
        final AudioInputStream stream = mStream;
        final SourceDataLine line = mLine;
        if (stream == null || line == null)
            return;

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
                onError(new PlayerException("failed to write audio data to stream line", e));
                break;
            }
        }
    }

    protected synchronized void onStateChanged(@NotNull State old, @NotNull State newState) {
        if (newState == State.PAUSED || newState == State.STOPPED || newState == State.ENDED || newState == State.ERROR || newState == State.CLOSED) {
            run = false;

            if (newState == State.STOPPED) {
                // todo reset input stream to start if seekable
            }
        }
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

    public synchronized boolean considerOpen() {
        if (isOpen())
            return true;

        try {
            final AudioInputStream stream = AudioPlayer.openAudioInputStream(source);
            mStream = stream;

            final AudioFormat format = stream.getFormat();

            // Detach
            final SourceDataLine oldLine = mLine;
            if (oldLine != null) {
                oldLine.removeLineListener(this);
                mLine = null;
            }

            final SourceDataLine newLine = AudioSystem.getSourceDataLine(format);
            mLine = newLine;

            ensureBuffer();
            newLine.addLineListener(this);
            newLine.open(format, bufferSize(format));
            return true;
        } catch (PlayerException e) {
            onError(e);
        } catch (LineUnavailableException e) {
            onError(new PlayerException("Audio data line could not be initialised due to system restrictions\nAudio Source: " + source, e));
        } catch (IllegalArgumentException e) {
            onError(new PlayerException("No installed mixer supports sound clip\nAudio Source: " + source, e));
        } catch (Throwable t) {
            onError(new PlayerException("Unknown error in initialising Audio Player\nAudio Source: " + source, t));
        }

        return false;
    }

    private synchronized boolean considerRun() {
        if (run) {
            return true;
        }

        if (mStream == null || mLine == null)
            return false;

        run = true;
        Async.execute(this);
        return true;
    }

    @Override
    public synchronized void play() {
        if (isPlaying())
            return;

        Async.execute(() -> {
            if (!considerOpen())
                return;

            final SourceDataLine line = mLine;
            if (line == null)
                return;

            try {
                if (considerRun()) {
                    line.start();
                }
            } catch (Throwable t) {
                onError(new PlayerException("Failed to play audio stream", t));
            }
        });
    }


    @Override
    protected synchronized void doClose() throws Exception {
        final SourceDataLine line = mLine;
        if (line != null) {
            line.close();
            mLine = null;
        }

        final AudioInputStream stream = mStream;
        if (stream != null) {
            stream.close();
            mStream = null;
        }
    }
}
