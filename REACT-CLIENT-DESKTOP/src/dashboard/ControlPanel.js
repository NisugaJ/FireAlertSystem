import React, { useState } from 'react';
import { Button, Grid, Typography } from '@material-ui/core';
import PlayCircleOutlineIcon from '@material-ui/icons/PlayCircleOutline'
import PauseIcon from '@material-ui/icons/Pause';
import { baseAxios } from '../config/config';
import alertify from "alertifyjs"

const Controls = () => {

    const [rmiServerStatus, setRmiServerStatus] = useState(sessionStorage.getItem("rmi_server") | false)
    const [rmiClientsStatus, setRmiClientsStatus] = useState(sessionStorage.getItem("rmi_clients") | false)


    const startRMIserver = () => {
        baseAxios.post("init/start_rmi_server",
            {
                "rmi_server_start": true
            }
        ).then(response => {
            const data = response.data
            if (data.message === 'Started') {
                setRmiServerStatus(true)
                sessionStorage.setItem("rmi_server", true)
                console.log("Sensor Monitoring Server Start Successfull")
                alertify.success("Sensor Monitoring started")
            } else {
                sessionStorage.setItem("rmi_server", false)
                console.log("Sensor Monitoring Server Start Failed")
                alertify.error("Sensor Monitoring Failed to start")
            }
        })
    }

    const shutDownRMIserver = () => {
        baseAxios.post("init/shutdown_rmi_server",
            {
                "rmi_server_shutdown": true
            }
        ).then(response => {
            const data = response.data
            if (data.message === 'Shutdown OK') {
                setRmiServerStatus(false)
                sessionStorage.setItem("rmi_server", false)
                console.log("Sensor Monitoring Server Shutdown Successfull")
                alertify.success("Sensor Monitoring Server is Down now")
            } else {
                console.log("Sensor Monitoring Server Shutdown Failed")
                alertify.error("Sensor Monitoring Server Shutdown Failed")
            }
        })
    }

    const startRMIclients = () => {
        baseAxios.post("init/start_rmi_sensors",
            {
                "rmi_sensors_start": true
            }
        ).then(response => {
            const data = response.data
            if (data.message === 'Started Clients') {
                setRmiClientsStatus(true)
                sessionStorage.setItem("rmi_clients", true)
                console.log("Sensors Start Successfull")
                alertify.success("Sensors started")
            } else {
                console.log("Sensors Start Failed")
                alertify.error("Failed to Start Sensors")
            }
        })
    }

    const shutdownRMIclients = () => {
        baseAxios.post("init/shutdown_rmi_sensor_clients",
            {
                "rmi_sensors_shutdown": true
            }
        ).then(response => {
            const data = response.data
            if (data.message === 'ClientS Shutdown OK') {
                setRmiServerStatus(false)
                sessionStorage.setItem("rmi_clients", false)
                console.log("Sensor Monitoring Server Shutdown Successfull")
                alertify.success("Sensors Stopped")

            } else {
                console.log("Sensor Monitoring Server Shutdown Failed")
                alertify.success("Failed to Shutdown Sensors")
            }
        })
    }

    return (
        <div style={{ height: 100 }}>
            <Grid container >
                <Grid item xs={6}>
                    <Typography>Sensor Monitor (RMI Server)
                    {(rmiServerStatus) ?
                            <Button variant="text" onClick={shutDownRMIserver}>
                                <PauseIcon />
                            </Button>
                            :
                            <Button variant="text" onClick={startRMIserver}>
                                <PlayCircleOutlineIcon />
                            </Button>
                        }
                    </Typography>
                </Grid>
                <Grid item xs={6}>
                    <Typography>Sensors

                    {(rmiClientsStatus) ?
                            <Button tooltip="Shutdown Sensors" variant="text" onClick={shutdownRMIclients}>
                                <PauseIcon />
                            </Button>
                            :
                            <Button variant="text" onClick={startRMIclients}>
                                <PlayCircleOutlineIcon />
                            </Button>
                        }
                    </Typography>


                </Grid>
            </Grid>
        </div>
    )
}

export default Controls