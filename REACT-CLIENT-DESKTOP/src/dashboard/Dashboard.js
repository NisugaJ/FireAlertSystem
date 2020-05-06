import React from 'react';
import SensorCards from '../components/SensorCard';
import Controls from './ControlPanel';
import { Grid } from '@material-ui/core';


const Dashboard = () => {
    return (
        <Grid >
            <Grid item xs={12}>
                <Controls />
            </Grid>
            <Grid item xs={12}>
                <SensorCards />
            </Grid>
        </Grid>
    )
}

export default Dashboard;
