package com.geeksville.json

import org.json4s.Formats
import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.native.Serialization
import com.geeksville.flight.Location
import org.json4s.CustomSerializer
import org.json4s.Extraction

object GeoJSON {

  class LocationSerializer extends CustomSerializer[Location](format => (
    {
      case JArray(s) =>
        throw new Exception("Not implemented")
    },
    {
      case x: Location =>
        var s = List(JDouble(x.lon), JDouble(x.lat))
        x.alt.foreach { a =>
          s = s :+ JDouble(a)
        }
        JArray(s): JArray
    }))

  // Sets up automatic case class to JSON output serialization
  implicit val jsonFormats: Formats = DefaultFormats ++ GeeksvilleFormats + new LocationSerializer

  def makePoint(coords: Location): JObject = {
    val j = Extraction.decompose(coords)
    ("type" -> "Point") ~ ("coordinates" -> j)
  }

  def makeLineString(coords: Seq[Location]): JObject = {
    ("type" -> "LineString") ~ ("coordinates" -> Extraction.decompose(coords))
  }

  def makePolygon(coords: Seq[Location]): JObject = {
    ("type" -> "Polygon") ~ ("coordinates" -> Extraction.decompose(coords))
  }

  def makeMultiLineString(coords: Seq[Seq[Location]]): JObject = {
    ("type" -> "MultiLineString") ~ ("coordinates" -> Extraction.decompose(coords))
  }

  def makeGeometryCollection(geos: Seq[JObject]) = {
    ("type" -> "GeometryCollection") ~ ("geometries" -> Extraction.decompose(geos))
  }

  def makeFeatureCollection(f: Seq[JObject]) = {
    ("type" -> "FeatureCollection") ~ ("features" -> Extraction.decompose(f))
  }

  def makeFeature(geo: JObject, properties: JValue = JNull): JObject = {
    ("type" -> "Feature") ~ ("geometry" -> geo) ~ ("properties" -> properties)
  }

  /**
   * color such as "#ff4444"
   */
  def makeMarker(coords: Location, title: String, color: Option[String] = None, size: String = "medium", symbol: Option[String] = None) = {
    val geo = makePoint(coords)
    var ps = ("marker-size" -> size) ~ ("title" -> title) ~ ("marker-color" -> color) ~ ("marker-symbol" -> symbol)

    val props: JObject = ps
    makeFeature(geo, props)
  }

  class BoundingBox {
    case class LatLngAlt(var lat: Double, var lon: Double, var alt: Double)
    var southWest = LatLngAlt(Double.MaxValue, Double.MaxValue, Double.MaxValue)
    var northEast = LatLngAlt(Double.MinValue, Double.MinValue, Double.MinValue)

    def range = Seq(southWest.lon, southWest.lat, southWest.alt,
      northEast.lon, northEast.lat, northEast.alt)

    def toJSON: JObject = ("bbox" -> range)

    /// Expand the bounding box to include the specified point
    def addPoint(l: Location) {
      southWest.lat = math.min(l.lat, southWest.lat)
      southWest.lon = math.min(l.lon, southWest.lon)
      southWest.alt = math.min(l.alt.getOrElse(0.0), southWest.alt)

      northEast.lat = math.max(l.lat, northEast.lat)
      northEast.lon = math.max(l.lon, northEast.lon)
      northEast.alt = math.max(l.alt.getOrElse(0.0), northEast.alt)
    }
  }

  def addBoundingBox(src: JObject, bbox: BoundingBox) = {

    src ~ bbox.toJSON
  }
}