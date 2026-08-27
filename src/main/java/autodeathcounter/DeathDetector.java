package autodeathcounter;

import java.util.function.Supplier;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.util.Duration;

/**
 * Detects Elden Ring's red "YOU DIED" screen without accessing the game process.
 *
 * The detector only captures a narrow region around the centre of the monitor and
 * looks for a wide, dark-red text-like pattern. Several consecutive matching
 * frames are required before a death event is emitted, and the detector stays
 * latched until the pattern disappears again.
 */
public final class DeathDetector {
    private static final Duration POLL_INTERVAL = Duration.millis(250);

    // Require a stable detection for ~750 ms to avoid reacting to single red flashes.
    private static final int REQUIRED_HIT_FRAMES = 3;
    private static final int REQUIRED_CLEAR_FRAMES = 8;
    private static final long MIN_LATCH_MILLIS = 12_000L;

    private static final int HORIZONTAL_BUCKETS = 12;

    private final Supplier<Screen> screenSupplier;
    private final Runnable onDeathDetected;

    private Robot robot;
    private Timeline timeline;

    private int hitFrames;
    private int clearFrames;
    private boolean latched;
    private long latchedAtMillis;

    public DeathDetector(Supplier<Screen> screenSupplier, Runnable onDeathDetected) {
        this.screenSupplier = screenSupplier;
        this.onDeathDetected = onDeathDetected;
    }

    public void start() {
        if (timeline != null) return;

        // JavaFX Robot must be created and used on the JavaFX Application Thread.
        robot = new Robot();
        timeline = new Timeline(new KeyFrame(POLL_INTERVAL, e -> sampleFrame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        robot = null;
        hitFrames = 0;
        clearFrames = 0;
        latched = false;
        latchedAtMillis = 0L;
    }

    private void sampleFrame() {
        try {
            Screen screen = screenSupplier.get();
            if (screen == null || robot == null) return;

            Rectangle2D bounds = screen.getBounds();

            // "YOU DIED" is displayed as a wide line around the centre of the screen.
            Rectangle2D region = new Rectangle2D(
                    bounds.getMinX() + bounds.getWidth() * 0.18,
                    bounds.getMinY() + bounds.getHeight() * 0.40,
                    bounds.getWidth() * 0.64,
                    bounds.getHeight() * 0.20);

            WritableImage capture = robot.getScreenCapture(null, region, false);
            boolean detected = looksLikeYouDied(capture);

            if (detected) {
                clearFrames = 0;

                if (!latched) {
                    hitFrames++;
                    if (hitFrames >= REQUIRED_HIT_FRAMES) {
                        latched = true;
                        latchedAtMillis = System.currentTimeMillis();
                        hitFrames = 0;
                        onDeathDetected.run();
                    }
                }
            } else {
                hitFrames = 0;

                if (latched) {
                    // The YOU DIED animation fades in and out. Without a minimum
                    // latch duration, a few clear frames during the fade can cause
                    // the same death to be emitted again.
                    if (System.currentTimeMillis() - latchedAtMillis >= MIN_LATCH_MILLIS) {
                        clearFrames++;
                        if (clearFrames >= REQUIRED_CLEAR_FRAMES) {
                            latched = false;
                            latchedAtMillis = 0L;
                            clearFrames = 0;
                        }
                    } else {
                        clearFrames = 0;
                    }
                }
            }
        } catch (Exception ex) {
            // Screen capture failure should never stop the DeathCounter itself.
            System.err.println("[DEATH DETECTOR] Screen capture failed: " + ex.getMessage());
        }
    }

    private boolean looksLikeYouDied(WritableImage image) {
        PixelReader pixels = image.getPixelReader();
        if (pixels == null) return false;

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        if (width <= 0 || height <= 0) return false;

        // Sampling rather than checking every pixel keeps the detector inexpensive.
        int step = Math.max(2, Math.min(width, height) / 120);
        int[] redPerBucket = new int[HORIZONTAL_BUCKETS];

        int samples = 0;
        int redSamples = 0;
        int darkSamples = 0;

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                Color color = pixels.getColor(x, y);
                double r = color.getRed();
                double g = color.getGreen();
                double b = color.getBlue();

                samples++;

                if (Math.max(r, Math.max(g, b)) < 0.32) {
                    darkSamples++;
                }

                // Elden Ring's YOU DIED lettering is a muted/dark red. These limits
                // intentionally accept a range of brightness values for different
                // monitors, HDR settings and post-processing configurations.
                boolean deathRed = r >= 0.20
                        && r > g * 1.35
                        && r > b * 1.25
                        && (r - g) >= 0.08
                        && (r - b) >= 0.06;

                if (deathRed) {
                    redSamples++;
                    int bucket = Math.min(
                            HORIZONTAL_BUCKETS - 1,
                            (int) ((long) x * HORIZONTAL_BUCKETS / Math.max(1, width)));
                    redPerBucket[bucket]++;
                }
            }
        }

        if (samples == 0) return false;

        double redRatio = (double) redSamples / samples;
        double darkRatio = (double) darkSamples / samples;

        int coveredBuckets = 0;
        int minimumRedSamplesPerBucket = Math.max(2, height / Math.max(1, step * 40));
        for (int count : redPerBucket) {
            if (count >= minimumRedSamplesPerBucket) {
                coveredBuckets++;
            }
        }

        // A death screen should contain a broad red pattern over a substantially
        // darkened centre region. The horizontal coverage rejects isolated blood,
        // enemy effects and small red HUD elements.
        return redRatio >= 0.008
                && darkRatio >= 0.28
                && coveredBuckets >= 6;
    }
}
