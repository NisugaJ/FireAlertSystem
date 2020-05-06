import React, { useState, useEffect } from "react"
import MaterialTable from "material-table"
import PureProgressSpinner from "../components/PureProgressSpinner"
import { baseAxios } from "../config/config"
import alertify from 'alertifyjs';
const SensorManager = () => {
    const [loaded, setLoaded] = useState(false)

    const [state, setState] = useState({
        columns: [
            { title: 'ID', field: 'fireSensorId', editable: 'never' },
            { title: 'Description', field: 'description' },
            { title: 'CO2 Level', field: 'currentCO2Level', editable: 'never' },
            { title: 'Smoke Level', field: 'currentSmokeLevel', editable: 'never' },
            { title: 'Active', field: 'active', type: 'boolean' },
            { title: 'Deletion Requested', field: 'deleteRequest', type: 'boolean' },
            {
                title: 'Floor Id',
                field: 'floorId',
                lookup: { 0: 'Ground Floor', 1: 'First Floor', 2: 'Second Floor', 3: 'Third Floor', 4: 'Fourth Floor' },
            },
            {
                title: 'Room Id',
                field: 'roomId',
                lookup: { 0: 'Reception', 1: 'Room 1', 2: 'Room 2', 3: 'Room 3', 4: 'Room 4' },
            },
        ]

    })

    const [entries, setEntries] = useState({
        data: [
            {
                fireSensorId: null,
                description: "s",
                active: false,
                currentCO2Level: 0,
                currentSmokeLevel: 0,
                floorId: null,
                roomId: null,
                deleteRequest: false,
            }
        ]
    })

    useEffect(() => {
        baseAxios
            .get("/sensors")
            .then(response => {
                let data = []
                console.log(response.data)
                if (response.data) setLoaded(true)
                response.data.data.forEach(el => {
                    data.push({
                        fireSensorId: el.fireSensorId,
                        description: el.description || "-",
                        active: el.active,
                        floorId: el.floorId,
                        roomId: el.roomId,
                        currentCO2Level: el.currentCO2Level,
                        currentSmokeLevel: el.currentSmokeLevel,
                        deleteRequest: el.deleteRequest
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
            title="Sensors"
            columns={state.columns}
            data={entries.data}
            options={{
                addRowPosition: "first",

            }}
            editable={{
                onRowAdd: (newData) =>
                    new Promise((resolve) => {
                        setTimeout(() => {
                            resolve();
                            console.log(newData);

                            baseAxios
                                .post("/sensors/create", newData)
                                .then(function (response) {
                                    console.log(response)
                                    if (response.data.data.fireSensorId) {
                                        newData.fireSensorId = response.data.data.fireSensorId
                                        alertify.success("Saved")
                                        setEntries(prevState => {
                                            const data = [...prevState.data]
                                            data.push(newData)
                                            return { ...prevState, data }
                                        })
                                    } else {
                                        alertify.error("Saved")

                                    }
                                })
                                .catch(function (error) {
                                    console.log(error)
                                })
                        }, 600)
                    }),


                onRowUpdate: (newData, oldData) =>
                    new Promise(resolve => {
                        setTimeout(() => {
                            resolve()
                            baseAxios
                                .put("/sensors/update/" + oldData.fireSensorId, newData)
                                .then(function (response) {
                                    console.log(response)
                                    if (response.data.message === 'Sensor Updated') {
                                        newData.fireSensorId = oldData.fireSensorId
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
                                .post("sensors/delete_request", {
                                    "id": oldData.fireSensorId
                                })
                                .then(response => {
                                    if (response.data.message === "Updated Sensor") {
                                        alertify.success("Delete Request Sent")

                                        // setEntries({ ...entries, data })
                                        // setEntries(prevState => {
                                        //     const data = [...prevState.data]
                                        //     data.splice(data.indexOf(oldData), 1)
                                        //     return { ...prevState, data }
                                        // })
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

export default SensorManager