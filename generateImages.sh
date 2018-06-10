# Assumes imagemagick is installed, and convert is in the path

# make my target directories
mkdir -p /media/sf_Screenshots/processed
mkdir -p /media/sf_Screenshots/rekognition

# process raw images
for file in /media/sf_Screenshots/raw/*.bmp
do
	file=$(basename "$file")
	echo "$file"
	convert /media/sf_Screenshots/raw/"$file" -crop 420x28+1079+419 -colorspace Gray -brightness-contrast 30x100 /media/sf_Screenshots/processed/"$file".title.png
	convert /media/sf_Screenshots/raw/"$file" -crop 614x68+943+626 -negate /media/sf_Screenshots/processed/"$file".price.png
	convert /media/sf_Screenshots/processed/"$file".title.png /media/sf_Screenshots/space.png /media/sf_Screenshots/processed/"$file".price.png -background black -append /media/sf_Screenshots/rekognition/"$file".png
done
