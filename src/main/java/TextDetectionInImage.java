import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

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

	@SuppressWarnings("deprecation")
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

			StringBuilder[] strs = new StringBuilder[10];
			String name = null;
			Integer[] prices = new Integer[2];
			int i = 0;
			boolean sawPrice = false;
			int price = 0;
			for (TextDetection text: textDetections) {
				if (text.getParentId() == null) {
					// found a parent
					strs[i++] = new StringBuilder();
					
					// bypass the nothing found to be sold/bought text
					if (text.getDetectedText().trim().contains("No matchable")) {
						prices[0]=-1;
					} else if (text.getDetectedText().trim().contains("No competing")) {
						prices[1]=-1;
					}
				} else {
					// process children
					String t = preprocessString(text.getDetectedText());
					try {
						int k = Integer.parseInt(t);
						strs[text.getParentId()].append(k).append(" ");
						if (sawPrice) {
							prices[price] = k;
							sawPrice = false;
						}
					} catch (NumberFormatException e) {
						// not a number
						if (t.contains("price") || (t.toLowerCase().contains("matchable") && !t.toLowerCase().contains("no"))) {
							sawPrice = true;
						} else {
							sawPrice = false;
						}
						if (text.getConfidence() > MIN_CONFIDENCE) {
							strs[text.getParentId()].append(t).append(" ");
						}
					}
				}
			}
			name = WordUtils.capitalize(strs[0].toString().trim());
			
			System.out.println(name + "," + prices[0] + "," + prices[1] + "," + photo + ",\"" + Arrays.toString(strs) + "\"");
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	/**
	 * Remove unneeded characters
	 * @param str
	 * @return
	 */
	private static String preprocessString(String str) {
		if (str.length() < 2) {
			str = "";
		} else {
			str = str.replace("(", "");
			str = str.replace(")", "");
			str = str.replace(".", "");
			str = str.replace(",", "");
			
			// Common mistakes
			str = str.replace("Simdle", "Simple");
			str = str.replace("Odulent", "Opulent");
			str = str.replace("eopulent", "Opulent");
			str = str.replace("Ornare", "Ornate");
			str = str.replace("Barooue", "Baroque");
			str = str.replace("EExceptional", "Exceptional");
			str = str.replace("Exceprionall", "Exceptional");
			str = str.replace("Exceprional", "Exceptional");
			str = str.replace("xceptional", "Exceptional");
			str = str.replace("Leatter", "Leather");
			str = str.replace("Hardene", "Hardened");
			str = str.replace("Uncommonornate", "Uncommon Ornate");
			str = str.replace("Uncommoncured", "Uncommon Cured");
			str = str.replace("Tiranium", "Titanium");
			str = str.replace("Sreel", "Steel");
			str = str.replace("Uncommons", "Uncommon");
			str = str.replace("Uncornmon", "Uncommon");
			str = str.replace("Adarnantium", "Adamantium");
			str = str.replace("Mefeort", "Meteorite");
			str = str.replace("Exceprionalsteel", "Exceptional Steel");
			str = str.replace("Meteorit", "Meteorite");
			str = str.replace("UncommonAshenba", "Uncommon Ashenbark");
			str = str.replace("Whirewood", "Whitewood");
			
		}
		return str;
	}
}