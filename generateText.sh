# Assumes imagemagick is installed, and convert is in the path

# make my target directories
mkdir -p /media/sf_Screenshots/final

# Get the date for the filename
DATE=$(date +"%Y-%m-%dT%H%M%SZ")

echo "Writing to /media/sf_Screenshots/final/$DATE.csv"

# Clear the file and add a header row:
> /media/sf_Screenshots/final/"$DATE".csv
echo "Item,Selling For,Buying For,Location,Image Filename,Raw Rekognize Output" | tee /media/sf_Screenshots/final/"$DATE".csv

#Make sure the project is built before using
mvn clean install

# process raw images
for file in /media/sf_Screenshots/rekognition/*.png
do
	echo Processing "$file"
	# Simple usage; just run the class (TextDetectionInImage) with the argument being the file to be processed with full file path.
	# then write the info Rekognition gets, line by line, to the output.
	# Output is as follows:
	# Item name,Selling For,Buying For,Location,Image Filename+Path,Full Rekognition result
	mvn exec:java -q -Dexec.mainClass="TextDetectionInImage" -Dexec.args="$file" >> /media/sf_Screenshots/final/"$DATE".csv
done

echo "Results written to /media/sf_Screenshots/final/$DATE.csv"
