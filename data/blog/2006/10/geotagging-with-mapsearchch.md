[Flickr](http://www.flickr.com/photos/bruchez/) (the well-known photo sharing Web site) introduced [geotagging](http://en.wikipedia.org/wiki/GeoTagging) on August 28, 2006, but I didn't get interested in that feature until a few weeks ago, mainly because I don't own a GPS (yet). However, using a GPS and a tool such as [RoboGEO](http://www.robogeo.com/) is not the only way to geotag your photos. With Flickr, you can simply drag and drop a photo to a map, and position it more or less precisely. The problem, for me at least, is that Flickr uses Yahoo! Maps (quite logical, since Yahoo! acquired Flickr in 2005), which is by far not the best mapping service for Switzerland (check out [my Flickr map](http://www.flickr.com/map/?&user_id=73293249@N00&fLat=46.245938&fLon=6.951598&zl=8) and you'll see why). Fortunately, somebody created [a very handy bookmarklet](https://web.archive.org/web/20060629012952/http://typolis.net:80/sumaato/stories/4323/) which allows to geotag your photos on Flickr using [Google Maps](http://maps.google.com/), which is already better for Switzerland than Yahoo! Maps. Even better yet is [map.search.ch](http://map.search.ch/), certainly the most detailed mapping service for Switzerland.

As far as I know and contrary to [Google Maps](https://developers.google.com/maps/) and [Yahoo! Maps](http://developer.yahoo.com/maps/), map.search.ch does not offer an API to develop third-party applications. So how exactly do you geotag your photos using that service? The first step is to use the following URL:

<http://map.search.ch/600000,200000>

This will force map.search.ch to use [Swiss coordinates](http://en.wikipedia.org/wiki/Swiss_coordinates) (at least for its reference point, located in Bern). By default, you will be in "interactive mode", so simply use the mouse to navigate across Switzerland and to zoom in and out. When you have found the position you were looking for, simply switch to "static mode". You will get a URL like this:

<http://map.search.ch/600000,200000?x=-123746&y=102334&z=1024>

The Swiss coordinates of the point at the center of the image are given by:

Y = 600000 + (x * 1024 / z) / 1.25
X = 200000 - (y * 1024 / z) / 1.25

You can convert the Swiss coordinates above to [WGS84 coordinates](http://en.wikipedia.org/wiki/WGS84) using, for example, [the formulas given by swisstopo](https://web.archive.org/web/20080328183728/http://www.swisstopo.admin.ch/internet/swisstopo/en/home/topics/survey/sys/refsys/projections.html):

Y' = (Y - 600000) / 1000000
X' = (X - 200000) / 1000000

L' = 2.6779094
 + 4.728982 * Y'
 + 0.791484 * Y' * X'
 + 0.1306 * Y' * X'^2
 - 0.0436 * Y'^3P' = 16.9023892
 + 3.238272 * X'
 - 0.270978 * Y'^2
 - 0.002528 * X'^2
 - 0.0447 * Y'^2 * X'
 - 0.0140 * X'^3

L = L' * 100 / 36
P = P' * 100 / 36

Then, simply add the following tags to your photo:

geotagged
geo:lon=xx.xxxxxx (value of L)
geo:lat=xx.xxxxxx (value of P)

Finally, use the [Flickr Map GeoCoding Bookmarklet](https://web.archive.org/web/20060629012952/http://typolis.net:80/sumaato/stories/4323/) I mentioned above, for example, to geotag your photo using the coordinates given in the tags. And, voilà, your photo is geotagged!

To make the whole process easier, I'm currently using a simple utility that monitors the clipboard and automatically converts the map.search.ch URLs directly into Flickr tags. Of course, this is not a satisfying solution, but it's still better than nothing!
