package application;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ServerController {
	@FXML
	private ImageView imgViewer;

	static boolean streaming = false;

	public static Image mat2Image(Mat img) {
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", img, byteMat);
		return new Image(new ByteArrayInputStream(byteMat.toArray()));
	}

	public static BufferedImage Mat2bufferedImage(Mat image) throws IOException {
		MatOfByte bytemat = new MatOfByte();
		Imgcodecs.imencode(".jpg", image, bytemat);
		byte[] bytes = bytemat.toArray();
		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage img = null;
		img = ImageIO.read(in);
		return img;
	}

	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}

	// OutputStream os = null;

	ServerSocket listener = null;
	String line = null;
	InputStream is = null;
	BufferedWriter os = null;
	Socket socket = null;

	@FXML
	public void onStart(ActionEvent event) {
		try {
			listener = new ServerSocket(1998);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Server is waiting to accept user!");

		try {
			socket = listener.accept();
			System.out.println("Accepted a client!");

			is = socket.getInputStream();
			os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						
						try {
							byte[] imageAr = new byte[40000];
							is.read(imageAr);

							BufferedImage image;
							image = ImageIO.read(new ByteArrayInputStream(imageAr));
							Platform.runLater(() -> {
								imgViewer.setImage(SwingFXUtils.toFXImage(image, null));
							});
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
			}).start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Event Listener on Button.onAction
	@FXML
	public void onStop(ActionEvent event) {
		Platform.runLater(() -> {
			streaming = false;
		});

	}

	public boolean init() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		return true;
	}
}
