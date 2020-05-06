import React from 'react';
import PropTypes from 'prop-types';
import { makeStyles, ThemeProvider } from '@material-ui/core/styles';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import { Switch, Route, Redirect, BrowserRouter as Router } from 'react-router-dom';
import NoMatch from './components/NotFound_404';
import AdminLogin from './login/Login';
import { isLogged, logOut } from './components/auth';
import { MainOuterTheme } from './styles/theme';
import Dashboard from './dashboard/Dashboard';
import SensorManager from './sensors/ManageSensors';
import AlertManager from './alerts/Alerts';

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      style={{
        width: "auto",
        height: "fit-content"
      }}
      role="tabpanel"
      hidden={value !== index}
      id={`vertical-tabpanel-${index}`}
      aria-labelledby={`vertical-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box p={3}>
          <Typography>{children}</Typography>
        </Box>
      )}
    </div>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.any.isRequired,
  value: PropTypes.any.isRequired,
};

function a11yProps(index) {
  return {
    id: `vertical-tab-${index}`,
    'aria-controls': `vertical-tabpanel-${index}`,
  };
}

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
    display: 'flex',
    height: "auto",
  },
  tabs: {
    marginRight: "6px",
    borderRight: `1px solid ${theme.palette.divider}`,
  },
}));

function VerticalTabs() {
  const classes = useStyles();
  const [value, setValue] = React.useState(0);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  return (
    <div className={classes.root}>
      <Tabs
        orientation="vertical"
        variant="scrollable"
        value={value}
        onChange={handleChange}
        aria-label="Vertical tabs example"
        className={classes.tabs}
      >
        <Tab label="Dashboard" {...a11yProps(0)} />
        <Tab label="Sensors" {...a11yProps(1)} />
        <Tab label="Alerts" {...a11yProps(2)} />
        <Tab label="Logout"

          textColor="tabpanel"
          href="/admin"
          style={{ textDecoration: "none" }}
          onClick={() => {
            logOut()
          }}
        >
        </Tab>

      </Tabs>

      <TabPanel value={value} index={0}>
        <Dashboard />
      </TabPanel>
      <TabPanel value={value} index={1}>
        <SensorManager />
      </TabPanel>
      <TabPanel value={value} index={2}>
        <AlertManager />
      </TabPanel>


    </div>
  );
}

// A wrapper for <Route> that redirects to the login
// screen if you're not yet authenticated.
function PrivateRoute({ children, ...rest }) {
  if (isLogged()) console.log("Logeeeeeeeeeeeeeee");

  return (
    <Route
      {...rest}
      render={({ location }) =>
        isLogged() ? (
          children
        ) : (
            <Redirect
              to={{
                pathname: "/login",
                state: { from: location }
              }}
            />
          )
      }
    />
  )
}


export default function App() {
  return (
    <ThemeProvider theme={MainOuterTheme}>

      <Router>
        <Switch>
          <Route exact path="/">
            {isLogged() ?
              <VerticalTabs /> : <AdminLogin />}
          </Route>
          <Route path="/login">
            <AdminLogin />
          </Route>
          {/* <Route path="/admin"> */}
          <PrivateRoute path="/admin">
            <VerticalTabs />
          </PrivateRoute>
          {/* </Route> */}
          <Route path="*">
            <NoMatch />
          </Route>
        </Switch>
      </Router>
    </ThemeProvider>
  )
}
