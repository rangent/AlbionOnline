import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		String locationFromFilename = photo.split("-")[0].split("/")[4]; // assumes "/media/sf_Screenshots/rekognition/" path
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
			int priceIndex = 0;
			for (TextDetection text: textDetections) {
				if (text.getParentId() == null) {
					// found a parent
					strs[i++] = new StringBuilder();
					
					// bypass the nothing found to be sold/bought text
					if (text.getDetectedText().trim().contains("No matchable")) {
						prices[0]=-1;
						priceIndex++;
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
							prices[priceIndex++] = k;
							sawPrice = false;
						}
					} catch (NumberFormatException e) {
						// not a number
						
						// sometimes we get "Best matchable ####" and a number
						// with price on the next item for some reason? Still want to look for a price though
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
			
			System.out.println(name + "," + prices[0] + "," + prices[1] + "," + locationFromFilename + "," + photo + ",\"" + Arrays.toString(strs) + "\"");
			
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
			str = str.replace(":", "");
			
			// Common mistakes
			str = str.replace("Simdle\\b", "Simple");
			str = str.replace("Odulent\\b", "Opulent");
			str = str.replace("eopulent\\b", "Opulent");
			str = str.replace("Ornare\\b", "Ornate");
			str = str.replace("EExceptional", "Exceptional");
			str = str.replace("Exceprionall\\b", "Exceptional");
			str = str.replace("Exceprional\\b", "Exceptional");
			str = str.replace("EExceptionall\\b", "Exceptional");
			str = str.replace("xceptional\\b", "Exceptional");
			str = str.replace("Leatter", "Leather");
			str = str.replace("Hardene\\b", "Hardened");
			str = str.replace("Hardenedd\\b", "Hardened");
			str = str.replace("Uncommonornate\\b", "Uncommon Ornate");
			str = str.replace("Uncommoncured\\b", "Uncommon Cured");
			str = str.replace("Tiranium\\b", "Titanium");
			str = str.replace("Sreel\\b", "Steel");
			str = str.replace("Uncommons\\b", "Uncommon");
			str = str.replace("Uncornmon\\b", "Uncommon");
			str = str.replace("Uncornmnon\\b", "Uncommon");
			str = str.replace("Adarnantium\\b", "Adamantium");
			str = str.replace("Mefeort\\b", "Meteorite");
			str = str.replace("Exceprionalsteel\\b", "Exceptional Steel");
			str = str.replace("Meteorit\\b", "Meteorite");
			str = str.replace("UncommonAshenba\\b", "Uncommon Ashenbark");
			str = str.replace("Whirewood\\b", "Whitewood");
			str = str.replace("EExceptionall\\b", "Exceptional");
			str = str.replace("EExceptional\\b", "Exceptional");
			str = str.replace("\\bBa", "Bar");
			str = str.replace("\\bBal", "Bar");
			str = str.replace("\\bBan", "Bar");
			str = str.replace("\\bClott", "Cloth");
			str = str.replace("\\bNorked\\b", "Worked");
			str = str.replace("leteorite\\b", "Meteorite");
			str = str.replace("xceptional\\b", "Exceptional");
			str = str.replace("eteorite\\b", "Meteorite");
			str = str.replace("Exceptionals\\b", "Exceptional");
			str = str.replace("Exceprional\\b", "Exceptional");
			str = str.replace("Barooue", "Baroque");
			
			// Numbers that contain an 'S' that should be 5
			String startsWithNumbers = "\\d+s\\d?";
			String endsWithNumbers = "\\d?s\\d+";
			Pattern ps = Pattern.compile(startsWithNumbers);
			Pattern pe = Pattern.compile(endsWithNumbers);
			Pattern endWordChar = Pattern.compile("(\\d+)\\D+");
			Matcher m = endWordChar.matcher(str);
			if (ps.matcher(str).matches() || pe.matcher(str).matches()) {
				str = str.toLowerCase().replace("s", "5");
			} else if (m.matches()) {
				// Numbers contain random letters due to the incorrect reading of the silvers symbol
				// want to chop these off
				str = m.group(1);
			}
			
		}
		return str;
	}
}