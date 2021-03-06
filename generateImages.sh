# Assumes imagemagick is installed, and convert is in the path

# make my target directories
mkdir -p /media/sf_Screenshots/processed
mkdir -p /media/sf_Screenshots/rekognition

# process raw images
for file in /media/sf_Screenshots/raw/*.bmp
do
	# get just the base filename (not the whole path)
	file=$(basename "$file")
	echo "$file"

	# First crop out the title of the item
	convert /media/sf_Screenshots/raw/"$file" -crop 480x28+1050+419 /media/sf_Screenshots/processed/"$file".title.png

	# Then crop the sale/buy prices
	convert /media/sf_Screenshots/raw/"$file" -crop 614x68+943+626 -negate -modulate 100,200,100 /media/sf_Screenshots/processed/"$file".price.png
	# Merge the two into a single image, then add some black space between the two
	# Rekognition does better with larger items, so I'm just rolling both title/price parts into the same image
	# Adding the space between the two parts seemed to help Rekognition process the two sections better
	convert /media/sf_Screenshots/processed/"$file".title.png ./assets/space.png /media/sf_Screenshots/processed/"$file".price.png -background black -append /media/sf_Screenshots/rekognition/"$file".png

	# The "silvers" image messes up Rekognition and it keeps trying to read it as an image. 
	# subimage-find (https://github.com/johnoneil/subimage) has the ability to find an image and surround it in red. This seems
	# to be sufficient to have Rekognition ignore that part of the image. If needed I could extract the coordinates from subimage-find
	# and use Imagemagick to just plop a blank image over the silvers. Will hold off on this for now though.
	subimage-find -v /media/sf_Screenshots/rekognition/"$file".png ./assets/change.png --confidence 0.5 > ./tmp.txt
	#Expect output to be something like: 
	#... "[(slice(132L, 151L, None), slice(275L, 292L, None)), (slice(156L, 175L, None), slice(310L, 327L, None))]" 
	# and output to ./tmp.txt

	# hacked perl script to process the slices from above and execute ImageMagick convert to overlay the cover on the image
	perl sliceToIm.pl "$file"

	# remove the temp files generated by subimage-find
	rm ./correlations.png
	rm ./tmp.txt
done
