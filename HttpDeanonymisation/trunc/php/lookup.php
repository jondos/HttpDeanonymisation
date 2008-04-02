<?php
  include("geoip.inc");
  include("geoipcity.inc");
  include("geoipregionvars.php");
  $ip = $_GET['ip'];

  $gi = geoip_open("/usr/local/share/GeoIP/GeoIP.dat",GEOIP_STANDARD);
  $country_code = geoip_country_code_by_addr($gi, $ip);
  geoip_close($gi);
  $giorg = geoip_open("/usr/local/share/GeoIP/GeoIPOrg.dat",GEOIP_STANDARD);
  $org = geoip_org_by_addr($giorg,$ip);
  geoip_close($giorg);
  $giisp = geoip_open("/usr/local/share/GeoIP/GeoIPISP.dat",GEOIP_STANDARD);
  $isp = geoip_org_by_addr($giisp,$ip);
  geoip_close($giisp);
  $gi = geoip_open("/usr/local/share/GeoIP/GeoLiteCity.dat",GEOIP_STANDARD);
  $record = geoip_record_by_addr($gi,$ip);
  $region=$GEOIP_REGION_NAME[$record->country_code][$record->region];
  $city=$record->city;
  $postal_code=$record->postal_code;
  $latitude=$record->latitude;
  $longitude=$record->longitude;
  geoip_close($gi);
  echo $country_code.", ".$region.", ".$city."\n";
  echo $org."\n";
  echo $isp."\n";
  echo $latitude."\n";
  echo $longitude."\n"; 
  echo gethostbyaddr($ip);
?>
