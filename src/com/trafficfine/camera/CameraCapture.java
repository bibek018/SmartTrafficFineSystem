package com.trafficfine.camera;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraCapture {

    static {
        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private VideoCapture camera;
    private CascadeClassifier plateDetector;
    private boolean cameraOpen = false;

    public CameraCapture() {
        // Load Haar cascade for license plate detection
        // Download haarcascade_russian_plate_number.xml and place in project root
        String cascadePath = "haarcascade_russian_plate_number.xml";
        plateDetector = new CascadeClassifier();
        if (!plateDetector.load(cascadePath)) {
            System.err.println("Warning: Plate cascade not loaded. Plate detection disabled.");
            plateDetector = null;
        }
    }

    public boolean startCamera() {
        camera = new VideoCapture(0);
        cameraOpen = camera.isOpened();
        return cameraOpen;
    }

    public boolean isCameraOpen() {
        return cameraOpen && camera != null && camera.isOpened();
    }

    /**
     * Capture one frame, draw plate bounding boxes, return as BufferedImage for Swing.
     */
    public BufferedImage captureFrame() {
        if (!isCameraOpen()) return null;
        Mat frame = new Mat();
        if (!camera.read(frame) || frame.empty()) return null;
        // Detect and draw plates
        if (plateDetector != null) {
            Mat gray = new Mat();
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
            MatOfRect plates = new MatOfRect();
            plateDetector.detectMultiScale(gray, plates, 1.1, 4, 0,
                    new Size(60, 20), new Size(300, 100));
            for (Rect r : plates.toArray()) {
                Imgproc.rectangle(frame, r, new Scalar(0, 255, 0), 2);
                Imgproc.putText(frame, "PLATE", new Point(r.x, r.y - 5),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 255, 0), 2);
            }
        }
        return matToBufferedImage(frame);
    }

    /**
     * Save current frame to disk and return file path.
     */
    public String saveSnapshot(String plateNumber) {
        if (!isCameraOpen()) return null;
        Mat frame = new Mat();
        if (!camera.read(frame) || frame.empty()) return null;

        String dir = "captured_violations";
        new File(dir).mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = dir + File.separator + "VIO_" + timestamp + "_" + plateNumber.replace(" ", "") + ".jpg";
        Imgcodecs.imwrite(filename, frame);
        return filename;
    }

    public void stopCamera() {
        if (camera != null) {
            camera.release();
            cameraOpen = false;
        }
    }

    /**
     * Convert OpenCV Mat to Java BufferedImage for display in Swing.
     */
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else {
            type = BufferedImage.TYPE_3BYTE_BGR;
            // OpenCV is BGR; Swing expects RGB — swap channels
            Mat rgb = new Mat();
            Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);
            mat = rgb;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}