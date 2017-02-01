package utils;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Some common lat long computations.
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class EarthFunctions {
    private static final double EARTH_RADIUS = 6378137; // in meters

    /**
     * Returns the distance between two coordinates.
     * 
     * @param lat1
     * @param lat2
     * @param lon1
     * @param lon2
     * @return distance between the coordinates in meters.
     */
    public static double haversianDistance(double lat1, double lat2, double lon1, double lon2) {
	double dLat = Math.toRadians(lat1 - lat2);
	double dLng = Math.toRadians(lon1 - lon2);
	double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat2))
		* Math.cos(Math.toRadians(lat1)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	double dist = EARTH_RADIUS * c;

	return dist;
    }

    /**
     * Returns the distance between two coordinates.
     * 
     * @param coord1
     * @param coord2
     * @return distance between the coordinates in meters.
     */
    public static double haversianDistance(Coordinate coord1, Coordinate coord2) {
	double dLat = Math.toRadians(coord1.y - coord2.y);
	double dLng = Math.toRadians(coord1.x - coord2.x);
	double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(coord2.y))
		* Math.cos(Math.toRadians(coord1.y)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	double dist = EARTH_RADIUS * c;

	return dist;
    }

    /**
     * Returns a {@link Coordinate} at a given distance and bearing from the one
     * passed as a parameter.
     * 
     * @param init
     *            The initial coordinate.
     * @param distance
     *            Distance in meters.
     * @param bearing
     *            the bearing angle in radians.
     * @return
     */
    public static Coordinate getPointAtDistanceAndBearing(Coordinate init, double distance, double bearing) {

	double lat1 = Math.toRadians(init.y);
	double lon1 = Math.toRadians(init.x);

	double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / EARTH_RADIUS)
		+ Math.cos(lat1) * Math.sin(distance / EARTH_RADIUS) * Math.cos(bearing));
	double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distance / EARTH_RADIUS) * Math.cos(lat1),
		Math.cos(distance / EARTH_RADIUS) - Math.sin(lat1) * Math.sin(lat2));
	return new Coordinate(Math.toDegrees(lon2), Math.toDegrees(lat2));

    }

    /**
     * Returns the bearing i.e the angle between the line and the true north.
     * 
     * @param lat1
     * @param lat2
     * @param lon1
     * @param lon2
     * @return bearing in degrees
     */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
	lat1 = Math.toRadians(lat1);
	lat2 = Math.toRadians(lat2);
	double dLon = Math.toRadians(lon2 - lon1);

	double y = Math.sin(dLon) * Math.cos(lat2);
	double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
	double theta = Math.atan2(y, x) * (180.0 / Math.PI);
	return (theta + 360) % 360;
    }

    /**
     * Returns the bearing i.e the angle between the line and the true north.
     * 
     * @param coord1
     * @param coord2
     * @return bearing in degrees
     */
    public static double bearing(Coordinate coord1, Coordinate coord2) {
	double lat1 = Math.toRadians(coord1.y);
	double lat2 = Math.toRadians(coord2.y);
	double dLon = Math.toRadians(coord2.x - coord1.x);

	double y = Math.sin(dLon) * Math.cos(lat2);
	double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
	double theta = Math.atan2(y, x) * (180.0 / Math.PI);
	return (theta + 360) % 360;
    }

    public static Coordinate midPoint(Coordinate coord1, Coordinate coord2) {
	return new Coordinate((coord1.x + coord2.x) / 2.0, (coord1.y + coord2.y) / 2.0);
    }

}
