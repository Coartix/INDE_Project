import React, { useState, useEffect } from 'react';
import './App.css';
import List from '@mui/joy/List';
import ListItem from '@mui/joy/ListItem';
import ListSubheader from '@mui/joy/ListSubheader';
import Sheet from '@mui/joy/Sheet';

import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

import L from 'leaflet';

import markerIcon from './happyFace.png';

interface Report {
  id: number;
  location: number[];
  citizens: string[];
  score: number[];
  words: string[];
}

const App: React.FC = () => {
  const [reports, setReports] = useState<Report[]>([]);
  const [positions, setPositions] = useState<number[][] | null>(null);

  const iconPerson = new L.Icon({
    iconUrl: require('./happyFace.png'),
    iconRetinaUrl: require('./happyFace.png'),
    iconSize: new L.Point(15, 15),
    className: 'leaflet-div-icon'
  });

  useEffect(() => {
    const fetchReports = async () => {
      try {
        const response = await fetch("http://localhost:5000/reports");
        const data = await response.json();
        setReports(data);
      } catch (error) {
        console.error("Error fetching reports:", error);
      }
    };

    const intervalId = setInterval(fetchReports, 5000); // Fetch reports every 5 seconds

    return () => {
      clearInterval(intervalId);
    };
  }, []);

  // Get all positions from reports when they are updated
  useEffect(() => {
    const positions = reports.map((report) => report.location);
    setPositions(positions);
  }
    , [reports]);

  return (
    <div className='App'>
      <div className='inline'>
        <div>
          <h2>Last 10 alerts</h2>
          <Sheet
            variant="outlined"
            sx={{
              width: 320,
              maxHeight: 750,
              overflow: 'auto',
              borderRadius: 'sm',
            }}
          >
            <List>
              {reports.slice(-10).map((report, index) => (
                <ListItem nested key={index}>
                  <ListSubheader sticky>Report {index + 1}</ListSubheader>
                  <List>
                    <p>Id: {report.id}</p>
                    <p>Location: {report.location.join(", ")}</p>
                    <p>Citizens: {report.citizens.join(", ")}</p>
                    <p>Score: {report.score.join(", ")}</p>
                    <p>Words: {report.words.join(", ")}</p>
                  </List>
                </ListItem>
              ))}
            </List>
          </Sheet>
        </div>
        <div className="map" id="map">
          {positions != null && positions.length > 0 && (
            <MapContainer center={positions[0] as any} zoom={4} scrollWheelZoom={true}>
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">
                OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {positions.map((position, index) => (
                <Marker key={index} position={position as any} icon={iconPerson}>
                  <Popup>
                    <img src="https://www.flaticon.com/svg/static/icons/svg/2938/2938617.svg" alt="icon" width="30" height="30" />
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          )}
          <div />
        </div>
      </div>
    </div>
  );
};

export default App;