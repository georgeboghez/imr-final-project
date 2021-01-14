
package com.example.opencv10thtry;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.opencv10thtry.midi.InvalidMidiDataException;
import com.example.opencv10thtry.midi.MidiMessage;
import com.example.opencv10thtry.midi.MidiSystem;
import com.example.opencv10thtry.midi.Sequence;
import com.example.opencv10thtry.midi.ShortMessage;
import com.example.opencv10thtry.midi.Track;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    private static final String TAG = "MainActivity";
    long startingTime = -1;


    boolean colorsInverted = false;

    private MediaPlayer mp;
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final String[] NOTE_NAMES_WO_F_S = {"C", "D", "E", "F", "G", "A", "B"};
    private MediaPlayer mediapl = null;
    private final Map<String, String> map = Stream.of(new String[][]{
            {"C", "0"},
            {"D", "1"},
            {"E", "2"},
            {"F", "3"},
            {"G", "4"},
            {"A", "5"},
            {"B", "6"},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    //    private Map<String, Integer> NOTE_SOUNDS = new HashMap<>();
//    private Map<Integer, Integer> NOTE_SOUNDS_WO_F_S = new HashMap<>();
    private Map<Integer, MediaPlayer> mediaPlayerMap = new HashMap<>();
    private List<Integer> allPressedKeys = new ArrayList<>();

    private int score = 0;

    //    private final int duration = 3; // seconds
//    private final int sampleRate = 8000;
    private final int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private final int displayHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    //    private boolean isImageRead = false;
//    private Point matchLoc = new Point();
    ArrayList<MusicalNote> musicalNotes;
    long milisPassedFromSongStart;
    long milisSinceLastKeyWasPressed;

    Handler handler = new Handler();

    private AudioTrack generateTone(double freqHz, int durationMs) {
        int count = (int) (44100.0 * 2.0 * (durationMs / 1000.0)) & ~1;
        short[] samples = new short[count];
        for (int i = 0; i < count; i += 2) {
            short sample = (short) (Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF);
            samples[i] = sample;
            samples[i + 1] = sample;
        }
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
        track.write(samples, 0, count);
        return track;
    }

    private AudioTrack returnAudioTrack(ArrayList<Pair<Integer, Integer>> pairs) {
        short[] samples = new short[0];
        for (Pair<Integer, Integer> pair : pairs) {
            int count = (int) (44100.0 * 2.0 * (pair.second / 1000.0)) & ~1;
            short[] tempSamples = new short[samples.length + count];
            System.arraycopy(samples, 0, tempSamples, 0, samples.length);
            for (int i = samples.length; i < samples.length + count; i += 2) {
                short sample = (short) (Math.sin(2 * Math.PI * i / (44100.0 / pair.first)) * 0x7FFF);
                tempSamples[i] = sample;
                tempSamples[i + 1] = sample;
            }
            samples = tempSamples;
        }
        System.out.println("BBBBBB::");
        System.out.println(samples.length);
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                samples.length * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
        track.write(samples, 0, samples.length);
        return track;
    }

    public void invertColors(View Button) {
        colorsInverted = !colorsInverted;
    }

    public ArrayList<MusicalNote> parseMidi(Sequence sequence) {
        HashMap<String, Long> hm = new HashMap<>();
        ArrayList<MusicalNote> musicalNotes = new ArrayList<>();


        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                com.example.opencv10thtry.midi.MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int octave = (key / 12) - 1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        hm.put(String.valueOf(note) + String.valueOf(octave), event.getTick());
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int octave = (key / 12) - 1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        MusicalNote musicalNote = new MusicalNote(note, octave, hm.get(String.valueOf(note) + String.valueOf(octave)), event.getTick() - hm.get(String.valueOf(note) + String.valueOf(octave)));
                        musicalNotes.add(musicalNote);
                        int velocity = sm.getData2();
                    }  //                        System.out.println("Command:" + sm.getCommand());

                }  //                    System.out.println("Other message: " + message.getClass());
            }

        }
        return musicalNotes;
    }

    public void drawNotes(ArrayList<MusicalNote> musicalNotes) {
        RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.activity_main_relative_layout);

        //add LayoutParams
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        int i = 0;
        for (MusicalNote musicalNote : musicalNotes) {
            ++i;
            long height = musicalNote.getPlayingTime();
            long startTime = musicalNote.getStartTime();
            System.out.println("stime: " + startTime);
            System.out.println("ptime: " + height);
            final DrawableView rectangle = new DrawableView(this, (musicalNote.getNote() + 1) * 100, (int) (displayHeight / 2 - height - 300), 50, (int) height);
            rectangle.setLayoutParams(params);

//            int finalI = i;
//            new android.os.Handler().postDelayed(
//                    new Runnable() {
//                        public void run() {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    System.out.println("cevaa" + finalI);
//                                }
//                            });
//                        }
//                    },
//                    20);

            long timeBeforeStart = startTime > 300 ? startTime - 300 : startTime;

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (startTime > 300) {
                                rectangle.animate().yBy(300).setDuration(height + 300);
                            } else {
                                rectangle.animate().yBy(startTime).setDuration(height + startTime);
                            }
                            mRelativeLayout.addView(rectangle);
                        }
                    });
                }
            }, timeBeforeStart);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRelativeLayout.removeView(rectangle);
                        }
                    });
                }
            }, musicalNote.getStartTime() + musicalNote.getPlayingTime());
        }

        System.out.println("cevaaa: " + mRelativeLayout.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.my_camera_view);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        mediaPlayerMap.put(0, MediaPlayer.create(this, R.raw.c5));
        mediaPlayerMap.put(1, MediaPlayer.create(this, R.raw.d5));
        mediaPlayerMap.put(2, MediaPlayer.create(this, R.raw.e5));
        mediaPlayerMap.put(3, MediaPlayer.create(this, R.raw.f5));
        mediaPlayerMap.put(4, MediaPlayer.create(this, R.raw.g5));
        mediaPlayerMap.put(5, MediaPlayer.create(this, R.raw.a5));
        mediaPlayerMap.put(6, MediaPlayer.create(this, R.raw.b5));
        mediaPlayerMap.put(7, MediaPlayer.create(this, R.raw.c6));
        mediaPlayerMap.put(8, MediaPlayer.create(this, R.raw.d6));
        mediaPlayerMap.put(9, MediaPlayer.create(this, R.raw.e6));
        mediaPlayerMap.put(10, MediaPlayer.create(this, R.raw.f6));
        mediaPlayerMap.put(11, MediaPlayer.create(this, R.raw.g6));
        mediaPlayerMap.put(12, MediaPlayer.create(this, R.raw.a6));
        mediaPlayerMap.put(13, MediaPlayer.create(this, R.raw.b6));

    }

    //        NOTE_SOUNDS.put(NOTE_NAMES[0], R.raw.c5);
//        NOTE_SOUNDS.put(NOTE_NAMES[1], R.raw.c_5);
//        NOTE_SOUNDS.put(NOTE_NAMES[2], R.raw.d5);
//        NOTE_SOUNDS.put(NOTE_NAMES[3], R.raw.d_5);
//        NOTE_SOUNDS.put(NOTE_NAMES[4], R.raw.e5);
//        NOTE_SOUNDS.put(NOTE_NAMES[5], R.raw.f5);
//        NOTE_SOUNDS.put(NOTE_NAMES[6], R.raw.f_5);
//        NOTE_SOUNDS.put(NOTE_NAMES[7], R.raw.g5);
//        NOTE_SOUNDS.put(NOTE_NAMES[8], R.raw.g_5);
//        NOTE_SOUNDS.put(NOTE_NAMES[9], R.raw.a5);
//        NOTE_SOUNDS.put(NOTE_NAMES[10], R.raw.a_5);
//        NOTE_SOUNDS.put(NOTE_NAMES[11], R.raw.b5);
//
//        NOTE_SOUNDS_WO_F_S.put(0, R.raw.c5);
//        NOTE_SOUNDS_WO_F_S.put(1, R.raw.d5);
//        NOTE_SOUNDS_WO_F_S.put(2, R.raw.e5);
//        NOTE_SOUNDS_WO_F_S.put(3, R.raw.f5);
//        NOTE_SOUNDS_WO_F_S.put(4, R.raw.g5);
//        NOTE_SOUNDS_WO_F_S.put(5, R.raw.a5);
//        NOTE_SOUNDS_WO_F_S.put(6, R.raw.b5);
//        NOTE_SOUNDS_WO_F_S.put(7, R.raw.c6);
//        NOTE_SOUNDS_WO_F_S.put(8, R.raw.d6);
//        NOTE_SOUNDS_WO_F_S.put(9, R.raw.e6);
//        NOTE_SOUNDS_WO_F_S.put(10, R.raw.f6);
//        NOTE_SOUNDS_WO_F_S.put(11, R.raw.g6);
//        NOTE_SOUNDS_WO_F_S.put(12, R.raw.a6);
//        NOTE_SOUNDS_WO_F_S.put(13, R.raw.b6);

    public Mat imgToMat(int resource) {
        InputStream is = getResources().openRawResource(resource);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Mat source = new Mat();
        Utils.bitmapToMat(bmp32, source);
        return source;
    }

    public void startTutorial(View view) {
        if (startingTime > 0) {
            startingTime = -1;
            Toast.makeText(this, "Tutorial Stopped", Toast.LENGTH_SHORT).show();

            return;
        }
        Toast.makeText(this, "Tutorial Started", Toast.LENGTH_SHORT).show();
        startingTime = new Date().getTime();
        milisSinceLastKeyWasPressed = new Date().getTime();
        milisPassedFromSongStart = 0;
        score = 0;
        InputStream ins = getResources().openRawResource(R.raw.ode_of_joy);
        Sequence sequence = null;
        try {
            sequence = MidiSystem.getSequence(ins);
            Log.d(TAG, "AAAA: ");
            musicalNotes = parseMidi(sequence);
            System.out.println(musicalNotes);
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }

    public void startFreePlay(View view) {
        if (startingTime == 0) {
            Toast.makeText(this, "Free Play Stopped", Toast.LENGTH_SHORT).show();
            startingTime = -1;
            return;
        }
        Toast.makeText(this, "Free Play Started", Toast.LENGTH_SHORT).show();
        startingTime = 0;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        try {
            Mat initialFrame = inputFrame.rgba();
//        Mat initialFrame = new Mat(frame.rows(), frame.cols(), frame.type());
//        frame.copyTo(initialFrame);

            if (!colorsInverted) {
                Mat gray = new Mat(initialFrame.rows(), initialFrame.cols(), initialFrame.type());
                Imgproc.cvtColor(initialFrame, gray, Imgproc.COLOR_BGR2GRAY);
                Mat binary = new Mat(initialFrame.rows(), initialFrame.cols(), initialFrame.type(), new Scalar(0));
//            Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10);
                Imgproc.threshold(gray, binary, 150, 255, Imgproc.THRESH_BINARY_INV);
                //Finding Contours
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
//            Imgproc.blur(binary, binary, new Size(3, 3));
                Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


                int maxValIdx = getLargestContourId(contours);

//            drawRectangles(initialFrame, getVerticesOfContour(contours.get(maxValIdx)), new Scalar(0, 255, 0));

                List<MatOfPoint> contoursOfRectangles = getAllLargeContours(contours);

                Imgproc.drawContours(initialFrame, contours, maxValIdx, new Scalar(0, 255, 0), 5);
                Imgproc.drawContours(initialFrame, contoursOfRectangles, -1, new Scalar(255, 0, 0), 5);

                List<MatOfPoint> innerContours = getInnerContoursOfContour(maxValIdx, contours, hierarchy);
                Collections.sort(innerContours, (o1, o2) -> (int) o1.toArray()[0].x - (int) o2.toArray()[0].x);

                List<Integer> pressedKeys = getPressedKeys(innerContours, contoursOfRectangles, initialFrame);

                if(startingTime == -1) {
                    return initialFrame;
                }
                if(startingTime == 0) {
                    playNotes(initialFrame, pressedKeys, innerContours, contoursOfRectangles);
                    return initialFrame;
                }
                if(milisPassedFromSongStart - 3000 > musicalNotes.get(musicalNotes.size() - 1).getStartTime() + musicalNotes.get(musicalNotes.size() - 1).getPlayingTime()) {
                    startingTime = -1;
                    return initialFrame;
                }
                playNotes(initialFrame, pressedKeys, innerContours, contoursOfRectangles);
                 startSongTutorial(initialFrame, innerContours, pressedKeys);
                return initialFrame;
//            for (int j = 0; j < contoursOfRectangles.size(); j++) {
////                getCoordsOfContour(contoursOfRectangles.get(j));
//                drawRectangles(initialFrame, contoursOfRectangles.get(j).toArray(), new Scalar(153, 0, 153));
////                drawRectangles(initialFrame, getVerticesOfContour(contoursOfRectangles.get(j)), new Scalar(153, 0, 153));
//            }
//
//
//
//            Point[] points = getVerticesOfContour(contours.get(maxValIdx));
//
//            System.out.println(Arrays.toString(points));
//
//            Rect boundingRect = getBoundingRectOfContour(contours.get(maxValIdx));
//
//
//            Imgproc.rectangle(initialFrame, new Point(boundingRect.x, boundingRect.y), new Point(boundingRect.x + boundingRect.width,
//                    boundingRect.y + boundingRect.height), new Scalar(255, 0, 0), 3);
//            Scalar color = new Scalar(0, 255, 0);
////            Imgproc.drawContours(initialFrame, contours, -1, color, 4, Imgproc.LINE_8, hierarchy, 2, new Point());
//
//            Drawing the Contours
//            return  binary;
//
//
//            MatOfPoint2f approx = new MatOfPoint2f();
//            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(maxValIdx).toArray()), approx,
//                    Imgproc.arcLength(new MatOfPoint2f(contours.get(maxValIdx).toArray()), true)*0.02, true);
//
//            MatOfPoint points = new MatOfPoint( approx.toArray() );
//
//            // Get bounding rect of contour
//            Rect rect = Imgproc.boundingRect(points);
//
//            Imgproc.rectangle(initialFrame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0, 255), 3);
            } else {
//                Imgproc.cvtColor(initialFrame, initialFrame, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.blur(initialFrame, initialFrame, new Size(3, 3));
//                Imgproc.Canny(initialFrame, initialFrame, 15, 100);
                Imgproc.cvtColor(initialFrame, initialFrame, Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(initialFrame, initialFrame, 150, 255, Imgproc.THRESH_BINARY_INV);

//            List<MatOfPoint> contours = new ArrayList<>();
//            Mat hierarchy = new Mat();
////            Imgproc.blur(binary, binary, new Size(3, 3));
//            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//            System.out.println(contours.size());
//
//            int maxValIdx = getLargestContourId(contours);
//            Imgproc.drawContours(initialFrame, getInnerContoursOfContour(maxValIdx, contours, hierarchy), -1, new Scalar(255, 0, 255),5);
//
//            Rect rect = getRectOfContour(contours.get(maxValIdx));
//            Imgproc.rectangle(initialFrame, new Point(rect.x, rect.y), new Point(rect.x + rect.width,
//                    rect.y + rect.height), new Scalar(255, 0, 0), 3);
//            Scalar color = new Scalar(0, 255, 0);
//            Imgproc.drawContours(initialFrame, contours, -1, color, 4, Imgproc.LINE_8, hierarchy, 2, new Point());


//            Imgproc.drawContours(initialFrame, contours, maxValIdx, new Scalar(0, 0, 255), 5);
//            Drawing the Contours
//            return  binary;


//            Mat template = imgToMat(R.raw.piano);
//            if(!isImageRead) {
//                isImageRead = true;
////                Mat source = imgToMat(R.raw.piano2);
//
//                Mat outputImage=new Mat();
//                int machMethod=Imgproc.TM_CCOEFF;
////                Template matching method
//
//                Imgproc.matchTemplate(initialFrame, template, outputImage, machMethod);
//
//
//                Core.MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
//                matchLoc = mmr.maxLoc;
//                //Draw rectangle on result image
//
////                Imgproc.resize(initialFrame, initialFrame, initialFrame.size(), 0);
//            }
//
//            Imgproc.rectangle(initialFrame, matchLoc, new Point(matchLoc.x + initialFrame.cols(),
//                    matchLoc.y + initialFrame.rows()), new Scalar(0, 255, 0));
            }
            return initialFrame;
        } catch (Exception e) {
            e.printStackTrace();
            return inputFrame.rgba();
        }
    }

    public void playNotes(Mat initialFrame, List<Integer> pressedKeys, List<MatOfPoint> innerContours, List<MatOfPoint> contoursOfRectangles) {
        for (Integer key : allPressedKeys) {
            if (!pressedKeys.contains(key)) {
                stopMediaPlayer(mediaPlayerMap.get(key));
            }
        }

        milisPassedFromSongStart = new Date().getTime() - startingTime;
//            Imgproc.putText(initialFrame, String.valueOf(milisPassedFromSongStart), new Point(100, 100), 1, 3, new Scalar(133, 133, 0), 3);

        if (!getPressedKeys(innerContours, contoursOfRectangles, initialFrame).isEmpty()) {
            for (Integer key : pressedKeys) {
                startMediaPlayer(mediaPlayerMap.get(key));
            }
        }

        allPressedKeys = pressedKeys;
    }

    public void startSongTutorial(Mat initialFrame, List<MatOfPoint> innerContours, List<Integer> pressedKeys) {
        Map<Integer, Integer> noteHeights = new HashMap<>();
        for (MusicalNote musicalNote : musicalNotes) {
            if (NOTE_NAMES[musicalNote.getNote()].length() == 1) {
                String secondsLeft = "";

                if (milisPassedFromSongStart >= musicalNote.getStartTime() - 1000 && milisPassedFromSongStart < musicalNote.getStartTime()) {
                    secondsLeft = "1";
                } else if (milisPassedFromSongStart >= musicalNote.getStartTime() - 2000 && milisPassedFromSongStart < musicalNote.getStartTime()) {
                    secondsLeft = "2";
                } else if (milisPassedFromSongStart >= musicalNote.getStartTime() - 3000 && milisPassedFromSongStart < musicalNote.getStartTime()) {
                    secondsLeft = "3";
                }
                if (milisPassedFromSongStart >= musicalNote.getStartTime() && milisPassedFromSongStart < musicalNote.getStartTime() + musicalNote.getPlayingTime()) { //
//                        Imgproc.putText(initialFrame, NOTE_NAMES[musicalNote.getNote()], new Point(100, 200), 1, 5, new Scalar(0, 133, 133), 5);
                    highlightNote(initialFrame, innerContours, Integer.parseInt(map.get(NOTE_NAMES[musicalNote.getNote()])), new Scalar(133, 0, 133), 6);
                    secondsLeft = "0";
                    if (pressedKeys.contains(Integer.parseInt(map.get(NOTE_NAMES[musicalNote.getNote()])))) {
                        score++;
                    }
                }
                if (secondsLeft.length() > 0) {
                    if (!noteHeights.containsKey(musicalNote.getNote())) {
                        Imgproc.putText(initialFrame, secondsLeft, innerContours.get(Integer.parseInt(map.get(NOTE_NAMES[musicalNote.getNote()]))).toArray()[0], 1, 3, new Scalar(133, 0, 133), 3);
                        noteHeights.put(musicalNote.getNote(), 100);
                    } else {
                        Imgproc.putText(initialFrame, secondsLeft, new Point(innerContours.get(Integer.parseInt(map.get(NOTE_NAMES[musicalNote.getNote()]))).toArray()[0].x, innerContours.get(Integer.parseInt(map.get(NOTE_NAMES[musicalNote.getNote()]))).toArray()[0].y - noteHeights.get(musicalNote.getNote())), 1, 3, new Scalar(133, 0, 133), 3);
                        noteHeights.put(musicalNote.getNote(), noteHeights.get(musicalNote.getNote()) + 100);
                    }
                }
            }
        }

        Imgproc.putText(initialFrame, String.valueOf(score), new Point((int) (displayWidth / 2), 100), 1, 7, new Scalar(0, 255, 0), 5);
    }



    private void highlightNote(Mat initialFrame, List<MatOfPoint> contours, Integer index, Scalar color, int thickness) {
        Imgproc.drawContours(initialFrame, contours, index, color, thickness, -1);
    }

    private boolean isKeyPressed(List<MatOfPoint> innerContours, List<MatOfPoint> contoursOfRectangles, Mat initialFrame) {
        int innerContoursSize = innerContours.size();

        for (int j = 0; j < innerContoursSize; j++) {
            if (!contoursOfRectangles.contains(innerContours.get(j))) {
                Imgproc.drawContours(initialFrame, innerContours, j, new Scalar(0, 0, 255), 5);
//                return true;
            }
//            Imgproc.putText(initialFrame, String.valueOf(j), innerContours.get(j).toArray()[0], 1, 3, new Scalar(133, 0, 133), 3);
        }
        return false;
    }

    private List<Integer> getPressedKeys(List<MatOfPoint> innerContours, List<MatOfPoint> contoursOfRectangles, Mat initialFrame) {
        List<Integer> pressedKeys = new ArrayList<>();
        int innerContoursSize = innerContours.size();

        for (int j = 0; j < innerContoursSize; j++) {
            if (!contoursOfRectangles.contains(innerContours.get(j))) {
                Imgproc.drawContours(initialFrame, innerContours, j, new Scalar(0, 0, 255), 5);
                pressedKeys.add(j);
            }
//            Imgproc.putText(initialFrame, String.valueOf(j), innerContours.get(j).toArray()[0], 1, 3, new Scalar(133, 0, 133), 3);
        }
        return pressedKeys;
    }

    private List<MatOfPoint> getAllRectangles(List<MatOfPoint> contours) {
        List<MatOfPoint> lmop = new ArrayList<>();
        for (int j = 0; j < contours.size(); j++) {
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(j).toArray()), approx,
                    Imgproc.arcLength(new MatOfPoint2f(contours.get(j).toArray()), true) * 0.02, true);
            if (approx.size(0) == 4) {
                MatOfPoint m = new MatOfPoint();
                approx.convertTo(m, CvType.CV_32S);
                lmop.add(m);
            }
        }
        return lmop;
    }

    private List<MatOfPoint> getAllLargeContours(List<MatOfPoint> contours) {
        List<MatOfPoint> lmop = new ArrayList<>();
        for (int j = 0; j < contours.size(); j++) {
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(j).toArray()), approx,
                    Imgproc.arcLength(new MatOfPoint2f(contours.get(j).toArray()), true) * 0.02, true);
            if (approx.size(0) == 4 && Imgproc.contourArea(contours.get(j)) > 10000) {
//                MatOfPoint m = new MatOfPoint();
//                approx.convertTo(m, CvType.CV_32S);
                lmop.add(contours.get(j));
            }
        }
        return lmop;
    }

    public Point[] getCoordsOfContour(MatOfPoint contour) {
        for (Point p : contour.toArray()) {
            System.out.println(p);
        }
        return contour.toArray();
    }

    public Point[] getVerticesOfContour(MatOfPoint contour) {
        RotatedRect r = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
        Mat points = new Mat();
        Imgproc.boxPoints(r, points);

        Point[] points1 = new Point[points.rows()];
        for (int j = 0; j < points.rows(); j++) {
            Point p = new Point(points.get(j, 0)[0], points.get(j, 1)[0]);
            points1[j] = p;
        }

        return points1;
    }

    public void drawRectangles(Mat initialFrame, Point[] points, Scalar color) {
        Imgproc.line(initialFrame, points[0], points[1], color, 3);
        Imgproc.line(initialFrame, points[1], points[2], color, 3);
        Imgproc.line(initialFrame, points[2], points[3], color, 3);
        Imgproc.line(initialFrame, points[3], points[0], color, 3);
    }

    public int getLargestContourId(List<MatOfPoint> contours) {
        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));


            if (maxVal < contourArea) {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }
        return maxValIdx;
    }

    public Rect getBoundingRectOfContour(MatOfPoint contour) {
        return Imgproc.boundingRect(contour);
    }

    public ArrayList<MatOfPoint> getInnerContoursOfContour(int contourId, List<MatOfPoint> contours, Mat hierarchy) {
        double nextVal = hierarchy.get(0, contourId)[2];
        ArrayList<MatOfPoint> innerContours = new ArrayList<>();
        while (nextVal > -1) {
            if (Imgproc.contourArea(contours.get((int) nextVal)) > 7000) {
                innerContours.add(contours.get((int) nextVal));
            }
            nextVal = hierarchy.get(0, (int) nextVal)[0];
        }
        return innerContours;
    }


    /*


//            FastFeatureDetector fd = FastFeatureDetector.create(30);
//            isImageRead = true;

//            Imgproc.cvtColor(initialFrame, initialFrame, Imgproc.COLOR_RGBA2GRAY);
//            Imgproc.threshold(initialFrame, initialFrame, 80, 255, Imgproc.THRESH_BINARY);
            MatOfKeyPoint kp = new MatOfKeyPoint();
//            fd.detect(initialFrame, kp);
            ORB extractor = ORB.create();

            AssetManager am = getResources().getAssets();

            InputStream is = getResources().openRawResource(R.raw.piano);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
            Mat source = new Mat();
            Utils.bitmapToMat(bmp32, source);
            Imgproc.cvtColor(source, source, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.threshold(source, source, 80, 255, Imgproc.THRESH_BINARY);

            MatOfKeyPoint firstImageMoKP = new MatOfKeyPoint();
////            fd.detect(source, firstImageMoKP);
            Mat descriptors1 = new Mat();
            extractor.detect(source, firstImageMoKP, descriptors1);

            is = getResources().openRawResource(R.raw.piano2);
            bmp = BitmapFactory.decodeStream(is);
            bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
            Mat source2 = new Mat();
            Utils.bitmapToMat(bmp32, source2);
            Imgproc.cvtColor(source2, source2, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.threshold(source2, source2, 80, 255, Imgproc.THRESH_BINARY);

            MatOfKeyPoint secondImageMoKP = new MatOfKeyPoint();
            Mat descriptors2 = new Mat();

            extractor.detect(source, firstImageMoKP, descriptors1);
            extractor.detect(source, secondImageMoKP, descriptors2);

            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(descriptors1, descriptors2, matches);
            Mat imgMatches = new Mat();
            Features2d.drawMatches(source2, secondImageMoKP, source, firstImageMoKP, matches, imgMatches);

            System.out.println(Arrays.toString(matches.toArray()));

            Mat outputImage=new Mat();
            int machMethod=Imgproc.TM_CCOEFF;
            //Template matching method
            Imgproc.matchTemplate(source2, source, outputImage, machMethod);


            Core.MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
            Point matchLoc=mmr.maxLoc;
            //Draw rectangle on result image
//            Imgproc.rectangle(source, matchLoc, new Point(matchLoc.x + source2.cols(),
//                    matchLoc.y + source2.rows()), new Scalar(255, 255, 255));

            Imgproc.resize(outputImage, initialFrame, initialFrame.size(), 0);
     */


    public String printMatOfPoint(List<MatOfPoint> lmop) {
        StringBuilder s = new StringBuilder();
        for (MatOfPoint mop : lmop) {
            s.append('[');
            for (Point p : mop.toList()) {
                s.append('(').append(p.x).append(", ").append(p.y).append(')').append(", ");
            }
            s.append("],");
        }
        return s.toString();
    }

    public Point getMaxPoint(MatOfPoint mop) {
        List<Point> m = mop.toList();
        Point maxPoint = m.get(0);
        double maxX = maxPoint.x;
        double maxY = maxPoint.y;

        for (int i = 1; i < m.size(); i++) {
            Point p = m.get(i);
            if (p.x > maxX || p.y > maxY) {
                maxPoint = p;
            }
        }
        return maxPoint;
    }

    public Point getMinPoint(MatOfPoint mop) {
        List<Point> m = mop.toList();
        Point maxPoint = m.get(0);
        double maxX = maxPoint.x;
        double maxY = maxPoint.y;

        for (int i = 1; i < m.size(); i++) {
            Point p = m.get(i);
            if (p.x < maxX || p.y < maxY) {
                maxPoint = p;
            }
        }
        return maxPoint;
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }


    @Override
    public void onCameraViewStopped() {

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Use a new tread as this can take a while
//        final Thread thread = new Thread(new Runnable() {
//            public void run() {
////                genTone();
//                handler.post(new Runnable() {
//
//                    public void run() {
//                        playSound();
//                    }
//                });
//            }
//        });
//        thread.start();

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "Problem encountered! -> !OpenCVLoader.initDebug()", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {

            cameraBridgeViewBase.disableView();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    public void playMidi(View v) {
        if (mp == null) {
            mp = MediaPlayer.create(this, R.raw.ode_of_joy);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopMidi();
                }
            });
        }
        if (mp.isPlaying()) {
            stopMidi();
            return;
        }
        mp.start();
//        Toast.makeText(this, "MIDI started", Toast.LENGTH_SHORT).show();
    }

    public void playNote(int song) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, song);
        mediaPlayer.start();
    }

    public void pauseMidi(View v) {
        if (mp != null) {
            mp.pause();
        }
    }

    public void stopMidi() {
        if (mp != null) {
            mp.release();
            mp = null;
//            Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();
        }
    }

    public void startMediaPlayer(MediaPlayer mplayer) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!mplayer.isPlaying()) {
            mplayer.start();
        }
    }

    public void stopMediaPlayer(MediaPlayer mplayer) {
        try {
            if (mplayer.isPlaying()) {
                mplayer.stop();
            }
            mplayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}