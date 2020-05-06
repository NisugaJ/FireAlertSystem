import React, { Component } from "react";
import "./App.css";
import SensorCard from "./components/sensorCard";
import Axios from "axios";
import { Config, baseAxios } from "./config/config.js";

class App extends Component {

  constructor(props) {
    super(props);
    this.state = {
      sensors: [],
    };
  }
  componentDidMount() {
    this.sensorData();
    this.timerId = setInterval(() => this.sensorData(), 40000);
  }
  componentWillUnmount() {
    clearInterval(this.timerId);
  }
  sensorData() {
    baseAxios.get("/sensors")
      .then((response) => {
        console.log(response);
        this.setState({ sensors: response.data.data });
      })
      .catch((error) => {
        console.log(error);
      });
  }
  sensorCard() {
    return this.state.sensors.map((object, index) => {
      return (
        <SensorCard
          description={object.description}
          key={object.fireSensorId}
          active={object.active}
          co2_level={object.currentCO2Level}
          smoke_level={object.currentSmokeLevel}
          floor_id={object.floorId}
          room_id={object.roomId}
        />
      );
    });
  }
  render() {
    return <div className="App">{this.sensorCard()}</div>;
  }
}

export default App;
