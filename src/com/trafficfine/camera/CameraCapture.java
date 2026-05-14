package com.trafficfine.camera;

import com.sun.jna.NativeLibrary;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraCapture {

    private static final String OPENCV_DLL =
            "C:\\Users\\ojhab\\Downloads\\opencv\\build\\java\\x64\\opencv_java4130.dll";

    private static final String TESS_PATH =
            "C:\\Program Files\\Tesseract-OCR";

    private static final String TESSDATA_PATH =
            "C:\\Program Files\\Tesseract-OCR\\tessdata";

    static {

        try {

            System.load(OPENCV_DLL);

            System.out.println(
                    "OpenCV loaded OK from: "
                            + OPENCV_DLL
            );

        } catch (UnsatisfiedLinkError e) {

            System.err.println(
                    "OpenCV load FAILED: "
                            + e.getMessage()
            );
        }

        try {

            NativeLibrary.addSearchPath(
                    "tesseract-5",
                    TESS_PATH
            );

            NativeLibrary.addSearchPath(
                    "tesseract50",
                    TESS_PATH
            );

            NativeLibrary.addSearchPath(
                    "leptonica-6",
                    TESS_PATH
            );

            System.setProperty(
                    "jna.library.path",
                    TESS_PATH
            );

            System.out.println(
                    "Tesseract path set: "
                            + TESS_PATH
            );

        } catch (Exception e) {

            System.err.println(
                    "Tesseract path error: "
                            + e.getMessage()
            );
        }
    }

    private VideoCapture camera;
    private CascadeClassifier plateDetector;
    private Tesseract tesseract;

    private boolean cameraOpen = false;

    private List<Rect> lastDetectedPlates =
            new ArrayList<>();

    private Mat lastRawFrame = new Mat();

    public CameraCapture() {

        plateDetector = new CascadeClassifier();

        if (!plateDetector.load(
                "haarcascade_russian_plate_number.xml"
        )) {

            System.err.println(
                    "WARNING: Plate cascade XML not found."
            );

            plateDetector = null;
        }

        try {

            tesseract = new Tesseract();

            tesseract.setDatapath(TESSDATA_PATH);

            tesseract.setLanguage("eng");

            tesseract.setPageSegMode(7);

            tesseract.setOcrEngineMode(1);

            tesseract.setTessVariable(
                    "tessedit_char_whitelist",
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            );

            System.out.println(
                    "Tesseract initialized OK."
            );

        } catch (Exception e) {

            System.err.println(
                    "Tesseract init failed: "
                            + e.getMessage()
            );

            tesseract = null;
        }
    }

    public boolean startCamera() {

        camera = new VideoCapture(0);

        cameraOpen = camera.isOpened();

        return cameraOpen;
    }

    public boolean isCameraOpen() {

        return cameraOpen
                && camera != null
                && camera.isOpened();
    }

    public BufferedImage captureFrame() {

        if (!isCameraOpen()) {
            return null;
        }

        Mat frame = new Mat();

        if (!camera.read(frame) || frame.empty()) {
            return null;
        }

        frame.copyTo(lastRawFrame);

        if (plateDetector != null) {

            Mat gray = new Mat();

            Imgproc.cvtColor(
                    frame,
                    gray,
                    Imgproc.COLOR_BGR2GRAY
            );

            Imgproc.equalizeHist(
                    gray,
                    gray
            );

            MatOfRect plates =
                    new MatOfRect();

            plateDetector.detectMultiScale(
                    gray,
                    plates,
                    1.08,
                    5,
                    0,
                    new Size(80, 25),
                    new Size(400, 120)
            );

            lastDetectedPlates =
                    new ArrayList<>(
                            List.of(plates.toArray())
                    );

            for (Rect r : lastDetectedPlates) {

                Imgproc.rectangle(
                        frame,
                        r,
                        new Scalar(0, 255, 0),
                        2
                );

                Imgproc.putText(
                        frame,
                        "PLATE DETECTED",
                        new Point(r.x, r.y - 8),
                        Imgproc.FONT_HERSHEY_SIMPLEX,
                        0.55,
                        new Scalar(0, 255, 0),
                        2
                );
            }
        }

        try {

            return matToBufferedImage(frame);

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    public String readPlateOCR() {

        if (tesseract == null) {

            System.err.println(
                    "Tesseract not initialized."
            );

            return null;
        }

        if (lastRawFrame.empty()) {
            return null;
        }

        if (lastDetectedPlates.isEmpty()) {
            return runOCR(lastRawFrame);
        }

        for (Rect r : lastDetectedPlates) {

            int pad = 6;

            int x = Math.max(0, r.x - pad);

            int y = Math.max(0, r.y - pad);

            int w = Math.min(
                    lastRawFrame.cols() - x,
                    r.width + pad * 2
            );

            int h = Math.min(
                    lastRawFrame.rows() - y,
                    r.height + pad * 2
            );

            Mat crop =
                    new Mat(
                            lastRawFrame,
                            new Rect(x, y, w, h)
                    );

            String result = runOCR(crop);

            if (result != null &&
                    result.length() >= 4) {

                return result;
            }
        }

        return null;
    }

    private String runOCR(Mat input) {

        Mat processed =
                preprocessForOCR(input);

        BufferedImage img =
                matToBufferedImage(processed);

        try {

            String raw =
                    tesseract.doOCR(img);

            String cleaned =
                    raw.toUpperCase()
                            .replaceAll(
                                    "[^A-Z0-9]",
                                    ""
                            )
                            .trim();

            System.out.println(
                    "OCR raw=["
                            + raw.trim()
                            + "] cleaned=["
                            + cleaned
                            + "]"
            );

            return cleaned.length() >= 4
                    ? cleaned
                    : null;

        } catch (TesseractException e) {

            System.err.println(
                    "OCR failed: "
                            + e.getMessage()
            );

            return null;
        }
    }

    private Mat preprocessForOCR(Mat src) {

        Mat result = new Mat();

        Imgproc.resize(
                src,
                result,
                new Size(
                        src.width() * 3,
                        src.height() * 3
                ),
                0,
                0,
                Imgproc.INTER_CUBIC
        );

        if (result.channels() > 1) {

            Imgproc.cvtColor(
                    result,
                    result,
                    Imgproc.COLOR_BGR2GRAY
            );
        }

        Imgproc.GaussianBlur(
                result,
                result,
                new Size(3, 3),
                0
        );

        Imgproc.adaptiveThreshold(
                result,
                result,
                255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                15,
                10
        );

        Mat kernel =
                Imgproc.getStructuringElement(
                        Imgproc.MORPH_RECT,
                        new Size(2, 2)
                );

        Imgproc.morphologyEx(
                result,
                result,
                Imgproc.MORPH_DILATE,
                kernel
        );

        return result;
    }

    public String saveSnapshot(String plateNumber) {

        if (!isCameraOpen()) {
            return null;
        }

        Mat frame = new Mat();

        if (!camera.read(frame)
                || frame.empty()) {

            return null;
        }

        for (Rect r : lastDetectedPlates) {

            Imgproc.rectangle(
                    frame,
                    r,
                    new Scalar(0, 255, 0),
                    2
            );
        }

        String dir =
                "captured_violations";

        new File(dir).mkdirs();

        String ts =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss"
                ).format(new Date());

        String safe =
                plateNumber.replaceAll(
                        "[^A-Z0-9]",
                        ""
                );

        String path =
                dir
                        + File.separator
                        + "VIO_"
                        + ts
                        + "_"
                        + safe
                        + ".jpg";

        Imgcodecs.imwrite(
                path,
                frame
        );

        System.out.println(
                "Snapshot saved: "
                        + path
        );

        return path;
    }

    public void stopCamera() {

        if (camera != null) {

            camera.release();

            cameraOpen = false;
        }
    }

    public boolean hasPlateDetected() {

        return !lastDetectedPlates.isEmpty();
    }

    public static BufferedImage matToBufferedImage(Mat mat) {

        if (mat == null || mat.empty()) {
            return null;
        }

        Mat converted = new Mat();

        if (mat.channels() == 1) {

            Imgproc.cvtColor(
                    mat,
                    converted,
                    Imgproc.COLOR_GRAY2BGR
            );

        } else if (mat.channels() == 3) {

            converted = mat.clone();

        } else if (mat.channels() == 4) {

            Imgproc.cvtColor(
                    mat,
                    converted,
                    Imgproc.COLOR_BGRA2BGR
            );

        } else {

            System.out.println(
                    "Unsupported channels: "
                            + mat.channels()
            );

            return null;
        }

        int width = converted.width();

        int height = converted.height();

        int channels = converted.channels();

        byte[] sourcePixels =
                new byte[
                        width
                                * height
                                * channels
                        ];

        converted.get(
                0,
                0,
                sourcePixels
        );

        BufferedImage image =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_3BYTE_BGR
                );

        byte[] targetPixels =
                ((DataBufferByte)
                        image.getRaster()
                                .getDataBuffer())
                        .getData();

        System.arraycopy(
                sourcePixels,
                0,
                targetPixels,
                0,
                sourcePixels.length
        );

        return image;
    }
}