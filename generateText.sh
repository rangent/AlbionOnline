# Assumes imagemagick is installed, and convert is in the path

# make my target directories
mkdir -p /media/sf_Screenshots/final
> /media/sf_Screenshots/final/output.csv

mvn clean install

# process raw images
for file in /media/sf_Screenshots/rekognition/*.png
do
	echo Processing "$file"
	mvn exec:java -q -Dexec.mainClass="TextDetectionInImage" -Dexec.args="$file" >> /media/sf_Screenshots/final/output.csv
done
