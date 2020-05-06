import React, { useState, useEffect } from "react"
import MaterialTable from "material-table"
import PureProgressSpinner from "../components/PureProgressSpinner"
import { baseAxios } from "../config/config"
import alertify from 'alertifyjs';

const AlertManager = () => {
    const [loaded, setLoaded] = useState(false)

    const [state, setState] = useState({
        columns: [
            { title: 'ID', field: 'alertId', editable: 'never' },
            { title: 'Sensor ID', field: 'sensorId', editable: 'never' },
            { title: 'CO2 Level', field: 'co2Level', editable: 'never' },
            { title: 'Smoke Level', field: 'smokeLevel', editable: 'never' },
            { title: 'Time', field: 'time', editable: 'never', type: "text" },
            { title: 'Status', field: 'status', type: 'text' },
        ]

    })

    const [entries, setEntries] = useState({
        data: [
            {
                alertId: null,
                sensorId: "s",
                co2Level: 0,
                smokeLevel: 0,
                time: null,
                roomId: null,
                status: false,
            }
        ]
    })

    useEffect(() => {
        baseAxios
            .get("/alerts")
            .then(response => {
                let data = []
                console.log(response.data)
                if (response.data) setLoaded(true)
                response.data.data.forEach(el => {
                    data.push({
                        alertId: el.alertId,
                        sensorId: el.sensorId,
                        co2Level: el.co2Level,
                        smokeLevel: el.smokeLevel,
                        time: el.time,
                        status: el.status,
                    })
                })
                setEntries({ data: data })
            })
            .catch(function (error) {
                console.log(error)
            })
    }, [])

    if (!loaded) return <PureProgressSpinner message="Loading ...." />

    return (
        <MaterialTable
            searchable
            title="Alerts"
            columns={state.columns}
            data={entries.data}
            options={{
                addRowPosition: "first",
            }}
            editable={{

                onRowUpdate: (newData, oldData) =>
                    new Promise(resolve => {
                        setTimeout(() => {
                            resolve()
                            baseAxios
                                .put("/alerts/status_update/" + oldData.alertId, newData)
                                .then(function (response) {
                                    console.log(response)
                                    if (response.data.message === 'Alert Updated') {
                                        newData.alertId = oldData.alertId
                                        alertify.success("Updated")

                                        setEntries(prevState => {
                                            const data = [...prevState.data]
                                            data[data.indexOf(oldData)] = newData
                                            return { ...prevState, data }
                                        })
                                    } else {
                                        alertify.error("Couldn't update")
                                    }
                                })
                                .catch(function (error) {
                                    console.log(error)
                                })
                        }, 600)
                    }),

                onRowDelete: (oldData) =>
                    new Promise(resolve => {
                        setTimeout(() => {
                            resolve()
                            //deleting from DB
                            // const data = [...entries.data]
                            // data.splice(data.indexOf(oldData), 1)
                            baseAxios
                                .delete("alerts/delete/" + oldData.alertId)
                                .then(response => {
                                    if (response.status === 204) {
                                        alertify.success("Deleted")

                                        // setEntries({ ...entries, data })
                                        setEntries(prevState => {
                                            const data = [...prevState.data]
                                            data.splice(data.indexOf(oldData), 1)
                                            return { ...prevState, data }
                                        })
                                    } else {
                                        alertify.error("Couldn't Delete")
                                    }
                                })
                                .catch(error => {
                                    console.log(error)
                                })
                        }, 600)
                    })
            }}
        />
    )
}

export default AlertManager