package edu.connection.models;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;

public class YoloV8Detector implements AutoCloseable {
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    private static final int WIDTH = 640;
    private static final int HEIGHT = 640;
    private static final float CONFIDENCE_THRESHOLD = 0.25f;

    private final OrtEnvironment env;
    private final OrtSession session;

    private final String[] classNames = {"good_crop", "bad_crop"};

    public YoloV8Detector(String modelPath) throws OrtException, IOException {
        this.env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
        this.session = env.createSession(modelPath, opts);
    }

    public String simpleDetect(Mat image) throws OrtException {
        Mat processedImage = preprocessImage(image);
        float[] inputArray = matToFloatArray(processedImage);

        long[] shape = {1, 3, HEIGHT, WIDTH};
        OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputArray), shape);

        try (OrtSession.Result results = session.run(Collections.singletonMap("images", tensor))) {
            Object rawOutput = results.get(0).getValue();

            float[][] output;
            if (rawOutput instanceof float[][]) {
                output = (float[][]) rawOutput;
            } else if (rawOutput instanceof float[][][]) {
                output = ((float[][][]) rawOutput)[0];
            } else {
                throw new OrtException("Format de sortie non supporté: " + rawOutput.getClass());
            }

            // Debug: Afficher les scores bruts
            System.out.println("Scores bruts: " + Arrays.toString(output[0]));

            // Les 4 premiers éléments sont les coordonnées, ensuite les scores de classe
            float[] classScores = Arrays.copyOfRange(output[0], 4, 4 + classNames.length);
            System.out.println("Scores de classe: " + Arrays.toString(classScores));

            int bestClassId = -1;
            float bestScore = CONFIDENCE_THRESHOLD; // Seuil minimum
            for (int i = 0; i < classScores.length; i++) {
                if (classScores[i] > bestScore) {
                    bestScore = classScores[i];
                    bestClassId = i;
                }
            }

            if (bestClassId != -1) {
                System.out.println("Détection: " + classNames[bestClassId] + " avec score: " + bestScore);
                return classNames[bestClassId];
            } else {
                System.out.println("Aucune détection valide au-dessus du seuil");
                return "indeterminate";
            }
        }
    }
    private Mat preprocessImage(Mat image) {
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new org.opencv.core.Size(WIDTH, HEIGHT));
        Imgproc.cvtColor(resizedImage, resizedImage, Imgproc.COLOR_BGR2RGB);
        resizedImage.convertTo(resizedImage, org.opencv.core.CvType.CV_32F, 1.0 / 255);
        return resizedImage;
    }

    private float[] matToFloatArray(Mat mat) {
        int channels = mat.channels();
        int width = mat.cols();
        int height = mat.rows();
        float[] data = new float[channels * width * height];
        mat.get(0, 0, data);
        return data;
    }

    @Override
    public void close() {
        try {
            session.close();
            env.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de YoloV8Detector: " + e.getMessage());
        }
    }
}
