import React, { Component } from "react";
import { Card, Typography, CardContent, Grid } from "@material-ui/core";
import { baseAxios, Config } from "../config/config";
import { MainOuterTheme } from "../styles/theme";
import Skeleton from "@material-ui/lab/Skeleton";
import alertify from "alertifyjs"

export default class SensorCards extends Component {

  constructor(props) {
    super(props);
    this.onChangeCO2Level = this.onChangeCO2Level.bind(this);
    this.onChangeSmokeLevel = this.onChangeSmokeLevel.bind(this);
    this.onChangeRoomNo = this.onChangeRoomNo.bind(this);
    this.onChangeFloorNo = this.onChangeFloorNo.bind(this);

    this.state = {
      sensors: [],
      floor_no: '',
      room_no: '',
      smoke_level: '',
      co2_level: ''
    };
  };

  onChangeCO2Level(e) {
    this.setState({
      co2_level: e.target.value
    });
  }
  onChangeSmokeLevel(e) {
    this.setState({
      smoke_level: e.target.value
    });
  }
  onChangeRoomNo(e) {
    this.setState({
      room_no: e.target.value
    });
  }

  onChangeFloorNo(e) {
    this.setState({
      floor_no: e.target.value
    });
  }

  componentDidMount() {
    this.sensorData();
    this.timerId = setInterval(() => this.sensorData(), 3000);
  }
  componentWillUnmount() {
    clearInterval(this.timerId);
  }
  sensorData() {
    baseAxios.get(Config.apiBaseURL + "/sensors")
      .then((response) => {
        this.setState({ sensors: response.data.data });

      })
      .catch((error) => {
        console.log(error);
      });
  }


  render() {
    // this.state.sensors.map(item => {
    //   if (item.currentCO2Level > 5 || item.cuurentSmokeLevel > 5)
    //     alertify.warning("Fire Alert at " + item.description)
    // })

    return (

      <Grid container spacing={1}>
        <Grid container item xs={12} spacing={3}>
          {this.state.sensors.map((tile) => (
            <Skeleton variant="rect" width={"auto"} height={"auto"} animation={(tile.currentCO2Level > 5 || tile.currentSmokeLevel > 5) ? "pulse" : "false"} >
              <Card style={{
                margin: "5px",
                backgroundColor: (tile.currentCO2Level > 5 || tile.currentSmokeLevel > 5) ? MainOuterTheme.palette.error.main : "green",
                color: (tile.currentCO2Level > 5 || tile.currentSmokeLevel > 5) ? "white" : "black",

              }}>
                <CardContent>
                  <Typography style={{ fontWeight: "bold" }} color={
                    (tile.currentCO2Level > 5 || tile.currentSmokeLevel > 5) ? "white" : "black"
                  } gutterBottom>
                    {tile.description}
                  </Typography>
                  <Typography variant="h5" component="h2">
                    CO2: {tile.currentCO2Level}<br />
                Smoke: {tile.currentSmokeLevel}
                  </Typography>
                  <Typography color={
                    (tile.currentCO2Level > 5 || tile.currentSmokeLevel > 5) ? "white" : "black"

                  }>
                    {tile.active ? "Active" : "Inactive"}
                  </Typography>
                  <Typography variant="body2" component="p">
                    Floor :    {tile.floorId}
                    <br />
                Room : {tile.roomId}
                  </Typography>
                </CardContent>
              </Card>
            </Skeleton>
          ))}
        </Grid>
      </Grid>
    );
  }
}