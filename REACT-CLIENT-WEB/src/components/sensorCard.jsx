import React from "react";

export default function sensorCard(props) {
  return (
    <div
      className="card"
      style={{
        backgroundColor:
          props.co2_level >= 5 || props.smoke_level >= 5 ? "brown" : "green",
      }}
    >
      <table>
        <tbody>
          <tr>
            <td>Description</td>
            <td>{props.description}</td>
          </tr>
          <tr>
            <td>Active</td>
            <td>{props.active === true ? "True" : "False"}</td>
          </tr>
          <tr
            style={{
              fontWeight: props.co2_level >= 5 ? "bold" : "none",
              color: props.co2_level >= 5 ? "orange" : "white",
            }}
          >
            <td>CO2 Level</td>
            <td>{props.co2_level}</td>
          </tr>
          <tr
            style={{
              fontWeight: props.smoke_level >= 5 ? "bold" : "none",
              color: props.smoke_level >= 5 ? "orange" : "white",
            }}
          >
            <td>Smoke Level</td>
            <td>{props.smoke_level}</td>
          </tr>
          <tr>
            <td>Floor ID</td>
            <td>{props.floor_id}</td>
          </tr>
          <tr>
            <td>Room No</td>
            <td>{props.room_id}</td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}
