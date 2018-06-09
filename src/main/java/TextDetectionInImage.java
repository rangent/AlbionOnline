import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.util.IOUtils;


/**
 * Just pass in the full path to the image and this calls AWS
 * @author brian
 */
public class TextDetectionInImage {

	private static final float MIN_CONFIDENCE = 70;

	public static void main(String[] args) throws Exception {
		String photo=args[0];
		ByteBuffer imageBytes;
		try (InputStream inputStream = new FileInputStream(new File(photo))) {
			imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
		}

		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();

		DetectTextRequest request = new DetectTextRequest()
				.withImage(new Image()
						.withBytes(imageBytes));

		try {
			DetectTextResult result = rekognitionClient.detectText(request);
			List<TextDetection> textDetections = result.getTextDetections();

			StringBuilder[] strs = new StringBuilder[3];
			String name = null;
			List<Integer> prices = new ArrayList<Integer>(2); 
			int i = 0;
			for (TextDetection text: textDetections) {
				if (text.getParentId() == null) {
					// found a parent
					strs[i++] = new StringBuilder();
				} else {
					// process children
					String t = text.getDetectedText();
					try {
						int k = Integer.parseInt(t);
						strs[text.getParentId()].append(k).append(" ");
						prices.add(k);
					} catch (NumberFormatException e) {
						// not a number
						if (text.getConfidence() > MIN_CONFIDENCE) {
							strs[text.getParentId()].append(t).append(" ");
						}
					}
				}
			}
			name = strs[0].toString();
			
			System.out.println(name + "," + prices.get(0) + "," + prices.get(1));
			
		} catch(AmazonRekognitionException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}