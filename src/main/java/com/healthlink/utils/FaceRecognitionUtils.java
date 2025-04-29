package com.healthlink.utils;


import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class FaceRecognitionUtils {

    public static double compareFacesWithOpenCV(String imagePath1, String imagePath2) {
        System.out.println("Chargement de l'image 1 : " + imagePath1);
        Mat img1 = Imgcodecs.imread(imagePath1, Imgcodecs.IMREAD_GRAYSCALE);
        System.out.println("Chargement de l'image 2 : " + imagePath2);
        Mat img2 = Imgcodecs.imread(imagePath2, Imgcodecs.IMREAD_GRAYSCALE);

        if (img1.empty()) {
            System.out.println("Erreur : Impossible de charger l'image 1 (" + imagePath1 + ")");
            return Double.MAX_VALUE; // Retourne une valeur très grande en cas d'erreur
        }
        if (img2.empty()) {
            System.out.println("Erreur : Impossible de charger l'image 2 (" + imagePath2 + ")");
            return Double.MAX_VALUE; // Retourne une valeur très grande en cas d'erreur
        }

        // Normalisation de l'éclairage
        Imgproc.equalizeHist(img1, img1);
        Imgproc.equalizeHist(img2, img2);

        // Redimensionner pour comparaison
        Size sz = new Size(200, 200);
        Imgproc.resize(img1, img1, sz);
        Imgproc.resize(img2, img2, sz);

        // Calcul de la différence
        Mat diff = new Mat();
        Core.absdiff(img1, img2, diff);
        Scalar sumDiff = Core.sumElems(diff);

        double totalDiff = sumDiff.val[0];
        System.out.println("Différence totale : " + totalDiff);

        return totalDiff; // Retourne la différence au lieu d'un booléen
    }
    public static double compareHistograms(String imagePath1, String imagePath2) {
        System.out.println("Chargement de l'image 1 : " + imagePath1);
        Mat img1 = Imgcodecs.imread(imagePath1, Imgcodecs.IMREAD_GRAYSCALE);
        System.out.println("Chargement de l'image 2 : " + imagePath2);
        Mat img2 = Imgcodecs.imread(imagePath2, Imgcodecs.IMREAD_GRAYSCALE);

        if (img1.empty() || img2.empty()) {
            System.out.println("Erreur : Impossible de charger une des images.");
            return -1.0; // -1 pour signaler une erreur
        }

        // Prétraitement : égalisation et redimensionnement
        Imgproc.equalizeHist(img1, img1);
        Imgproc.equalizeHist(img2, img2);
        Imgproc.resize(img1, img1, new Size(200, 200));
        Imgproc.resize(img2, img2, new Size(200, 200));

        // Calcul des histogrammes
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();
        Imgproc.calcHist(java.util.Arrays.asList(img1), new MatOfInt(0), new Mat(), hist1, new MatOfInt(256), new MatOfFloat(0, 256));
        Imgproc.calcHist(java.util.Arrays.asList(img2), new MatOfInt(0), new Mat(), hist2, new MatOfInt(256), new MatOfFloat(0, 256));

        // Normalisation des histogrammes
        Core.normalize(hist1, hist1, 0, 1, Core.NORM_MINMAX);
        Core.normalize(hist2, hist2, 0, 1, Core.NORM_MINMAX);

        // Comparaison par corrélation (score entre -1 et 1 ; 1 = identique)
        double score = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);
        System.out.println("Score de corrélation : " + score);
        return score;
    }

}