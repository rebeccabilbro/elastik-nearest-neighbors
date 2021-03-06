/**
 * Given an image, use a pre-trained deep convolutional neural network
 * to compute its feature vector.
 */


package myapps;

import org.datavec.image.loader.Java2DNativeImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.graph.vertex.GraphVertex;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.zoo.model.ResNet50;
import org.deeplearning4j.zoo.*;
import org.nd4j.linalg.api.ndarray.INDArray;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImagePrediction {

    public static void main(String[] args) throws Exception {

        // Load pre-trained Convnet.
        ZooModel zooModel = new ResNet50();
        ComputationGraph pretrained = (ComputationGraph) zooModel.initPretrained(PretrainedType.IMAGENET);

        // Load input image.
        final String imgPath = "../imagenet-pizza.JPEG";
        File inputFile = new File(imgPath);
        BufferedImage inputImage = ImageIO.read(inputFile);

        // Convert input image into format used by Convnet..
        NativeImageLoader inputLoader = new NativeImageLoader(224, 224, 3, true);
        INDArray imageMatrix = inputLoader.asMatrix(inputImage);

        // Write image back to disk (to see exactly how the previous step pre-processed the image.
        Java2DNativeImageLoader outputLoader = new Java2DNativeImageLoader();
        BufferedImage outputImage = outputLoader.asBufferedImage(imageMatrix);
        ImageIO.write(outputImage, "jpg", new File("../out.JPEG"));

        // Make prediction and print most probable class, e.g. 963.
        INDArray output = pretrained.outputSingle(imageMatrix);
        System.out.println(output.argMax());

        ComputationGraph model = new TransferLearning.GraphBuilder(pretrained)
                .removeVertexAndConnections("fc1000")
                .setOutputs("flatten_3")
                .build();

        GraphVertex[] vertices = model.getVertices();
        for (int i = 0; i < vertices.length; i++) {
            GraphVertex v = vertices[i];
            // System.out.println(String.format("%d %s", v.getVertexIndex(), v.getVertexName()));
        }

        output = model.outputSingle(imageMatrix);
        System.out.println(output.shapeInfoToString());
        System.out.println(String.format("%.3f %.3f %.3f",
                output.minNumber(), output.meanNumber(), output.maxNumber()));

        System.exit(0);

    }

}
